// ============================================================================
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
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