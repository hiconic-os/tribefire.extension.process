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
// ============================================================================
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
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
 * ResumeProcess can be executed on a {@link ProcessItem} with {@link ProcessItem#getActivity() activity} {@link ProcessActivity#waiting waiting}
 * in order to resume the process from its waiting state into the {@link ProcessItem#getActivity() activity} {@link ProcessActivity#processing processing}.
 * 
 * <p>
 * This request should be used by a <b>decoupled interaction</b> (e.g. some worker or notification principle) to hand back the process handling
 * to the <b>process manager</b>
 * 
 * @see {@link ResumeProcessToState} in order to specify the next state in the routing
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

public interface ResumeProcess extends LockedProcessRequest {
	EntityType<ResumeProcess> T = EntityTypes.T(ResumeProcess.class);
	
	@Override
	EvalContext<Neutral> eval(Evaluator<ServiceRequest> evaluator);
}
