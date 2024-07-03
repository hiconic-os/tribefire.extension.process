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
import tribefire.extension.process.data.model.state.TransitionPhase;
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
		
		// Temporary check and fix for null transition phase which may result from PE-PM-migrated processes
		// In the migration case the logSequence must be null and can be used as a migration detector to validate the automatic fix 
		if (processItem.getTransitionPhase() == null && processItem.getLogSequence() == null)
			processItem.setTransitionPhase(TransitionPhase.DECOUPLED_INTERACTION);
		
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