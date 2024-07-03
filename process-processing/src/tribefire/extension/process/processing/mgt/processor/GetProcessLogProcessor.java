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

import java.util.List;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.SelectQueryResult;

import tribefire.extension.process.api.model.analysis.GetProcessLog;
import tribefire.extension.process.api.model.data.ProcessLog;
import tribefire.extension.process.data.model.log.ProcessLogEntry;
import tribefire.extension.process.processing.mgt.common.ProcessLogQueries;

public class GetProcessLogProcessor extends ProcessRequestProcessor<GetProcessLog, ProcessLog> {

	@Override
	protected Maybe<ProcessLog> processValidatedRequest() {
		SelectQuery entriesQuery = ProcessLogQueries.logEntries(itemId, request, request.getDescending(), request);
		
		SelectQueryResult result = systemSession().queryDetached().select(entriesQuery).result();
		
		ProcessLog processLog = ProcessLog.T.create();
		processLog.setHasMore(result.getHasMore());
		
		List<ProcessLogEntry> entries = processLog.getEntries();
		
		result.getResults().stream().map(e -> (ProcessLogEntry)e).forEach(entries::add);
		
		return Maybe.complete(processLog);
	}
}