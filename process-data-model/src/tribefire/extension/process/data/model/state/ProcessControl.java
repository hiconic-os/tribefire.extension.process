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

import java.util.Date;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.annotation.meta.Indexed;
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
	ProcessActivity getActivity();
	void setActivity(ProcessActivity activity);

	/**
	 * The state in which the process was previously in. It is associated to a node defined in the graph of a process definition
	 * Along with {@link #getState()} it defines the last edge in the graph that was taken by the routing. 
	 */
	String getPreviousState();
	void setPreviousState(String previousState);

	/**
	 * The state in which the process is currently in. It is associated to a node defined in the graph of a process definition.
	 * Along with {@link #getPreviousState()} it defines the last edge in the graph that was taken by the routing.  
	 */
	String getState();
	void setState(String state);

	/**
	 * The state that was required to be the next state for routing by either a transition processor or some decoupled interaction that resumed a waiting
	 * process to a specific state.
	 */
	String getNextState();
	void setNextState(String nextState);
	
	@Indexed
	Date getOverdueAt();
	void setOverdueAt(Date overdueAt);
	
	/**
	 * This property keeps information about the fine grained progress after a state change in order to be able to continue
	 * processes with the correct step after it got unattended (e.g by shutdown)
	 */
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
