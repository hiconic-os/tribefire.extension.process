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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.query.building.SelectQueries;
import com.braintribe.model.processing.session.api.managed.ModelAccessory;
import com.braintribe.model.query.From;
import com.braintribe.model.query.OrderingDirection;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.conditions.Condition;
import com.braintribe.model.service.api.result.Neutral;
import com.braintribe.model.time.TimeSpan;
import com.braintribe.model.time.TimeUnit;

import tribefire.extension.process.api.model.ctrl.ReviveProcesses;
import tribefire.extension.process.data.model.ProcessItem;
import tribefire.extension.process.data.model.state.ProcessActivity;
import tribefire.extension.process.model.deployment.ProcessDefinition;
import tribefire.extension.process.model.deployment.meta.ManageProcessItemWith;

public class ReviveProcessesProcessor extends ProcessManagerRequestProcessor<ReviveProcesses, Neutral> {
	private static final Logger logger = Logger.getLogger(RecoverProcessProcessor.class);

	@Override
	public Maybe<Neutral> processReasoned() {
		try {
			for (EntityType<? extends ProcessItem> processType: getMappedProcessesTypes()) {
				// unattended processes
				reviveUnattended(processType);
				
				// overdue processes
				reviveOverdue(processType);
			}
		} catch (Exception e) {
			logger.error("error while reviving processes for access " + session().getAccessId(), e);
		}
		
		return Maybe.complete(Neutral.NEUTRAL);
	}
	
	private void reviveOverdue(EntityType<? extends ProcessItem> processType) {
		List<String> overdueProcessItemIds = session().queryDetached().select(RevivalQueries.overdueProcessIds(processType)).list();
		
		logger.debug("Found " + overdueProcessItemIds.size() + " overdue process(es) of type " + processType.getTypeSignature());
		overdueProcessItemIds.forEach(id -> processManagerContext.enqueueProcessContinuation(processType, id, session().getAccessId()));
		
		logger.debug("Revived" + overdueProcessItemIds.size() + " overdue process(es) of type " + processType.getTypeSignature());
	}

	private void reviveUnattended(EntityType<? extends ProcessItem> processType) {
		List<String> unattendedProcessItemIds = session().queryDetached().select(RevivalQueries.unattendedProcessIds(processType)).list();
		
		logger.debug("Found " + unattendedProcessItemIds.size() + " unattended process(es) of type " + processType.getTypeSignature());

		unattendedProcessItemIds.forEach(id -> processManagerContext.enqueueProcessContinuation(processType, id, session().getAccessId()));
		
		logger.debug("Revived " + unattendedProcessItemIds.size() + " unattended process(es) of type " + processType.getTypeSignature());
	}

	private List<EntityType<? extends ProcessItem>> getMappedProcessesTypes() {
		ModelAccessory modelAccessory = session().getModelAccessory();
		Set<EntityType<? extends ProcessItem>> processTypes = modelAccessory.getOracle().findEntityTypeOracle(ProcessItem.T).getSubTypes().onlyInstantiable().asTypes();
		
		List<EntityType<? extends ProcessItem>> mappedProcessTypes = new ArrayList<>();
		for (EntityType<? extends ProcessItem> processType: processTypes) {
			ManageProcessItemWith manageProcessItemWith = modelAccessory.getMetaData().entityType(processType).meta(ManageProcessItemWith.T).exclusive();
			
			if (manageProcessItemWith == null)
				continue;
			
			ProcessDefinition processDefinition = manageProcessItemWith.getProcessDefinition();
			
			if (processDefinition == null)
				continue;
			
			mappedProcessTypes.add(processType);
		}
		
		return mappedProcessTypes;
	}
	
	private static class RevivalQueries extends SelectQueries {
		static SelectQuery unattendedProcessIds(EntityType<? extends ProcessItem> processType) {
			Date stateThreshold = new Date(System.currentTimeMillis() - TimeSpan.create(1, TimeUnit.minute).toLongMillies());
			From p = source(processType);
			
			Condition condition = and( //
					lt(property(p, ProcessItem.lastTransit), stateThreshold), //
					eq(property(p, ProcessItem.activity), ProcessActivity.processing) //
			);
			
			return from(p) //
					.where(condition) //
					.orderBy(OrderingDirection.ascending, property(p, ProcessItem.overdueAt)) //
					.select(property(p, ProcessItem.id));
		}
		
		static SelectQuery overdueProcessIds(EntityType<? extends ProcessItem> processType) {
			Date now = new Date();
			From p = source(processType);
			
			Condition condition = and( //
					lt(property(p, ProcessItem.overdueAt), now), //
					eq(property(p, ProcessItem.activity), ProcessActivity.waiting) //
			);
			
			
			return from(p) //
					.where(condition) //
					.orderBy(OrderingDirection.ascending, property(p, ProcessItem.lastTransit)) //
					.select(property(p, ProcessItem.id));
		}
	}
}