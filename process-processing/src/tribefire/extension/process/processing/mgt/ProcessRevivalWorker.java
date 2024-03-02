// ============================================================================
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package tribefire.extension.process.processing.mgt;

import java.util.Collections;
import java.util.List;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.logging.Logger;
import com.braintribe.model.service.api.result.Neutral;

import tribefire.extension.process.api.model.crtl.ReviveProcesses;

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
