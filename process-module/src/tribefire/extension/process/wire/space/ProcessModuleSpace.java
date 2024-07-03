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

import com.braintribe.model.processing.deployment.api.binding.DenotationBindingBuilder;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.extension.process.model.deployment.ProcessManager;
import tribefire.extension.process.module.wire.contract.ProcessBindersContract;
import tribefire.module.api.WireContractBindingBuilder;
import tribefire.module.wire.contract.TribefireModuleContract;
import tribefire.module.wire.contract.TribefireWebPlatformContract;

@Managed
public class ProcessModuleSpace implements TribefireModuleContract {

	@Import
	private TribefireWebPlatformContract tfPlatform;
	
	@Import
	private ProcessDeployablesSpace deployables;
	
	@Import
	private ProcessBindersSpace processBinders;
	
	@Override
	public void bindWireContracts(WireContractBindingBuilder bindings) {
		bindings.bind(ProcessBindersContract.class, processBinders);
	}
	
	@Override
	public void bindDeployables(DenotationBindingBuilder bindings) {
		//@formatter:off
		bindings.bind(ProcessManager.T)
		.component(tfPlatform.binders().accessRequestProcessor()).expertFactory(deployables::processManager)
		.component(tfPlatform.binders().worker()).expertFactory(deployables::processManager);
		//@formatter:on
	}
}
