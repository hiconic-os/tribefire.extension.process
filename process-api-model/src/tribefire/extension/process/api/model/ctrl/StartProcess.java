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
import tribefire.extension.process.reason.model.CouldNotAcquireProcessLock;
import tribefire.extension.process.reason.model.ProcessDefinitionNotFound;
import tribefire.extension.process.reason.model.ProcessNotFound;
import tribefire.extension.process.reason.model.UnexpectedProcessActivity;
import tribefire.extension.process.reason.model.UnexpectedProcessState;

/**
 * Starts the identified {@link ProcessItem} and requires it to have the following control property values:
 * 
 * <ul>
 * 		<li>{@link ProcessItem#getState()} = null
 * 		<li>{@link ProcessItem#getActivity()} = null
 * </ul>
 * 
 * The <b>process definition</b> must be given in a way that the process can automatically find its
 * way to the first state. If it cannot please use the {@link StartProcessToState} request instead.
 * 
 * @author Dirk Scheffler
 *
 */

@UnsatisfiedBy(InvalidArgument.class)
@UnsatisfiedBy(ProcessDefinitionNotFound.class)
@UnsatisfiedBy(CouldNotAcquireProcessLock.class)
@UnsatisfiedBy(ProcessNotFound.class)
@UnsatisfiedBy(UnexpectedProcessActivity.class)
@UnsatisfiedBy(UnexpectedProcessState.class)

public interface StartProcess extends LockedProcessRequest {
	EntityType<StartProcess> T = EntityTypes.T(StartProcess.class);
	
	@Override
	EvalContext<Neutral> eval(Evaluator<ServiceRequest> evaluator);
}
