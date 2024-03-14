// ============================================================================
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package tribefire.extension.process.processing.mgt.processor;

import java.util.Date;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.model.service.api.result.Neutral;

import tribefire.extension.process.api.model.ctrl.ResumeProcess;
import tribefire.extension.process.api.model.ctrl.ResumeProcessToState;
import tribefire.extension.process.data.model.ProcessItem;
import tribefire.extension.process.data.model.log.ProcessLogEvent;
import tribefire.extension.process.data.model.state.ProcessActivity;
import tribefire.extension.process.reason.model.EdgeNotFound;
import tribefire.extension.process.reason.model.IllegalTransition;
import tribefire.extension.process.reason.model.NodeNotFound;
import tribefire.extension.process.reason.model.UnexpectedProcessActivity;

public class ResumeProcessProcessor extends OracledProcessRequestProcessor<ResumeProcess, Neutral> {
	
	@Override
	protected Reason validateItem(ProcessItem processItem) {
		Reason superReason = super.validateItem(processItem);
		
		if (superReason != null)
			return superReason;
		
		if (processItem.getActivity() != ProcessActivity.waiting) {
			return Reasons.build(UnexpectedProcessActivity.T) //
					.text("ProcessItem " + processItem + " is in unexpected activity " + processItem.getActivity()) //
					.toReason();
		}
		
		return null;
	}
	
	@Override
	protected Maybe<Neutral> processWithLockedItem() {
		// do state selection if required
		Reason stateSelectionError = selectNextStateIfRequired();
		
		if (stateSelectionError != null)
			return stateSelectionError.asMaybe();

		// reactivate process
		processItem.setLastTransit(new Date());
		processItem.setActivity(ProcessActivity.processing);
		log(ProcessLogEvent.PROCESS_RESUMED, "process resumed");
		
		commitItem();
		enqueueProcessContinuation();
		
		return Maybe.complete(Neutral.NEUTRAL);	
	}

	private Reason selectNextStateIfRequired() {
		ResumeProcess request = request();
		if (request instanceof ResumeProcessToState) {
			ResumeProcessToState resumeProcessToState = (ResumeProcessToState)request;
			
			String state = processItem.getState();
			String nextState = resumeProcessToState.getToState();
			
			Reason illegalTransitionCause = null;
			
			if (!processOracle.hasState(nextState)) {
				illegalTransitionCause = Reasons.build(NodeNotFound.T).text("State " + nextState + " not found").toReason();
			}
			else if (!processOracle.hasEdge(state, nextState)) {
				illegalTransitionCause = Reasons.build(EdgeNotFound.T).text("Edge from state [" + state + "] to state [" + nextState + "] not found").toReason();
			}
			else {
				processItem.setNextState(nextState);
				log(ProcessLogEvent.NEXT_STATE_SELECTED, String.format("resuming to state [%s]", resumeProcessToState.getToState()));

				return null;
			}
			
			return Reasons.build(IllegalTransition.T).text("Illegal transition for process " + processItem).cause(illegalTransitionCause).toReason();
		}
		
		return null;
	}
	
}