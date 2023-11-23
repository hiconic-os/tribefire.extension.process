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
package tribefire.extension.process.wire.space;

import com.braintribe.model.processing.deployment.api.ComponentBinder;
import com.braintribe.model.processing.deployment.api.ComponentBinders;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.extension.process.model.deployment.ConditionProcessor;
import tribefire.extension.process.model.deployment.TransitionProcessor;
import tribefire.extension.process.module.wire.contract.ProcessBindersContract;

@Managed
public class ProcessBindersSpace implements ProcessBindersContract {

	@Override
	@Managed
	public ComponentBinder<TransitionProcessor, tribefire.extension.process.api.TransitionProcessor<?>> transitionProcessor() {
		return ComponentBinders.binder(TransitionProcessor.T, tribefire.extension.process.api.TransitionProcessor.class);
	}
	
	@Override
	@Managed
	public ComponentBinder<ConditionProcessor, tribefire.extension.process.api.ConditionProcessor<?>> conditionProcessor() {
		return ComponentBinders.binder(ConditionProcessor.T, tribefire.extension.process.api.ConditionProcessor.class);
	}
	
}
