// ============================================================================
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package tribefire.extension.process.api.model.data;

import java.util.List;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.pagination.Paginated;

import tribefire.extension.process.data.model.ProcessItem;

public interface ProcessList extends Paginated {
	EntityType<ProcessList> T = EntityTypes.T(ProcessList.class);
	
	String processes = "processes";
	
	List<ProcessItem> getProcesses();
	void setProcesses(List<ProcessItem> processes);
}
