// ============================================================================
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
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
