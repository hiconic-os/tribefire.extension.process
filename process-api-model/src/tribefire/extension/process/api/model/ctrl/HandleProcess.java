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
package tribefire.extension.process.api.model.ctrl;

import com.braintribe.gm.model.reason.essential.InvalidArgument;
import com.braintribe.model.generic.annotation.meta.UnsatisfiedBy;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.api.result.Neutral;

import tribefire.extension.process.api.model.LockedProcessRequest;
import tribefire.extension.process.data.model.ProcessItem;
import tribefire.extension.process.data.model.state.ProcessActivity;
import tribefire.extension.process.reason.model.CouldNotAcquireProcessLock;
import tribefire.extension.process.reason.model.EdgeNotFound;
import tribefire.extension.process.reason.model.NodeNotFound;
import tribefire.extension.process.reason.model.ProcessDefinitionNotFound;
import tribefire.extension.process.reason.model.ProcessNotFound;
import tribefire.extension.process.reason.model.UnexpectedProcessActivity;
import tribefire.extension.process.reason.model.UnexpectedProcessState;

/**
 * Puts or carries on attention of the <b>process manager</b> to the identified {@link ProcessItem}. It is used in the following typical situations:
 * 
 * <ul>
 * 	<li>Continuous asynchronous, interruptible and distributed processing carried on via a messaging queue
 *  <li>Automatic or explicit process revival of unattended processes (e.g. after a shutdown/restart)
 * </ul>
 * 
 * <p>
 * Normally the <b>process manager</b> executes a fine grained handling operation on the {@link ProcessItem} keeping track of that with {@link ProcessItem#getTransitionPhase()}
 * and then enqueues an instance of {@link HandleProcess} into a message queue from which any <b>process manager</b> instance in a cluster can take on the handling.
 * 
 * <p>
 * A process that is in {@link ProcessItem#getActivity() activity} {@link ProcessActivity#processing processing} and is continuously handled by self enqueued {@link HandleProcess}
 * messages is considered attended by the <b>process manager</b> which is detectable by actuality of {@link ProcessItem#getLastTransit()}. If {@link ProcessItem#getLastTransit()}
 * is stale the process is considered unattendend and will be detected by the revival monitor.  
 *  
 * @author Dirk Scheffler
 *
 */

@UnsatisfiedBy(InvalidArgument.class)
@UnsatisfiedBy(ProcessDefinitionNotFound.class)
@UnsatisfiedBy(CouldNotAcquireProcessLock.class)
@UnsatisfiedBy(ProcessNotFound.class)
@UnsatisfiedBy(NodeNotFound.class)
@UnsatisfiedBy(EdgeNotFound.class)
@UnsatisfiedBy(UnexpectedProcessActivity.class)
@UnsatisfiedBy(UnexpectedProcessState.class)

public interface HandleProcess extends LockedProcessRequest {
	EntityType<HandleProcess> T = EntityTypes.T(HandleProcess.class);
	
	@Override
	EvalContext<Neutral> eval(Evaluator<ServiceRequest> evaluator);
}
