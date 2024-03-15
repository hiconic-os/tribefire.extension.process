// ============================================================================
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package tribefire.extension.process.api.model.analysis;

import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.pagination.HasPagination;
import com.braintribe.model.service.api.ServiceRequest;

import tribefire.extension.process.api.model.ProcessRequest;
import tribefire.extension.process.api.model.data.ProcessFilter;
import tribefire.extension.process.api.model.data.ProcessList;

public interface GetProcessList extends ProcessRequest, ProcessFilter, HasPagination {
	EntityType<GetProcessList> T = EntityTypes.T(GetProcessList.class);
	
	String descending = "descending";
	
	boolean getDescending();
	void setDescending(boolean descending);
	
	@Override
	EvalContext<? extends ProcessList> eval(Evaluator<ServiceRequest> evaluator);
}