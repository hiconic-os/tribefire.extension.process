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
package tribefire.extension.process.rx.processing.mgt.processor;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;

import tribefire.extension.process.api.model.LockedProcessRequest;
import tribefire.extension.process.data.model.ProcessItem;
import tribefire.extension.process.model.configuration.ProcessDefinition;
import tribefire.extension.process.rx.processing.oracle.ProcessOracle;

public abstract class OracledProcessRequestProcessor<R extends LockedProcessRequest, E> extends LockingProcessRequestProcessor<R, E> {

	protected ProcessOracle processOracle;

	@Override
	protected Reason validateItem(ProcessItem processItem) {

		Maybe<ProcessDefinition> definitionMaybe = processManagerContext.processDefinitionResolver.resolve(processItem.entityType(),
				systemSession().getModelAccessory().getCmdResolver());

		if (definitionMaybe.isUnsatisfied())
			return definitionMaybe.whyUnsatisfied();

		processOracle = processManagerContext.processManagerOracle.get(definitionMaybe.get());

		return null;
	}

	protected void enqueueProcessContinuation() {
		logger.debug("enqueueing " + processItem.asString() + " in state " + processItem.getState());
		processManagerContext.enqueueProcessContinuation(processItem, context().getDomainId());
	}

}
