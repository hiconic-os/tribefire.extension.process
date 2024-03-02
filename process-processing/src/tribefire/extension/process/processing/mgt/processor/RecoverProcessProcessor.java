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
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.service.api.result.Neutral;

import tribefire.extension.process.api.model.crtl.RecoverProcess;
import tribefire.extension.process.data.model.ProcessItem;
import tribefire.extension.process.data.model.log.ProcessLogEvent;
import tribefire.extension.process.data.model.state.ProcessActivity;
import tribefire.extension.process.data.model.state.ProcessControl;
import tribefire.extension.process.data.model.state.TransitionPhase;
import tribefire.extension.process.model.deployment.TransitionProcessor;
import tribefire.extension.process.processing.oracle.TransitionOracle;
import tribefire.extension.process.reason.model.UnexpectedProcessActivity;

public class RecoverProcessProcessor extends OracledProcessRequestProcessor<RecoverProcess, Neutral> {
	
	@Override
	protected Reason validateItem(ProcessItem processItem) {
		Reason invalidation = super.validateItem(processItem);
		if (invalidation != null)
			return invalidation;
		
		if (processItem.getActivity() != ProcessActivity.halted)
			return Reasons.build(UnexpectedProcessActivity.T).text("Cannot recover a process that is not halted. Process activity is " + processItem.getActivity()).toReason();
		
		return null;
	}
	
	@Override
	protected Maybe<Neutral> processWithLockedItem() {
		
		TransitionOracle transitionOracle = processOracle.transitionOracle(processItem.getPreviousState(), processItem.getState(), true);

		
		// reshape process control
		for (Property property: ProcessControl.T.getDeclaredProperties()) {
			Object value = property.get(request);
			if (value != null)
				property.set(processItem, value);
		}
		
		if (processItem.getTransitionPhase() == TransitionPhase.EXECUTING_PROCESSOR) {
			TransitionProcessor predecessorProcessor = transitionOracle.getPredecessorTransitionProcessor(processItem.getTransitionProcessorId());
			
			if (predecessorProcessor != null) {
				processItem.setTransitionPhase(TransitionPhase.COMPLETED_PROCESSOR);
				processItem.setTransitionProcessorId(predecessorProcessor.getExternalId());
			}
			else {
				processItem.setTransitionPhase(TransitionPhase.CHANGED_STATE);
				processItem.setTransitionProcessorId(null);
			}
		}
		
		if (processItem.getActivity() == ProcessActivity.halted)
			processItem.setActivity(ProcessActivity.processing);
		
		processItem.setLastTransit(new Date());
		
		final String logMsg;
		
		if (processItem.getTransitionProcessorId() != null) {
			logMsg = String.format("process recovered to activity [%s] and transition phase [%s] of transition processor [%s]", //
					processItem.getActivity(), processItem.getTransitionPhase(), processItem.getTransitionProcessorId());
		}
		else {
			logMsg = String.format("process recovered to activity [%s] and transition phase [%s]", //
					processItem.getActivity(), processItem.getTransitionPhase());
		}
		
		log(ProcessLogEvent.PROCESS_RECOVERED, logMsg);
		
		
		commitItem();
		
		enqueueProcessContinuation();
		return Maybe.complete(Neutral.NEUTRAL);
	}
	
	
}