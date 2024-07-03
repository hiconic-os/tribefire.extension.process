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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.Canceled;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.query.building.SelectQueries;
import com.braintribe.model.query.From;
import com.braintribe.model.query.PropertyOperand;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.record.ListRecord;
import com.braintribe.model.time.TimeSpan;
import com.braintribe.model.time.TimeUnit;

import tribefire.extension.process.api.model.ctrl.WaitForProcesses;
import tribefire.extension.process.api.model.data.ProcessAndActivities;
import tribefire.extension.process.api.model.data.ProcessIdentification;
import tribefire.extension.process.data.model.ProcessItem;
import tribefire.extension.process.data.model.state.ProcessActivity;
import tribefire.extension.process.processing.mgt.api.Reference;

public class WaitForProcessesProcessor extends ProcessManagerRequestProcessor<WaitForProcesses, Map<ProcessIdentification, ProcessActivity>> {
	private static final Logger logger = Logger.getLogger(WaitForProcessesProcessor.class);

	@Override
	public Maybe<Map<ProcessIdentification, ProcessActivity>> processReasoned() {
		Maybe<Map<Reference<? extends ProcessItem>, Set<ProcessActivity>>> expectedActivitiesMaybe = resolveTypes(request.getProcesses());
		
		if (expectedActivitiesMaybe.isUnsatisfied())
			return expectedActivitiesMaybe.whyUnsatisfied().asMaybe();
		
		Map<Reference<? extends ProcessItem>, Set<ProcessActivity>> expectedAcitivities = expectedActivitiesMaybe.get();
		
		ProcessWaiter processWaiter = new ProcessWaiter(expectedAcitivities);
		
		processManagerContext.addProcessListener(processWaiter);
		
		getCurrentActivities(expectedAcitivities.keySet()).forEach(processWaiter);
		
		try {
			if (!processWaiter.waitNotified(getMaxWait()))
				return Reasons.build(Canceled.T).text("Waiting was canceled to due configured max waiting time").toMaybe();
			
			return Maybe.complete(processWaiter.getReachedActivities());
		}
		finally {
			processManagerContext.removeProcessListener(processWaiter);
		}
	}
	
	private Maybe<Map<Reference<? extends ProcessItem>, Set<ProcessActivity>>> resolveTypes(Set<ProcessAndActivities> processes) {
		Map<Reference<? extends ProcessItem>, Set<ProcessActivity>> map = new HashMap<>();
		
		for (ProcessAndActivities processAndActivities : processes) {
			Maybe<EntityType<? extends ProcessItem>> typeMaybe = resolveType(processAndActivities.getItemType());
			
			if (typeMaybe.isUnsatisfied())
				return typeMaybe.whyUnsatisfied().asMaybe();
			
			EntityType<? extends ProcessItem> itemType = typeMaybe.get();
			
			map.put(Reference.of(itemType, processAndActivities.getItemId()), processAndActivities.getActivities());
		}
		
		return Maybe.complete(map);
	}
	
	private Map<Reference<? extends ProcessItem>, ProcessActivity> getCurrentActivities(Set<Reference<? extends ProcessItem>> processes) {
		Map<Reference<? extends ProcessItem>, ProcessActivity> activities = new HashMap<>();
		
		Map<EntityType<? extends ProcessItem>, Set<String>> idsMap = new HashMap<>();
		
		// sort processes by type for concrete queries
		for (Reference<? extends ProcessItem> itemReference : processes) {
			idsMap.computeIfAbsent(itemReference.type(), k -> new HashSet<>()).add(itemReference.id());
		}
		
		for (Map.Entry<EntityType<? extends ProcessItem>, Set<String>> entry : idsMap.entrySet()) {
			EntityType<? extends ProcessItem> type = entry.getKey();
			Set<String> ids = entry.getValue();
			SelectQuery query = ProcessWaiterQueries.processes(type, ids);
			
			List<ListRecord> results = session().queryDetached().select(query).list();
			
			for (ListRecord result: results) {
				String id = (String) result.get(0);
				ProcessActivity activity = (ProcessActivity) result.get(1);
				activities.put(Reference.of(type, id), activity);
			}
		}
		
		return activities;
	}

	private TimeSpan getMaxWait() {
		TimeSpan maxWait = request.getMaxWait();
		
		if (maxWait == null)
			maxWait = TimeSpan.create(1, TimeUnit.minute);
		
		return maxWait;
	}
	
	private static class ProcessWaiterQueries extends SelectQueries {

		public static SelectQuery processes(EntityType<? extends ProcessItem> type, Set<String> ids) {
			From p = source(type);
			
			PropertyOperand idProperty = property(p, ProcessItem.id);
			PropertyOperand activitiyProperty = property(p, ProcessItem.activity);
			
			return from(p) //
					.where(in(idProperty, ids)) //
					.select(idProperty) //
					.select(activitiyProperty);
		}
		
	}
	
	private class ProcessWaiter implements BiConsumer<Reference<? extends ProcessItem>, ProcessActivity> {
		
		private volatile boolean notified;
		private Map<Reference<? extends ProcessItem>, Set<ProcessActivity>> expectedActivities;
		private Map<Reference<? extends ProcessItem>, ProcessActivity> reachedActivities = new HashMap<>();

		public ProcessWaiter(Map<Reference<? extends ProcessItem>, Set<ProcessActivity>> expectedActivities) {
			super();
			this.expectedActivities = new HashMap<>(expectedActivities); 
		}

		@Override
		public synchronized void accept(Reference<? extends ProcessItem> item, ProcessActivity activity) {
			Set<ProcessActivity> activities = expectedActivities.get(item);
			
			if (activities != null) {
				if (activities.contains(activity)) {
					logger.debug("received activity [" + activity + "] change for process " + item);
					
					expectedActivities.remove(item);
					reachedActivities.put(item, activity);
					
					int expectedActivitiesCount = expectedActivities.size();
					
					if (expectedActivitiesCount == 0) {
						notified = true;
						notify();
					}
				}
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
		
		public Map<ProcessIdentification, ProcessActivity> getReachedActivities() {
			Map<ProcessIdentification, ProcessActivity> result = new HashMap<>();
			
			for (Map.Entry<Reference<? extends ProcessItem>, ProcessActivity> entry: reachedActivities.entrySet()) {
				Reference<? extends ProcessItem> reference = entry.getKey();
				ProcessIdentification pi = ProcessIdentification.T.create();
				pi.setItemType(reference.type().getTypeSignature());
				pi.setItemId(reference.id());
				result.put(pi, entry.getValue());
			}
			
			return result;
		}
		
		
	}
	
}