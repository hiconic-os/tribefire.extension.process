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

import tribefire.extension.process.api.model.ctrl.HandleProcess;
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
