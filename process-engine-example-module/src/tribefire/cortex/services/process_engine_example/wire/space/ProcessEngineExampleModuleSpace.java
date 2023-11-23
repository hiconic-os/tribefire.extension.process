// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
// 
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package tribefire.cortex.services.process_engine_example.wire.space;

import com.braintribe.model.goofydeployment.GoofyClearanceChecker;
import com.braintribe.model.goofydeployment.GoofyClearedChecker;
import com.braintribe.model.goofydeployment.GoofyDecoder;
import com.braintribe.model.goofydeployment.GoofyErrorProducer;
import com.braintribe.model.goofydeployment.GoofyHasher;
import com.braintribe.model.goofydeployment.GoofyOutputer;
import com.braintribe.model.goofydeployment.GoofyValidator;
import com.braintribe.model.goofydeployment.GoofyWatcher;
import com.braintribe.model.processing.deployment.api.binding.DenotationBindingBuilder;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.extension.process.module.wire.contract.ProcessBindersContract;
import tribefire.module.wire.contract.TribefireModuleContract;
import tribefire.module.wire.contract.TribefireWebPlatformContract;

@Managed
public class ProcessEngineExampleModuleSpace implements TribefireModuleContract {

	@Import
	private TribefireWebPlatformContract tfPlatform;
	
	@Import
	private ProcessEngineExampleDeployablesSpace deployables;
	
	@Import
	private ProcessBindersContract processBinders;
	
	@Override
	public void bindDeployables(DenotationBindingBuilder bindings) {
		bindings.bind(GoofyWatcher.T) //
				.component(tfPlatform.binders().worker()) //
				.expertFactory(deployables::goofyWatcher);

		bindings.bind(GoofyClearanceChecker.T) //
				.component(processBinders.conditionProcessor()) //
				.expertSupplier(deployables::goofyClearanceChecker);

		bindings.bind(GoofyClearedChecker.T) //
				.component(processBinders.conditionProcessor()) //
				.expertSupplier(deployables::goofyClearedChecker);

		bindings.bind(GoofyOutputer.T) //
				.component(processBinders.transitionProcessor()) //
				.expertFactory(deployables::goofyOutputer);

		bindings.bind(GoofyHasher.T) //
				.component(processBinders.transitionProcessor()) //
				.expertSupplier(deployables::goofyHashProcessor);

		bindings.bind(GoofyValidator.T) //
				.component(processBinders.transitionProcessor()) //
				.expertSupplier(deployables::goofyValidateProcessor);

		bindings.bind(GoofyDecoder.T) //
				.component(processBinders.transitionProcessor()) //
				.expertSupplier(deployables::goofyDecoder);

		bindings.bind(GoofyErrorProducer.T) //
				.component(processBinders.transitionProcessor()) //
				.expertSupplier(deployables::goofyErrorProducer);
	}
}
