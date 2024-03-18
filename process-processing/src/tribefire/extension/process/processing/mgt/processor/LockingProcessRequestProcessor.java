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