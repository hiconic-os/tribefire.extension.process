// ============================================================================
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package tribefire.extension.process.processing.mgt;

import java.util.List;
import java.util.concurrent.Future;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.LifecycleAware;
import com.braintribe.cfg.Required;
import com.braintribe.common.concurrent.TaskScheduler;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.messaging.Message;
import com.braintribe.model.processing.accessrequest.api.AbstractDispatchingAccessRequestProcessor;
import com.braintribe.model.processing.accessrequest.api.DispatchConfiguration;
import com.braintribe.model.processing.deployment.api.DeployedComponentResolver;
import com.braintribe.model.processing.lock.api.Locking;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.processing.worker.api.Worker;
import com.braintribe.model.processing.worker.api.WorkerContext;
import com.braintribe.model.processing.worker.api.WorkerException;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.api.result.Neutral;
import com.braintribe.transport.messaging.api.MessagingException;
import com.braintribe.transport.messaging.api.MessagingSession;
import com.braintribe.transport.messaging.api.MessagingSessionProvider;

import tribefire.extension.process.api.model.ProcessManagerRequest;
import tribefire.extension.process.api.model.ctrl.ClearProcessLog;
import tribefire.extension.process.api.model.ctrl.ClearProcessLogs;
import tribefire.extension.process.api.model.ctrl.GetProcessLog;
import tribefire.extension.process.api.model.ctrl.HandleProcess;
import tribefire.extension.process.api.model.ctrl.NotifyProcessActivity;
import tribefire.extension.process.api.model.ctrl.RecoverProcess;
import tribefire.extension.process.api.model.ctrl.ResumeProcess;
import tribefire.extension.process.api.model.ctrl.ReviveProcesses;
import tribefire.extension.process.api.model.ctrl.StartProcess;
import tribefire.extension.process.api.model.ctrl.WaitForProcess;
import tribefire.extension.process.api.model.ctrl.WaitForProcesses;
import tribefire.extension.process.processing.mgt.processor.ClearProcessLogProcessor;
import tribefire.extension.process.processing.mgt.processor.ClearProcessLogsProcessor;
import tribefire.extension.process.processing.mgt.processor.GetProcessLogProcessor;
import tribefire.extension.process.processing.mgt.processor.HandleProcessProcessor;
import tribefire.extension.process.processing.mgt.processor.NotifyProcessActivityProcessor;
import tribefire.extension.process.processing.mgt.processor.RecoverProcessProcessor;
import tribefire.extension.process.processing.mgt.processor.ResumeProcessProcessor;
import tribefire.extension.process.processing.mgt.processor.ReviveProcessesProcessor;
import tribefire.extension.process.processing.mgt.processor.StartProcessProcessor;
import tribefire.extension.process.processing.mgt.processor.WaitForProcessProcessor;
import tribefire.extension.process.processing.mgt.processor.WaitForProcessesProcessor;

public class ProcessManager extends AbstractDispatchingAccessRequestProcessor<ProcessManagerRequest, Object> implements Worker, LifecycleAware, ProcessManagerConstants  {
	
	private static final Logger logger = Logger.getLogger(ProcessManager.class);
	
	private ProcessManagerContext processManagerContext = new ProcessManagerContext();

	private Future<?> monitorFuture;

	/**
	 * Optionally configured message queue name. Default is {@link #DEFAULT_QUEUE_NAME} 
	 */
	@Configurable
	public void setMessageQueueName(String messageQueueName) {
		processManagerContext.messageQueueName = messageQueueName;
	}
	
	@Required
	public void setTaskScheduler(TaskScheduler taskScheduler) {
		processManagerContext.taskScheduler = taskScheduler;
	}
	
	@Required
	public void setDeployedComponentResolver(DeployedComponentResolver deployedComponentResolver) {
		processManagerContext.deployedComponentResolver = deployedComponentResolver;
	}
	
	@Required
	public void setMessagingSessionProvider(MessagingSessionProvider messagingSessionProvider) {
		processManagerContext.messagingSessionProvider = messagingSessionProvider;
	}
	
	@Required
	public void setLocking(Locking locking) {
		processManagerContext.locking = locking;
	}
	
	@Required
	public void setEvaluator(Evaluator<ServiceRequest> evaluator) {
		processManagerContext.evaluator = evaluator;
	}
	
	@Required
	public void setMonitoredAccessIdsSupplier(Supplier<List<String>> monitoredAccessSupplier) {
		processManagerContext.monitoredAccessSupplier = monitoredAccessSupplier;
	}
	
	@Required
	public void setSystemSessionFactory(PersistenceGmSessionFactory systemSessionFactory) {
		processManagerContext.systemSessionFactory = systemSessionFactory;
	}
	
	@Override
	public void postConstruct() {
		MessagingSession messagingSession = processManagerContext.messagingSessionProvider.provideMessagingSession();
		processManagerContext.messagingSession = messagingSession;
		processManagerContext.transitionQueue = messagingSession.createQueue(processManagerContext.messageQueueName);
		processManagerContext.messageProducer = messagingSession.createMessageProducer(processManagerContext.transitionQueue);
		processManagerContext.messageConsumer = messagingSession.createMessageConsumer(processManagerContext.transitionQueue);
		processManagerContext.messageConsumer.setMessageListener(this::onMessage);
	}
	
	@Override
	public void preDestroy() {
		// first stop message listening
		processManagerContext.messageConsumer.close();

		// wait for completion of current requests
		// TODO: find out how such waiting can be accomplished and if it is necessary
		
		// close other messaging related resources
		processManagerContext.messageProducer.close();
		processManagerContext.messagingSession.close();
	}
	
	private void onMessage(Message message) throws MessagingException {
		
		Object body = message.getBody();
		
		if (body instanceof HandleProcess) {
			HandleProcess handleProcess = (HandleProcess) body;
			
			try {
				Maybe<Neutral> reasoned = handleProcess.eval(processManagerContext.evaluator).getReasoned();
				
				if (reasoned.isUnsatisfied()) {
					logger.error("Error while handling process item from message queue: " + reasoned.whyUnsatisfied().stringify());
				}
				
			} catch (Exception e) {
				logger.error("Exception while handling process item via message queue: " + handleProcess, e);
			}
		}
		else {
			logger.error("Received unexpected message from message queue: " + body);
		}
	}
	
	@Override
	protected void configureDispatching(DispatchConfiguration dispatching) {
		ProcessManagerContextualizedDispatchConfigurer configurer = new ProcessManagerContextualizedDispatchConfigurer(dispatching, () -> processManagerContext);
		// flow management
		configurer.register(StartProcess.T, StartProcessProcessor::new);
		configurer.register(ResumeProcess.T, ResumeProcessProcessor::new);
		configurer.register(HandleProcess.T, HandleProcessProcessor::new);
		configurer.register(RecoverProcess.T, RecoverProcessProcessor::new);
		
		// notification
		configurer.register(NotifyProcessActivity.T, NotifyProcessActivityProcessor::new);
		configurer.register(WaitForProcess.T, WaitForProcessProcessor::new);
		configurer.register(WaitForProcesses.T, WaitForProcessesProcessor::new);
		
		// other management
		configurer.register(ReviveProcesses.T, ReviveProcessesProcessor::new);
		configurer.register(GetProcessLog.T, GetProcessLogProcessor::new);
		configurer.register(ClearProcessLog.T, ClearProcessLogProcessor::new);
		configurer.register(ClearProcessLogs.T, ClearProcessLogsProcessor::new);
	}

	@Override
	public void start(WorkerContext workerContext) throws WorkerException {
		if (processManagerContext.monitorInterval != null)
			monitorFuture = workerContext.submit(new ProcessRevivalWorker(processManagerContext));
	}

	@Override
	public void stop(WorkerContext workerContext) throws WorkerException {
		if (monitorFuture != null)
			monitorFuture.cancel(true);
	}
}