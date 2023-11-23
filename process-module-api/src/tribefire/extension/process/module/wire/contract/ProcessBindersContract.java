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
package tribefire.extension.process.module.wire.contract;

import com.braintribe.model.processing.deployment.api.ComponentBinder;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.extension.process.api.ConditionProcessor;
import tribefire.extension.process.api.TransitionProcessor;

/**
 * @author peter.gazdik
 */
public interface ProcessBindersContract extends WireSpace {

	ComponentBinder<tribefire.extension.process.model.deployment.TransitionProcessor, TransitionProcessor<?>> transitionProcessor();
	
	ComponentBinder<tribefire.extension.process.model.deployment.ConditionProcessor, ConditionProcessor<?>> conditionProcessor();
	
}
