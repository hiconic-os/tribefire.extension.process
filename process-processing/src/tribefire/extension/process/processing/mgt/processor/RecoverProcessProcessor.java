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
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.service.api.result.Neutral;

import tribefire.extension.process.api.model.ctrl.RecoverProcess;
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