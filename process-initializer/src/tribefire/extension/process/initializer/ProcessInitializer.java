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
package tribefire.extension.process.initializer;

import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.wire.api.module.WireTerminalModule;

import tribefire.cortex.initializer.support.api.WiredInitializerContext;
import tribefire.cortex.initializer.support.impl.AbstractInitializer;

import tribefire.extension.process.initializer.wire.ProcessInitializerWireModule;
import tribefire.extension.process.initializer.wire.contract.ProcessInitializerContract;

public class ProcessInitializer extends AbstractInitializer<ProcessInitializerContract> {

	@Override
	public WireTerminalModule<ProcessInitializerContract> getInitializerWireModule() {
		return ProcessInitializerWireModule.INSTANCE;
	}

	@Override
	public void initialize(PersistenceInitializationContext context, WiredInitializerContext<ProcessInitializerContract> initializerContext,
			ProcessInitializerContract initializerContract) {

		
		initializerContract.initialize();

	}

}