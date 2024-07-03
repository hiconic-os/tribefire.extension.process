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
package tribefire.extension.process.processing.mgt;

import java.util.Collections;
import java.util.List;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.logging.Logger;
import com.braintribe.model.service.api.result.Neutral;

import tribefire.extension.process.api.model.ctrl.ReviveProcesses;

public class ProcessRevivalWorker implements Runnable {
	private static final Logger logger = Logger.getLogger(ProcessRevivalWorker.class);
	private ProcessManagerContext context;
	
	
	public ProcessRevivalWorker(ProcessManagerContext context) {
		super();
		this.context = context;
	}

	@Override
	public void run() {
		while (true) {
			if (Thread.interrupted())
				break;

			try {
				List<String> accessIds = determinedAccesses();
				
				for (String accessId: accessIds) {
					
					if (Thread.interrupted())
						break;
					
					try {
						ReviveProcesses reviveProcesses = ReviveProcesses.T.create();
						reviveProcesses.setDomainId(accessId);
						Maybe<Neutral> maybe = reviveProcesses.eval(context.evaluator).getReasoned();
						
						if (maybe.isUnsatisfied()) {
							logger.error("Error while reviving processes in access [" + accessId + "]: " + maybe.whyUnsatisfied().stringify());
						}
					}
					catch (Exception e)  {
						logger.error("Error while reviving processes in access [" + accessId + "]", e);
					}
				}
			}
			catch (Exception e) {
				logger.error("Error in worker", e);
			}
			
			try {
				Thread.sleep(context.monitorInterval.toLongMillies());
			} catch (InterruptedException e) {
				break;
			}
		}
	}

	private List<String> determinedAccesses() {
		try {
			return context.monitoredAccessSupplier.get();
		} catch (Exception e) {
			logger.error("error while determining accesses to be monitored", e);
			return Collections.emptyList();
		}
	}
}
