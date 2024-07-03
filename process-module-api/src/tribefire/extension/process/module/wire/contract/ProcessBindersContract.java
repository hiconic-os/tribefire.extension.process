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
package tribefire.extension.process.module.wire.contract;

import com.braintribe.model.processing.deployment.api.ComponentBinder;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.extension.process.api.ConditionProcessor;
import tribefire.extension.process.api.TransitionProcessor;

/**
 * @author peter.gazdik
 */
public interface ProcessBindersContract extends WireSpace {

	ComponentBinder<tribefire.extension.process.model.deployment.TransitionProcessor, TransitionProcessor<?>> transitionProcessor();
	
	ComponentBinder<tribefire.extension.process.model.deployment.ConditionProcessor, ConditionProcessor<?>> conditionProcessor();
	
}
