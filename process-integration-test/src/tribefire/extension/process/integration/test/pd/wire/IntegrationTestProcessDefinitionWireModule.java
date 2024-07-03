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
package tribefire.extension.process.integration.test.pd.wire;

import java.util.List;

import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.wire.api.context.WireContextBuilder;
import com.braintribe.wire.api.module.WireModule;
import com.braintribe.wire.api.module.WireTerminalModule;
import com.braintribe.wire.api.util.Lists;

import tribefire.extension.process.integration.test.pd.wire.contract.IntegrationTestProcessDefinitionContract;
import tribefire.extension.process.integration.test.pd.wire.contract.IntegrationTestProcessDefinitionRequirementsContract;
import tribefire.extension.process.processing.base.pd.wire.TestProcessDefinitionWireModule;

public class IntegrationTestProcessDefinitionWireModule implements WireTerminalModule<IntegrationTestProcessDefinitionContract> {
	
	private PersistenceGmSession session;

	public IntegrationTestProcessDefinitionWireModule(PersistenceGmSession session) {
		super();
		this.session = session;
	}
	
	@Override
	public List<WireModule> dependencies() {
		return Lists.list(TestProcessDefinitionWireModule.INSTANCE);
	}
	
	@Override
	public void configureContext(WireContextBuilder<?> contextBuilder) {
		WireTerminalModule.super.configureContext(contextBuilder);
		contextBuilder.bindContract(IntegrationTestProcessDefinitionRequirementsContract.class, () -> session);
	}

}
