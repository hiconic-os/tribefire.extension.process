// ============================================================================
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ============================================================================
package tribefire.extension.process.processing.base.wire.space;

import java.util.HashMap;

import com.braintribe.gm.service.access.api.AccessProcessingConfiguration;
import com.braintribe.gm.service.access.wire.common.contract.AccessProcessingConfigurationContract;
import com.braintribe.gm.service.access.wire.common.contract.CommonAccessProcessingContract;
import com.braintribe.gm.service.wire.common.contract.CommonServiceProcessingContract;
import com.braintribe.gm.service.wire.common.contract.ServiceProcessingConfigurationContract;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.messaging.Topic;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.meta.configuration.ConfigurationModels;
import com.braintribe.model.processing.meta.editor.BasicModelMetaDataEditor;
import com.braintribe.model.processing.mqrpc.server.GmMqRpcServer;
import com.braintribe.model.processing.securityservice.commons.service.InMemorySecurityServiceProcessor;
import com.braintribe.model.processing.service.api.aspect.EndpointExposureAspect;
import com.braintribe.model.processing.service.common.ConfigurableDispatchingServiceProcessor;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.service.api.MulticastRequest;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.context.WireContextConfiguration;

import tribefire.extension.process._ProcessDataModel_;
import tribefire.extension.process._ProcessTestModel_;
import tribefire.extension.process.api.model.ProcessManagerRequest;
import tribefire.extension.process.model.deployment.meta.ManageProcessItemWith;
import tribefire.extension.process.processing.base.ProcessProcessingTestCommons;
import tribefire.extension.process.processing.base.pd.wire.contract.TestProcessDefinitionContract;
import tribefire.extension.process.processing.base.wire.contract.ProcessProcessingTestContract;
import tribefire.extension.process.processing.base.wire.contract.TestPlatformContract;
import tribefire.extension.process.test.model.AmountTestProcess;
import tribefire.extension.process.test.model.FailingByExceptionTestProcess;
import tribefire.extension.process.test.model.FailingByReasonTestProcess;
import tribefire.extension.process.test.model.ResumableTestProcess;
import tribefire.extension.process.test.model.SelfRoutingTestProcess;
import tribefire.extension.process.test.model.TestProcess;
import tribefire.platform.impl.multicast.MulticastProcessor;

@Managed
public class ProcessProcessingTestSpace implements ProcessProcessingTestContract {

	@Import
	private AccessProcessingConfigurationContract accessProcessingConfiguration;

	@Import
	private CommonAccessProcessingContract commonAccessProcessing;

	@Import
	private ServiceProcessingConfigurationContract serviceProcessingConfiguration;

	@Import
	private CommonServiceProcessingContract commonServiceProcessing;

	@Import
	private TestProcessDefinitionContract process;
	
	@Import
	private ProcessManagerSpace processManager;
	
	@Import
	private TestPlatformContract testPlatform;
	
	@Override
	public void onLoaded(WireContextConfiguration configuration) {
		accessProcessingConfiguration.registerAccessConfigurer(this::configureAccesses);
		serviceProcessingConfiguration.registerServiceConfigurer(this::configureServices);
		serviceProcessingConfiguration.registerSecurityConfigurer(this::configureSecurity);
	}

	private void configureAccesses(AccessProcessingConfiguration configuration) {
		configuration.registerAccess(ProcessProcessingTestCommons.ACCESS_ID_PROCESSES, configuredProcessTestModel());
		configuration.registerAccessRequestProcessor(ProcessManagerRequest.T, processManager.processManager());
	}
	
	@Managed
	private GmMetaModel configuredProcessTestModel() {
		GmMetaModel model = ConfigurationModels.create(_ProcessDataModel_.reflection.groupId(), "configured-process-test-model")
				.addDependency(_ProcessTestModel_.reflection)
				.get();
		
		BasicModelMetaDataEditor editor = new BasicModelMetaDataEditor(model);
		
		ManageProcessItemWith manageProcessItemWith = ManageProcessItemWith.T.create();
		manageProcessItemWith.setProcessDefinition(process.simpleTaggingProcess());
		
		editor.onEntityType(TestProcess.T).addMetaData(manageProcessItemWith);
		
		ManageProcessItemWith manageSelfRoutingProcessItemWith = ManageProcessItemWith.T.create();
		manageSelfRoutingProcessItemWith.setProcessDefinition(process.selfRoutingProcess());
		
		editor.onEntityType(SelfRoutingTestProcess.T).addMetaData(manageSelfRoutingProcessItemWith);
		
		ManageProcessItemWith manageResumableProcessItemWith = ManageProcessItemWith.T.create();
		manageResumableProcessItemWith.setProcessDefinition(process.resumableProcess());
		
		editor.onEntityType(ResumableTestProcess.T).addMetaData(manageResumableProcessItemWith);

		ManageProcessItemWith manageFailingByExceptionProcessItemWith = ManageProcessItemWith.T.create();
		manageFailingByExceptionProcessItemWith.setProcessDefinition(process.failingByProcessorExceptionProcess());
		
		editor.onEntityType(FailingByExceptionTestProcess.T).addMetaData(manageFailingByExceptionProcessItemWith);
		
		ManageProcessItemWith manageFailingByReasonProcessItemWith = ManageProcessItemWith.T.create();
		manageFailingByReasonProcessItemWith.setProcessDefinition(process.failingByProcessorReasonProcess());
		
		editor.onEntityType(FailingByReasonTestProcess.T).addMetaData(manageFailingByReasonProcessItemWith);
		
		ManageProcessItemWith manageAmountProcessItemWith = ManageProcessItemWith.T.create();
		manageAmountProcessItemWith.setProcessDefinition(process.amountProcess());
		
		editor.onEntityType(AmountTestProcess.T).addMetaData(manageAmountProcessItemWith);
		return model;
	}

	private void configureServices(ConfigurableDispatchingServiceProcessor bean) {
		bean.removeInterceptor(CommonServiceProcessingContract.AUTH_INTERCEPTOR_ID);
		// TODO register or remove interceptors and register tested service processors
		/*
			bean.registerInterceptor("someInterceptor");
			bean.removeInterceptor("someInterceptor");
			bean.register(SomeServiceRequest.T, someServiceProcessor());
		*/
		
		bean.register(MulticastRequest.T, multicastProcessor());
	}
	
	@Managed
	private MulticastProcessor multicastProcessor() {
		MulticastProcessor bean = new MulticastProcessor();
		bean.setMessagingSessionProvider(testPlatform.messagingSessionProvider());
		bean.setRequestTopicName("service-requests");
		bean.setResponseTopicName("service-responses");
		bean.setSenderId(testPlatform.instanceId());
		bean.setMetaDataProvider(HashMap::new);
		bean.setLiveInstances(testPlatform.liveInstances());
		
		multicastServer().start();
		return bean;
	}
	
	@Managed
	public GmMqRpcServer multicastServer() {
		GmMqRpcServer bean = new GmMqRpcServer();
		bean.setRequestEvaluator(testPlatform.systemServiceRequestEvaluator());
		bean.setMessagingSessionProvider(testPlatform.messagingSessionProvider());
		bean.setRequestDestinationName("service-requests");
		bean.setRequestDestinationType(Topic.T);
		bean.setConsumerId(testPlatform.instanceId());
		bean.setExecutor(testPlatform.threadPool());
		bean.setTrusted(false);
		bean.setEndpointExposure(EndpointExposureAspect.MULTICAST);
		return bean;
	}


	private void configureSecurity(InMemorySecurityServiceProcessor bean) {
		// TODO add users IF your requests are to be authorized while testing
		// (make sure the 'auth' interceptor is NOT REMOVED in that case in the 'configureServices' method)
		/* 
			User someUser = User.T.create();
			user.setId("someUserId");
			user.setName("someUserName");
			user.setPassword("somePassword");

			bean.addUser(someUser);
		*/
	}

	@Override
	public Evaluator<ServiceRequest> evaluator() {
		return commonServiceProcessing.evaluator();
	}
	
	

	@Override
	public PersistenceGmSessionFactory sessionFactory() {
		return commonAccessProcessing.sessionFactory();
	}

}