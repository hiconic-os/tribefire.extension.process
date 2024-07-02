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

import com.braintribe.logging.Logger;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.auth.SessionAuthorization;

import tribefire.extension.process.data.model.ProcessItem;
import tribefire.extension.process.data.model.log.ProcessLogEntry;
import tribefire.extension.process.data.model.log.ProcessLogEvent;
import tribefire.extension.process.processing.oracle.TransitionOracle;

public interface ProcessLog {
	Logger logger = Logger.getLogger(ProcessLog.class);
			
	ProcessItem processItem();
	PersistenceGmSession systemSession();
	PersistenceGmSession session();
	int nextLogOrder();
	default Logger logger() { return logger; }
	
	default ProcessLogEntry log(ProcessLogEvent event, String msg, TransitionOracle oracle) {
		return log(event, msg, oracle.getToState());
	}
	
	default ProcessLogEntry log(ProcessLogEvent event, String msg, String state) {
		return build(event).msg(msg).state(state).done();
	}
	
	default ProcessLogEntry log(ProcessLogEvent event, String msg) {
		return build(event).msg(msg).done();
	}
	
	default ProcessLogEntryBuilder build(ProcessLogEvent event) {
		SessionAuthorization sessionAuthorization = session().getSessionAuthorization();
		String initiator = sessionAuthorization != null? sessionAuthorization.getUserId(): "<unauthenticated>";
		
		return new ProcessLogEntryBuilderImpl(systemSession(), event, this::log) //
				.order(nextLogOrder()) //
				.initiator(initiator) //
				.itemId(processItem().getId()) // 
				.state(processItem().getState());
	}
	
	default void log(ProcessLogEntry e) {
		logger().debug(e.toString());
	}
}
