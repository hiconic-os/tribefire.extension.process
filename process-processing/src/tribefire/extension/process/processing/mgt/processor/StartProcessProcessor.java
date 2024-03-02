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

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.InternalError;
import com.braintribe.model.service.api.result.Neutral;

import tribefire.extension.process.api.model.crtl.StartProcess;
import tribefire.extension.process.api.model.crtl.StartProcessToState;
import tribefire.extension.process.data.model.ProcessItem;
import tribefire.extension.process.data.model.log.ProcessLogEvent;
import tribefire.extension.process.data.model.state.ProcessActivity;
import tribefire.extension.process.model.deployment.Edge;
import tribefire.extension.process.reason.model.EdgeNotFound;
import tribefire.extension.process.reason.model.IllegalTransition;
import tribefire.extension.process.reason.model.NodeNotFound;
import tribefire.extension.process.reason.model.UnexpectedProcessActivity;
import tribefire.extension.process.reason.model.UnexpectedProcessState;

public class StartProcessProcessor extends OracledProcessRequestProcessor<StartProcess, Neutral> {

	@Override
	protected Reason validateItem(ProcessItem processItem) {
		Reason superReason = super.validateItem(processItem);
		
		if (superReason != null)
			return superReason;
		
		if (processItem.getActivity() != null) {
			return Reasons.build(UnexpectedProcessActivity.T) //
					.text("ProcessItem " + processItem + " is in unexpected activity " + processItem.getActivity()) //
					.toReason();
		}
		
		if (processItem.getState() != null) {
			return Reasons.build(UnexpectedProcessState.T) //
					.text("ProcessItem " + processItem + " is in unexpected state " + processItem.getState()) //
					.toReason();
		}
			
		return null;
	}
	
	@Override
	protected Maybe<Neutral> processWithLockedItem() {
		
		Maybe<String> nextStateMaybe = determineNextState();
		
		if (nextStateMaybe.isUnsatisfied())
			return nextStateMaybe.whyUnsatisfied().asMaybe();
		
		String fromState = null;
		String toState = nextStateMaybe.get();
		
		processOracle.transitionOracle(fromState, toState).initTransition(processItem);
		processItem.setStartedAt(processItem.getLastTransit());
		processItem.setActivity(ProcessActivity.processing);
		
		log(ProcessLogEvent.PROCESS_STARTED, "process started to state [" + toState + "]");
		
		try {
			commitItem();
		} catch (Exception e) {
			return InternalError.from(e, "Internal error while initializing ProccessItem " + processItem).asMaybe();
		}
		
		enqueueProcessContinuation();
		
		return Maybe.complete(Neutral.NEUTRAL);
	}

	private Maybe<String> determineNextState() {
		Reason illegalTransitionCause = null;
		
		if (request instanceof StartProcessToState) {
			String nextState = ((StartProcessToState) request).getState();
			
			if (!processOracle.hasState(nextState)) {
				illegalTransitionCause = Reasons.build(NodeNotFound.T).text("State [" + nextState + "] not found").toReason();
			}
			else if (!processOracle.hasEdge(null, nextState)) {
				illegalTransitionCause = Reasons.build(EdgeNotFound.T).text("Initial edge to state [" + nextState + "] not found").toReason();
			}
			else {
				return Maybe.complete(nextState);
			}
		}
		else {
			Maybe<Edge> edgeMaybe = processOracle.getInitialDefaultEdge();
			
			if (edgeMaybe.isSatisfied()) {
				return Maybe.complete((String)edgeMaybe.get().getTo().getState());
			}
				
			illegalTransitionCause = edgeMaybe.whyUnsatisfied();
		}
		
		return Reasons.build(IllegalTransition.T).text("Illegal initial transition for process " + processItem).cause(illegalTransitionCause).toMaybe();
	}
	
}