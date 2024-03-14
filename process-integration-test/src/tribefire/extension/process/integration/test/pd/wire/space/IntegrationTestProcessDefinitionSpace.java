// ============================================================================
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package tribefire.extension.process.integration.test.pd.wire.space;

import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.extension.process.integration.test.pd.wire.contract.IntegrationTestProcessDefinitionContract;
import tribefire.extension.process.processing.base.pd.wire.contract.TestProcessDefinitionContract;
import tribefire.extension.process.processing.base.pd.wire.contract.TestProcessRequirementsContract;

@Managed
public class IntegrationTestProcessDefinitionSpace implements IntegrationTestProcessDefinitionContract {
	@Import
	private TestProcessDefinitionContract processes;
	
	@Import
	private TestProcessRequirementsContract processors;
	
	@Override
	public TestProcessDefinitionContract processes() {
		return processes;
	}

	@Override
	public TestProcessRequirementsContract processors() {
		return processors;
	}

}