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

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.model.processing.accessrequest.api.ReasonedStatefulProcessor;

import tribefire.extension.process.api.model.LockedProcessRequest;
import tribefire.extension.process.data.model.ProcessItem;
import tribefire.extension.process.processing.mgt.ProcessLog;
import tribefire.extension.process.processing.mgt.common.ProcessLockingTrait;

public abstract class LockingProcessRequestProcessor<R extends LockedProcessRequest, E> extends ProcessRequestProcessor<R, E> implements ReasonedStatefulProcessor<E>, ProcessLog, ProcessLockingTrait {
	
	protected ProcessItem processItem;
	protected int logSequence;

	public ProcessItem processItem() {
		return processItem;
	}
	
	@Override
	public Maybe<E> processValidatedRequest() {
		
		Reason invalidationReason = validateRequest();
		
		if (invalidationReason != null)
			return invalidationReason.asMaybe();
		
		return executeLocked(this::processLocked, request.getReentranceLockId());
	}
	
	private Maybe<E> processLocked() {
		Maybe<ProcessItem> itemMaybe = getProcessItem();
		
		if (itemMaybe.isUnsatisfied())
			return itemMaybe.whyUnsatisfied().asMaybe();
		
		processItem = itemMaybe.get();
		logSequence = processItem.logSequence();
	
		return processWithLockedItem();
	}
	
	@Override
	public int nextLogOrder() {
		return logSequence++;
	}
	
	protected void commitItem() {
		int sequence = processItem.logSequence();
		
		if (sequence != logSequence)
			processItem.setLogSequence(logSequence);
		
		systemSession().commit();
	}
	
	protected abstract Maybe<E> processWithLockedItem();
}