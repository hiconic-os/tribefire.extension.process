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

import java.util.List;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.generic.manipulation.DeleteMode;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.service.api.result.Neutral;
import com.braintribe.utils.CollectionTools;

import tribefire.extension.process.api.model.crtl.ClearProcessLog;
import tribefire.extension.process.data.model.log.ProcessLogEntry;
import tribefire.extension.process.processing.mgt.common.ProcessLogQueries;

public class ClearProcessProcessor extends ProcessRequestProcessor<ClearProcessLog, Neutral> {

	@Override
	protected Maybe<Neutral> processValidatedRequest() {
		SelectQuery entriesQuery = ProcessLogQueries.logEntryIds(itemId, request);
		
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