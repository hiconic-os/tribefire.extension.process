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

import java.util.Date;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.annotation.meta.Indexed;
import com.braintribe.model.generic.annotation.meta.Priority;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

import tribefire.extension.process.data.model.ProcessItem;

/**
 * The section of properties actually meant for {@link ProcessItem} but can also be used to recover a process after it got halted.
 */
@Abstract
public interface ProcessControl extends GenericEntity {
	EntityType<ProcessControl> T = EntityTypes.T(ProcessControl.class);
	
	String activity = "activity";
	String state = "state";
	String transitionPhase = "transitionPhase";
	String transitionProcessorId = "transitionProcessorId";
	String nextState = "nextState";
	String previousState = "previousState";
	
	/**
	 * The activity of the process which is used to differentiate unattended processes waiting or ended ones and to validate operations that are done to the process
	 */
	@Priority(10)
	ProcessActivity getActivity();
	void setActivity(ProcessActivity activity);

	/**
	 * The state in which the process is currently in. It is associated to a node defined in the graph of a process definition.
	 * Along with {@link #getPreviousState()} it defines the last edge in the graph that was taken by the routing.  
	 */
	@Priority(9)
	String getState();
	void setState(String state);
	
	/**
	 * The state in which the process was previously in. It is associated to a node defined in the graph of a process definition
	 * Along with {@link #getState()} it defines the last edge in the graph that was taken by the routing. 
	 */
	@Priority(8)
	String getPreviousState();
	void setPreviousState(String previousState);

	/**
	 * The state that was required to be the next state for routing by either a transition processor or some decoupled interaction that resumed a waiting
	 * process to a specific state.
	 */
	@Priority(7)
	String getNextState();
	void setNextState(String nextState);
	
	@Indexed
	Date getOverdueAt();
	void setOverdueAt(Date overdueAt);
	
	/**
	 * This property keeps information about the fine grained progress after a state change in order to be able to continue
	 * processes with the correct step after it got unattended (e.g by shutdown)
	 */
	@Priority(6)
	TransitionPhase getTransitionPhase();
	void setTransitionPhase(TransitionPhase transitionPhase);

	/**
	 * This property holds identification information about the related transition processor 
	 * for the {@link #getTransitionPhase() transition phases} {@link TransitionPhase#EXECUTING_PROCESSOR EXECUTING_PROCESSOR} and {@link TransitionPhase#COMPLETED_PROCESSOR COMPLETED_PROCESSOR}. 
	 * It is used to continue with the correct action after it got unattended (e.g by shutdown) and revived.
	 */
	String getTransitionProcessorId();
	void setTransitionProcessorId(String transitionProcessorId);
}
