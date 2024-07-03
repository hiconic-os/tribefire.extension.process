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

import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.api.result.Neutral;

import tribefire.extension.process.api.model.ProcessManagerRequest;

/**
 * ReviveProcesses will detect unattended processes and overdue processes in the persistence and will revive each of them by enqueueing {@link HandleProcess} requests
 * into the messaging of the <b>process manager</b> which will then take further responsibility of the handling.
 * 
 * @author Dirk Scheffler
 *
 */
public interface ReviveProcesses extends ProcessManagerRequest {
	EntityType<ReviveProcesses> T = EntityTypes.T(ReviveProcesses.class);
	
	@Override
	EvalContext<Neutral> eval(Evaluator<ServiceRequest> evaluator);
}
