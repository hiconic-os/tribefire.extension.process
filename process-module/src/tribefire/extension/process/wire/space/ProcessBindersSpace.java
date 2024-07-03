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
package tribefire.extension.process.wire.space;

import com.braintribe.model.processing.deployment.api.ComponentBinder;
import com.braintribe.model.processing.deployment.api.ComponentBinders;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.extension.process.model.deployment.ConditionProcessor;
import tribefire.extension.process.model.deployment.TransitionProcessor;
import tribefire.extension.process.module.wire.contract.ProcessBindersContract;

@Managed
public class ProcessBindersSpace implements ProcessBindersContract {

	@Override
	@Managed
	public ComponentBinder<TransitionProcessor, tribefire.extension.process.api.TransitionProcessor<?>> transitionProcessor() {
		return ComponentBinders.binder(TransitionProcessor.T, tribefire.extension.process.api.TransitionProcessor.class);
	}
	
	@Override
	@Managed
	public ComponentBinder<ConditionProcessor, tribefire.extension.process.api.ConditionProcessor<?>> conditionProcessor() {
		return ComponentBinders.binder(ConditionProcessor.T, tribefire.extension.process.api.ConditionProcessor.class);
	}
	
}
