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

import java.util.Set;
import java.util.function.Consumer;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.Canceled;
import com.braintribe.model.time.TimeSpan;
import com.braintribe.model.time.TimeUnit;

import tribefire.extension.process.api.model.crtl.WaitForProcess;
import tribefire.extension.process.data.model.ProcessItem;
import tribefire.extension.process.data.model.state.ProcessActivity;

public class WaitForProcessProcessor extends ProcessRequestProcessor<WaitForProcess, ProcessActivity> {

	@Override
	protected Maybe<ProcessActivity> processValidatedRequest() {

		Set<ProcessActivity> activities = request.getActivities();
		
		ProcessWaiter processWaiter = new ProcessWaiter(activities);
		
		processManagerContext.addProcessListener(getItemReference(), processWaiter);
		
		try {
			Maybe<ProcessItem> processItemMaybe = getProcessItem();
			
			if (processItemMaybe.isUnsatisfied())
				return processItemMaybe.whyUnsatisfied().asMaybe();
			
			ProcessItem processItem = processItemMaybe.get();
			ProcessActivity activity = processItem.getActivity();
			if (activities.contains(activity))
				return Maybe.complete(activity);
			
			if (!processWaiter.waitNotified(getMaxWait()))
				return Reasons.build(Canceled.T).text("Waiting was canceled to due configured max waiting time").toMaybe();
			
			return Maybe.complete(processWaiter.getReachedActivity());
		}
		finally {
			processManagerContext.removeProcessListener(getItemReference(), processWaiter);
		}
	}
	
	private TimeSpan getMaxWait() {
		TimeSpan maxWait = request.getMaxWait();
		
		if (maxWait == null)
			maxWait = TimeSpan.create(1, TimeUnit.minute);
		
		return maxWait;
	}
	
	private class ProcessWaiter implements Consumer<ProcessActivity> {
		private volatile boolean notified;
		private Set<ProcessActivity> activities;
		private ProcessActivity reachedActivity;

		public ProcessWaiter(Set<ProcessActivity> activities) {
			super();
			this.activities = activities;
		}

		@Override
		public synchronized void accept(ProcessActivity activity) {
			if (activities.contains(activity)) {
				notified = true;
				reachedActivity = activity;
				notify();
			}
		}
		
		public synchronized boolean waitNotified(TimeSpan timeSpan) {
			if (notified)
				return true;
			
			try {
				wait(timeSpan.toLongMillies());
			} catch (InterruptedException e) {
				// ignore
			}
			
			return notified;
		}
		
		public ProcessActivity getReachedActivity() {
			return reachedActivity;
		}
		
		
	}
	
}