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
package tribefire.extension.process.initializer.wire.space;

import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.cortex.initializer.support.wire.space.AbstractInitializerSpace;
import tribefire.extension.process.initializer.wire.contract.ProcessInitializerContract;

@Managed
public class ProcessInitializerSpace extends AbstractInitializerSpace implements ProcessInitializerContract {

	@Import
	private ProcessModelsSpace processModels;

	/* To ensure beans are initialized simply reference them here (i.e. invoke their defining methods).  */
	@Override
	public void initialize() {
		processModels.configuredApiModel();
		processModels.configuredDataModel();
	}
}
