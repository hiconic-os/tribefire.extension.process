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
package tribefire.extension.process.api.model.data;

import java.util.List;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.pagination.Paginated;

import tribefire.extension.process.data.model.log.ProcessLogEntry;

public interface ProcessLog extends Paginated {
	EntityType<ProcessLog> T = EntityTypes.T(ProcessLog.class);
	
	String entries = "entries";
	
	List<ProcessLogEntry> getEntries();
	void setEntries(List<ProcessLogEntry> entries);
}
