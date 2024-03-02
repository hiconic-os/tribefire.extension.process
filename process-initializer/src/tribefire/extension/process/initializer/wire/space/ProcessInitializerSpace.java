// ============================================================================
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
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
