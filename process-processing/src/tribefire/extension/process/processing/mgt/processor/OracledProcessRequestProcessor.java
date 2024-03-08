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


import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.logging.Logger;

import tribefire.extension.process.api.model.LockedProcessRequest;
import tribefire.extension.process.data.model.ProcessItem;
import tribefire.extension.process.model.deployment.ProcessDefinition;
import tribefire.extension.process.model.deployment.meta.ManageProcessItemWith;
import tribefire.extension.process.processing.oracle.ProcessOracle;
import tribefire.extension.process.reason.model.ProcessDefinitionNotFound;

public abstract class OracledProcessRequestProcessor<R extends LockedProcessRequest, E> extends LockingProcessRequestProcessor<R, E> {
	private static final Logger logger = Logger.getLogger(OracledProcessRequestProcessor.class);
	protected ProcessOracle processOracle;
	
	protected Reason validateItem(ProcessItem processItem) {
		
		ManageProcessItemWith manageProcessItemWith = systemSession().getModelAccessory().getMetaData().entity(processItem).meta(ManageProcessItemWith.T).exclusive();

		if (manageProcessItemWith == null)
			return Reasons.build(ProcessDefinitionNotFound.T).text("ProcessDefinition not mapped for process type " + processItem.entityType().getTypeSignature()).toReason();
		
		ProcessDefinition processDefinition = manageProcessItemWith.getProcessDefinition();
		
		if (processDefinition == null)
			return Reasons.build(ProcessDefinitionNotFound.T).text("ProcessDefinition not assigned on mapping for process type " + processItem.entityType().getTypeSignature()).toReason();
		
		processOracle = processManagerContext.processManagerOracle.get(manageProcessItemWith.getProcessDefinition());
		
		return null;
	}
	
	
	protected void enqueueProcessContinuation() {
		logger.debug("enqueueing " + processItem.asString() + " in state " + processItem.getState());
		processManagerContext.enqueueProcessContinuation(processItem, context().getDomainId());
	}
	
}