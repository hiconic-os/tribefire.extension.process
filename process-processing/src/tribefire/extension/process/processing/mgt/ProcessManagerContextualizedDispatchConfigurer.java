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
package tribefire.extension.process.processing.mgt;

import java.util.function.Supplier;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.accessrequest.api.AccessRequestContext;
import com.braintribe.model.processing.accessrequest.api.DispatchConfiguration;

import tribefire.extension.process.api.model.ProcessManagerRequest;
import tribefire.extension.process.api.model.ProcessRequest;
import tribefire.extension.process.processing.mgt.processor.ProcessManagerRequestProcessor;

public class ProcessManagerContextualizedDispatchConfigurer {
	private DispatchConfiguration dispatching;
	private Supplier<ProcessManagerContext> contextSupplier;

	public ProcessManagerContextualizedDispatchConfigurer(DispatchConfiguration dispatching, Supplier<ProcessManagerContext> contextSupplier) {
		super();
		this.dispatching = dispatching;
		this.contextSupplier = contextSupplier;
	}
	
	public <S extends ProcessManagerRequest> void register(EntityType<S> type, Supplier<ProcessManagerRequestProcessor<S, ?>> processorSupplier) {
		dispatching.registerStatefulWithContext(type, c -> newInstance(c, processorSupplier));
	}
	
	private <S extends ProcessManagerRequest> ProcessManagerRequestProcessor<S, ?> newInstance(AccessRequestContext<S> requestContext, Supplier<ProcessManagerRequestProcessor<S, ?>> processorSupplier) {
		ProcessManagerRequestProcessor<S, ?> processor = processorSupplier.get();
		processor.initProcessManagerContext(contextSupplier.get());
		return processor;
	}
}
