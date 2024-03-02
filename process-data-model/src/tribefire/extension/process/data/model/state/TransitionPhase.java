// ============================================================================
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package tribefire.extension.process.data.model.state;

import com.braintribe.model.generic.base.EnumBase;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.EnumTypes;

import tribefire.extension.process.data.model.ProcessItem;

/**
 * The {@link ProcessItem#getTransitionPhase() TransitionPhase} enum along with {@link ProcessItem#getTransitionProcessorId()} controls the fine 
 * grained progress after a state change of a {@link ProcessItem}.
 * 
 * @author Dirk Scheffler
 */
public enum TransitionPhase implements EnumBase {
	/**
	 * This phase is set right after a {@link ProcessItem} changed its {@link ProcessItem#getState() state}.
	 */
	CHANGED_STATE,
	
	/**
	 * This phase is set whenever a state associated transition processor is executed and kept until it completed. An unattended process
	 * that is in that transition phase cannot be automatically revived due to a questionable idempotency.
	 * <p>
	 * This phase is accompanied by the id of the executed transition processor in {@link ProcessItem#getTransitionProcessorId()}.  
	 */
	EXECUTING_PROCESSOR, 
	
	/**
	 * This phase is set right after a transition processor has been successfully executed.
	 * <p>
	 * This phase is accompanied by the id of the executed transition processor in {@link ProcessItem#getTransitionProcessorId()}.  
	 */
	COMPLETED_PROCESSOR, 

	/**
	 * This phase is set right after a transition has been completed which means that any associated transition processor has been successfully executed.
	 */
	COMPLETED_TRANSITION, 

	/**
	 * This phase is set right after a {@link ProcessItem#getActivity() process} has been changed its activity to {@link ProcessActivity#waiting}
	 */
	DECOUPLED_INTERACTION;

	public static final EnumType T = EnumTypes.T(TransitionPhase.class);
	
	@Override
	public EnumType type() {
		return T;
	}	
}
