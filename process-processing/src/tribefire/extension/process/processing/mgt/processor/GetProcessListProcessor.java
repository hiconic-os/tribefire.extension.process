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
package tribefire.extension.process.processing.mgt.processor;

import java.util.List;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.SelectQueryResult;

import tribefire.extension.process.api.model.analysis.GetProcessList;
import tribefire.extension.process.api.model.data.ProcessList;
import tribefire.extension.process.data.model.ProcessItem;
import tribefire.extension.process.processing.mgt.common.ProcessLogQueries;

public class GetProcessListProcessor extends ProcessManagerRequestProcessor<GetProcessList, ProcessList> {
	@Override
	public Maybe<ProcessList> processReasoned() {
		SelectQuery processesQuery = ProcessLogQueries.processes(request, request.getDescending(), request);
		
		SelectQueryResult result = systemSession().queryDetached().select(processesQuery).result();
		
		ProcessList processList = ProcessList.T.create();
		processList.setHasMore(result.getHasMore());
		
		List<ProcessItem> processes = processList.getProcesses();
		
		result.getResults().stream().map(e -> (ProcessItem)e).forEach(processes::add);
		
		return Maybe.complete(processList);
	}
}