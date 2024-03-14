// ============================================================================
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package tribefire.extension.process.processing.base.wire.space;

import static com.braintribe.wire.api.util.Sets.set;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.codec.marshaller.bin.Bin2Marshaller;
import com.braintribe.common.concurrent.TaskSchedulerImpl;
import com.braintribe.execution.CustomThreadFactory;
import com.braintribe.execution.ExtendedThreadPoolExecutor;
import com.braintribe.gm.service.access.wire.common.contract.CommonAccessProcessingContract;
import com.braintribe.gm.service.wire.common.contract.CommonServiceProcessingContract;
import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.messaging.dmb.GmDmbMqMessaging;
import com.braintribe.model.processing.lock.impl.SimpleCdlLocking;
import com.braintribe.model.processing.securityservice.commons.provider.StaticUserSessionHolder;
import com.braintribe.model.processing.securityservice.commons.scope.StandardUserSessionScoping;
import com.braintribe.model.processing.service.common.context.UserSessionStack;
import com.braintribe.model.processing.service.common.eval.AuthorizingServiceRequestEvaluator;
import com.braintribe.model.service.api.InstanceId;
import com.braintribe.model.user.Role;
import com.braintribe.model.user.User;
import com.braintribe.model.usersession.UserSession;
import com.braintribe.model.usersession.UserSessionType;
import com.braintribe.transport.messaging.api.MessagingConnectionProvider;
import com.braintribe.transport.messaging.api.MessagingContext;
import com.braintribe.transport.messaging.api.MessagingSessionProvider;
import com.braintribe.transport.messaging.dbm.GmDmbMqConnectionProvider;
import com.braintribe.transport.messaging.impl.StandardMessagingSessionProvider;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.cortex.leadership.impl.LockingBasedLeadershipManager;
import tribefire.extension.process.model.deployment.ConditionProcessor;
import tribefire.extension.process.model.deployment.TransitionProcessor;
import tribefire.extension.process.processing.base.wire.contract.TestPlatformContract;
import tribefire.platform.impl.deployment.BasicDeployRegistry;
import tribefire.platform.impl.deployment.ComponentInterfaceBindingsRegistry;
import tribefire.platform.impl.deployment.ConfigurableDeployedUnit;
import tribefire.platform.impl.deployment.proxy.ProxyingDeployedComponentResolver;
import tribefire.platform.impl.topology.CartridgeLiveInstances;
import tribefire.platform.impl.worker.impl.BasicWorkerManager;

@Managed
public class TestPlatformSpace implements TestPlatformContract {
	
	protected static final Set<String> internalRoles = set("tf-internal");
	protected static final String internalName = "internal";
	
	@Import
	private CommonAccessProcessingContract commonAccessProcessing;
	
	@Import
	private CommonServiceProcessingContract commonServiceProcessing;
	
	@Override
	@Managed
	public MessagingSessionProvider messagingSessionProvider() {
		StandardMessagingSessionProvider bean = new StandardMessagingSessionProvider();
		bean.setMessagingConnectionProvider(messagingConnectionSupplier());
		return bean;
	}
	
	@Managed
	private MessagingConnectionProvider<?> messagingConnectionSupplier() {
		GmDmbMqConnectionProvider bean = new GmDmbMqConnectionProvider();
		bean.setConnectionConfiguration(GmDmbMqMessaging.T.create());
		bean.setMessagingContext(context());

		return bean;
	}

	@Managed
	private MessagingContext context() {
		MessagingContext bean = new MessagingContext();
		bean.setMarshaller(messageMarshaller());
		
		InstanceId instanceId = instanceId();
		bean.setApplicationId(instanceId.getApplicationId());
		bean.setNodeId(instanceId.getNodeId());
		return bean;
	}

	@Managed
	private Marshaller messageMarshaller() {
		Bin2Marshaller bean = new Bin2Marshaller();
		return bean;
	}
	
	@Override
	@Managed
	public BasicWorkerManager workerManager() {
		BasicWorkerManager bean = new BasicWorkerManager();
		bean.setLeadershipManagerSuppier(this::leadershipManager);
		bean.setExecutorService(threadPool());
		bean.setSystemUserSessionProvider(systemUserSessionSupplier());
		bean.setUserSessionScoping(userSessionScoping());
		return bean;
	}
	
	@Override
	public void startWorkerManager() {
		workerManager().start();
	}
	
	@Override
	@Managed
	public SimpleCdlLocking locking() {
		return new SimpleCdlLocking();
	}
	
	@Override
	@Managed
	public AuthorizingServiceRequestEvaluator systemServiceRequestEvaluator() {
		AuthorizingServiceRequestEvaluator bean = new AuthorizingServiceRequestEvaluator();
		bean.setUserSessionProvider(systemUserSessionSupplier());
		bean.setDelegate(commonServiceProcessing.evaluator());
		return bean;
	}
	
	private UserSessionStack userSessionStack() {
		UserSessionStack bean = new UserSessionStack();
		return bean;
	}
	
	@Override
	@Managed
	public StandardUserSessionScoping userSessionScoping() {
		StandardUserSessionScoping bean = new StandardUserSessionScoping();
		bean.setUserSessionStack(userSessionStack());
		bean.setRequestEvaluator(systemServiceRequestEvaluator());
		bean.setDefaultUserSessionSupplier(systemUserSessionSupplier());
		return bean;
	}

	@Override
	@Managed
	public StaticUserSessionHolder systemUserSessionSupplier() {
		StaticUserSessionHolder bean = new StaticUserSessionHolder();
		bean.setUserSession(internalUserSession());
		return bean;
	}
	
	@Managed
	public UserSession internalUserSession() {
		UserSession bean = UserSession.T.create();

		User user = internalUser();
		
		Set<String> effectiveRoles = new HashSet<>();
		effectiveRoles.add("$all");
		effectiveRoles.add("$user-" + user.getName());
		for (Role userRole : user.getRoles())
			effectiveRoles.add(userRole.getName());

		Date now = new Date();

		bean.setSessionId(UUID.randomUUID().toString());
		bean.setType(UserSessionType.internal);
		bean.setCreationInternetAddress("0:0:0:0:0:0:0:1");
		bean.setCreationDate(now);
		bean.setLastAccessedDate(now);
		bean.setUser(user);
		bean.setEffectiveRoles(effectiveRoles);

		return bean;
	}
	
	@Managed
	public User internalUser() {

		User bean = User.T.create();
		bean.setId(internalName);
		bean.setName(internalName);

		for (String internalRole : internalRoles) {
			Role tfInternal = Role.T.create();
			tfInternal.setId(internalRole);
			tfInternal.setName(internalRole);
			bean.getRoles().add(tfInternal);
		}

		return bean;

	}
	
	@Override
	public LockingBasedLeadershipManager leadershipManager() {
		LockingBasedLeadershipManager bean = new LockingBasedLeadershipManager();
		bean.setLocking(locking());
		return bean;
	}
	
	@Override
	@Managed
	public ThreadPoolExecutor threadPool() {
		int threadPoolSize = 50;

		// @formatter:off
		ExtendedThreadPoolExecutor bean = new ExtendedThreadPoolExecutor( //
				threadPoolSize, // corePoolSize
				threadPoolSize, // maxPoolSize
				5, // keepAliveTime
				TimeUnit.MINUTES, //
				new LinkedBlockingQueue<>(), //
				CustomThreadFactory.create().namePrefix("tf.platform-")
		);
		// @formatter:on
		bean.allowCoreThreadTimeOut(true);
		bean.setDescription("Platform Thread-Pool");
		return bean;
	}

	@Override
	@Managed
	public BasicDeployRegistry deployRegistry() {
		BasicDeployRegistry bean = new BasicDeployRegistry();
		return bean;
	}
	
	@Override
	@Managed
	public ProxyingDeployedComponentResolver deployedComponentResolver() {
		ProxyingDeployedComponentResolver bean = new ProxyingDeployedComponentResolver();
		bean.setDeployRegistry(deployRegistry());
		bean.setProcessingInstanceId(instanceId());
		bean.setComponentInterfaceBindings(componentInterfaceBindings());
		return bean;
	}
	
	@Override
	@Managed
	public ComponentInterfaceBindingsRegistry componentInterfaceBindings() {
		ComponentInterfaceBindingsRegistry bean = new ComponentInterfaceBindingsRegistry();
		bean.registerComponentInterfaces(TransitionProcessor.T, tribefire.extension.process.api.TransitionProcessor.class);
		bean.registerComponentInterfaces(ConditionProcessor.T, tribefire.extension.process.api.ConditionProcessor.class);
		return bean;
	}
	
	@Override
	public <D extends Deployable, C extends Deployable> void deploy(D deployable, EntityType<C> componentType, Object expert) {
		ConfigurableDeployedUnit unit = new ConfigurableDeployedUnit();
		unit.put(componentType, expert);
		deployRegistry().register(deployable, unit);
	}
	
	@Managed
	@Override
	public InstanceId instanceId() {
		return InstanceId.of("test-node-0", "test-application");
	}
	
	@Managed
	@Override
	public CartridgeLiveInstances liveInstances() {
		CartridgeLiveInstances bean = new CartridgeLiveInstances();
		bean.setCurrentInstanceId(instanceId());
		bean.acceptCurrentInstance();
		bean.setEnabled(true);
		return bean;
	}
	
	@Managed
	@Override
	public TaskSchedulerImpl taskScheduler() {
		TaskSchedulerImpl bean = new TaskSchedulerImpl();
		bean.setName("test-platform-task-scheduler");
		bean.setExecutor(Executors.newScheduledThreadPool(5));
		return bean;
	}

}
