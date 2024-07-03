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