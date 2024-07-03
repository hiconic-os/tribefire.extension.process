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