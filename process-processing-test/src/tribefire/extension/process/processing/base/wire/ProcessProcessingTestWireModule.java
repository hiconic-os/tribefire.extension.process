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
// ============================================================================
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package tribefire.extension.process.processing.base.wire;

import static com.braintribe.utils.lcd.CollectionTools2.asList;

import java.util.List;

import com.braintribe.gm.service.access.wire.common.CommonAccessProcessingWireModule;
import com.braintribe.gm.service.wire.common.CommonServiceProcessingWireModule;
import com.braintribe.wire.api.context.WireContextBuilder;
import com.braintribe.wire.api.module.WireModule;
import com.braintribe.wire.api.module.WireTerminalModule;

import tribefire.extension.process.processing.base.pd.wire.TestProcessDefinitionWireModule;
import tribefire.extension.process.processing.base.pd.wire.contract.TestProcessRequirementsContract;
import tribefire.extension.process.processing.base.wire.contract.ProcessProcessingTestConfigurationContract;
import tribefire.extension.process.processing.base.wire.contract.ProcessProcessingTestContract;
import tribefire.extension.process.processing.base.wire.space.TestProcessRequirementsSpace;

public enum ProcessProcessingTestWireModule implements WireTerminalModule<ProcessProcessingTestContract> {
	INSTANCE(true), MONITORED_INSTANCE(false);
	
	private boolean suppressRevivalMonitor;

	private ProcessProcessingTestWireModule(boolean suppressRevivalMonitor) {
		this.suppressRevivalMonitor = suppressRevivalMonitor;
		
	}
	
	@Override
	public void configureContext(WireContextBuilder<?> contextBuilder) {
		WireTerminalModule.super.configureContext(contextBuilder);
		contextBuilder.bindContract(ProcessProcessingTestConfigurationContract.class, () -> suppressRevivalMonitor);
		contextBuilder.bindContract(TestProcessRequirementsContract.class, TestProcessRequirementsSpace.class);
	}

	@Override
	public List<WireModule> dependencies() {
		return asList(CommonServiceProcessingWireModule.INSTANCE, CommonAccessProcessingWireModule.INSTANCE, TestProcessDefinitionWireModule.INSTANCE);
	}

}
