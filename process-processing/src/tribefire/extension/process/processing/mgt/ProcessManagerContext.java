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
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

import com.braintribe.common.concurrent.TaskScheduler;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.messaging.Message;
import com.braintribe.model.messaging.Queue;
import com.braintribe.model.processing.deployment.api.DeployedComponentResolver;
import com.braintribe.model.processing.lock.api.LockManager;
import com.braintribe.model.processing.lock.api.Locking;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.time.TimeSpan;
import com.braintribe.model.time.TimeUnit;
import com.braintribe.transport.messaging.api.MessageConsumer;
import com.braintribe.transport.messaging.api.MessageProducer;
import com.braintribe.transport.messaging.api.MessagingSession;
import com.braintribe.transport.messaging.api.MessagingSessionProvider;

import tribefire.extension.process.api.model.crtl.HandleProcess;
import tribefire.extension.process.data.model.ProcessItem;
import tribefire.extension.process.processing.oracle.ProcessManagerOracle;

public class ProcessManagerContext extends ProcessTerminationListening {
	// configurable
	public MessagingSessionProvider messagingSessionProvider;
	
	public String messageQueueName = ProcessManagerConstants.DEFAULT_QUEUE_NAME;

	public MessagingSession messagingSession;

	public Queue transitionQueue;

	public MessageProducer messageProducer;

	public MessageConsumer messageConsumer;
	
	public Evaluator<ServiceRequest> evaluator;
	
	public ProcessManagerOracle processManagerOracle = new ProcessManagerOracle();
	
	public DeployedComponentResolver deployedComponentResolver;
	
	public Supplier<List<String>> monitoredAccessSupplier;
	
	public PersistenceGmSessionFactory systemSessionFactory;
	
	public ExecutorService executor;
	
	public TimeSpan monitorInterval = TimeSpan.create(1, TimeUnit.minute);
	
	public TaskScheduler taskScheduler;

	public Locking locking;
	
	public void enqueueProcessContinuation(ProcessItem item, String accessId) {
		enqueueProcessContinuation(item.entityType(), item.getId(), accessId);
	}
	
	public void enqueueProcessContinuation(EntityType<? extends ProcessItem> type, String itemId, String accessId) {
		HandleProcess handleProcess = HandleProcess.T.create();
		handleProcess.setItemId(itemId);
		handleProcess.setItemType(type.getTypeSignature());
		handleProcess.setDomainId(accessId);
		Message message = messagingSession.createMessage();
		message.setBody(handleProcess);
		messageProducer.sendMessage(message);
	}
	
}
