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
package tribefire.extension.process.processing.experts;

import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.generic.reflection.EntityType;

import tribefire.extension.process.api.TransitionProcessor;
import tribefire.extension.process.test.model.TestProcess;

public abstract class TestTransitionProcessor<T extends TestProcess> extends TestProcessor implements TransitionProcessor<T> {

	private tribefire.extension.process.model.deployment.TransitionProcessor deployable;
	
	protected String getIdentifier() {
		return getClass().getSimpleName();
	}
	
	public tribefire.extension.process.model.deployment.TransitionProcessor getDeployable() {
		if (deployable == null) {
			deployable = tribefire.extension.process.model.deployment.TransitionProcessor.T.create();
			String identifier = getIdentifier();
			deployable.setExternalId("tp." + identifier);
			deployable.setName(identifier);
		}
		return deployable;
	}
	
	@Override
	public EntityType<? extends Deployable> getComponentType() {
		return tribefire.extension.process.model.deployment.TransitionProcessor.T;
	}
}
