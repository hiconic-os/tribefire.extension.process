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
