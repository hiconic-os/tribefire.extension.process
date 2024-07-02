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
import java.util.UUID;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.InternalError;
import com.braintribe.logging.Logger;
import com.braintribe.model.processing.session.api.persistence.auth.SessionAuthorization;
import com.braintribe.model.service.api.result.Neutral;
import com.braintribe.model.time.TimeSpan;

import tribefire.extension.process.api.model.ctrl.StartProcess;
import tribefire.extension.process.api.model.ctrl.StartProcessToState;
import tribefire.extension.process.data.model.ProcessItem;
import tribefire.extension.process.data.model.log.ProcessLogEvent;
import tribefire.extension.process.data.model.state.ProcessActivity;
import tribefire.extension.process.data.model.state.TransitionPhase;
import tribefire.extension.process.model.deployment.Node;
import tribefire.extension.process.model.deployment.StandardNode;
import tribefire.extension.process.reason.model.EdgeNotFound;
import tribefire.extension.process.reason.model.IllegalTransition;
import tribefire.extension.process.reason.model.NodeNotFound;
import tribefire.extension.process.reason.model.UnexpectedProcessActivity;
import tribefire.extension.process.reason.model.UnexpectedProcessState;

public class StartProcessProcessor extends OracledProcessRequestProcessor<StartProcess, Neutral> {
	private static final Logger logger = Logger.getLogger(OracledProcessRequestProcessor.class);
	
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
		
		String toState = nextStateMaybe.get();
		
		if (toState != null)
			processItem.setNextState(toState);
		
		Date now = new Date();
		
		// phase reset
		processItem.setTransitionPhase(TransitionPhase.CHANGED_STATE);
		processItem.setTransitionProcessorId(null);
		processItem.setStartedAt(now);
		processItem.setInitiator(getInitiator());
		processItem.setLastTransit(now);
		processItem.setActivity(ProcessActivity.processing);
		
		determineOverdue();

		log(ProcessLogEvent.PROCESS_STARTED, "process started");
		
		if (toState != null)
			log(ProcessLogEvent.NEXT_STATE_SELECTED, "process was started to state [" + toState + "]");
		
		try {
			commitItem();
		} catch (Exception e) { 
			String tb = UUID.randomUUID().toString();
			String msg = "Internal error while initializing ProccessItem " + processItem + " (traceback="+tb+")";
			logger.error(msg, e);
			return InternalError.from(e, msg).asMaybe();
		}
		
		enqueueProcessContinuation();
		
		return Maybe.complete(Neutral.NEUTRAL);
	}

	private String getInitiator() {
		String requestorUserName = context().getRequestorUserName();
		
		if (requestorUserName != null)
			return requestorUserName;
		
		SessionAuthorization sessionAuthorization = session().getSessionAuthorization();
		
		if (sessionAuthorization != null)
			return sessionAuthorization.getUserName();
		
		return "<unauthenticated>";
	}

	private void determineOverdue() {
		Node node = processOracle.nodeByState.get(null);
		
		if (!(node instanceof StandardNode)) 
			return;
		
		StandardNode standardNode = (StandardNode)node;
		
		if (standardNode.getDecoupledInteraction() == null)
			return;
		
		TimeSpan gracePeriod = standardNode.getGracePeriod();
		
		if (gracePeriod == null)
			gracePeriod = processOracle.processDefinition.getGracePeriod();
		
		if (gracePeriod == null)
			return;
		
		long millies = gracePeriod.toLongMillies();
		Date overdueAt = new Date(System.currentTimeMillis() + millies);
		processItem.setOverdueAt(overdueAt);

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
			
			return Reasons.build(IllegalTransition.T).text("Illegal initial transition for process " + processItem).cause(illegalTransitionCause).toMaybe();
		}
		
		return Maybe.complete(null);
	}
	
}