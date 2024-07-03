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

import tribefire.extension.process.api.ConditionProcessor;
import tribefire.extension.process.test.model.TestProcess;

public abstract class TestConditionProcessor<T extends TestProcess> extends TestProcessor implements ConditionProcessor<T> {

	private tribefire.extension.process.model.deployment.ConditionProcessor deployable;
	
	public tribefire.extension.process.model.deployment.ConditionProcessor getDeployable() {
		if (deployable == null) {
			deployable = tribefire.extension.process.model.deployment.ConditionProcessor.T.create();
			deployable.setExternalId("cp." + getClass().getSimpleName());
			deployable.setName(getClass().getSimpleName());
		}
		return deployable;
	}
	
	@Override
	public EntityType<? extends Deployable> getComponentType() {
		return tribefire.extension.process.model.deployment.ConditionProcessor.T;
	}
}
