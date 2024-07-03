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
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

import tribefire.extension.process.api.model.LockedProcessRequest;
import tribefire.extension.process.data.model.ProcessItem;
import tribefire.extension.process.data.model.state.ProcessActivity;
import tribefire.extension.process.data.model.state.ProcessControl;
import tribefire.extension.process.reason.model.CouldNotAcquireProcessLock;
import tribefire.extension.process.reason.model.NodeNotFound;
import tribefire.extension.process.reason.model.ProcessDefinitionNotFound;
import tribefire.extension.process.reason.model.ProcessNotFound;
import tribefire.extension.process.reason.model.UnexpectedProcessActivity;
import tribefire.extension.process.reason.model.UnexpectedProcessState;

/**
 * RecoverProcess can be executed on a {@link ProcessItem} with {@link ProcessItem#getActivity() activity}  {@link ProcessActivity#halted halted}
 * in order to resolve the problem of such a halt and get that process back into a unproblematic {@link ProcessItem#getActivity() activity}.
 * 
 * <p>
 * Using the properties of {@link ProcessControl} the request can influence the resolving of the problematic state by setting
 * the process into any fine grained state after thinking about idempotency based on the knowledge about process specifics.
 * 
 * <p>
 * The automatic resolution leads to a state where a failed <b>transition processor</b> that caused a halt would be executed again. Using
 * this automatic feature also involves thought about the idempotency aspects.
 * 
 * @author Dirk Scheffler
 *
 */

@UnsatisfiedBy(InvalidArgument.class)
@UnsatisfiedBy(ProcessDefinitionNotFound.class)
@UnsatisfiedBy(CouldNotAcquireProcessLock.class)
@UnsatisfiedBy(ProcessNotFound.class)
@UnsatisfiedBy(NodeNotFound.class)
@UnsatisfiedBy(UnexpectedProcessActivity.class)
@UnsatisfiedBy(UnexpectedProcessState.class)

public interface RecoverProcess extends LockedProcessRequest, ProcessControl {
	EntityType<RecoverProcess> T = EntityTypes.T(RecoverProcess.class);
}
