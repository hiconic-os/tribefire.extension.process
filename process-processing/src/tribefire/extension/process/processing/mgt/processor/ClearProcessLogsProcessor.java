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
import com.braintribe.model.generic.manipulation.DeleteMode;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.service.api.result.Neutral;
import com.braintribe.utils.CollectionTools;

import tribefire.extension.process.api.model.ctrl.ClearProcessLogs;
import tribefire.extension.process.data.model.log.ProcessLogEntry;
import tribefire.extension.process.processing.mgt.common.ProcessLogQueries;

public class ClearProcessLogsProcessor extends ProcessManagerRequestProcessor<ClearProcessLogs, Neutral> {

	@Override
	public Maybe<Neutral> processReasoned() {
		SelectQuery entriesQuery = ProcessLogQueries.logEntryIds(request, request);
		
		List<String> ids = systemSession().queryDetached().select(entriesQuery).list();
		
		List<List<String>> bulks = CollectionTools.split(ids, 100);
		
		for (List<String> bulk: bulks) {
			PersistenceGmSession bulkSession = systemSession().newEquivalentSession();
			
			for (String id: bulk) {
				ProcessLogEntry entry = bulkSession.query().entity(ProcessLogEntry.T, id).findLocalOrBuildShallow();
				bulkSession.deleteEntity(entry, DeleteMode.ignoreReferences);
			}
			
			bulkSession.commit();
		}
		
		return Maybe.complete(Neutral.NEUTRAL);
	}
}