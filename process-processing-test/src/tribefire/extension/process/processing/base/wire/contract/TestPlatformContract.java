// ============================================================================
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package tribefire.extension.process.processing.base.wire.contract;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Supplier;

import com.braintribe.common.concurrent.TaskScheduler;
import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.deployment.api.ComponentInterfaceBindings;
import com.braintribe.model.processing.deployment.api.DeployRegistry;
import com.braintribe.model.processing.deployment.api.DeployedComponentResolver;
import com.braintribe.model.processing.lock.api.Locking;
import com.braintribe.model.processing.securityservice.commons.scope.StandardUserSessionScoping;
import com.braintribe.model.processing.worker.api.WorkerManager;
import com.braintribe.model.service.api.InstanceId;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.usersession.UserSession;
import com.braintribe.transport.messaging.api.MessagingSessionProvider;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.cortex.leadership.api.LeadershipManager;
import tribefire.platform.impl.topology.CartridgeLiveInstances;

public interface TestPlatformContract extends WireSpace {

	MessagingSessionProvider messagingSessionProvider();

	WorkerManager workerManager();

	Evaluator<ServiceRequest> systemServiceRequestEvaluator();

	StandardUserSessionScoping userSessionScoping();

	Supplier<UserSession> systemUserSessionSupplier();

	LeadershipManager leadershipManager();

	ThreadPoolExecutor threadPool();
	
	DeployRegistry deployRegistry();

	DeployedComponentResolver deployedComponentResolver();

	ComponentInterfaceBindings componentInterfaceBindings();

	Locking locking();
	
	void startWorkerManager();
	
	InstanceId instanceId();

	<D extends Deployable, C extends Deployable> void deploy(D deployable, EntityType<C> componentType, Object expert);

	CartridgeLiveInstances liveInstances();
	
	TaskScheduler taskScheduler();
}