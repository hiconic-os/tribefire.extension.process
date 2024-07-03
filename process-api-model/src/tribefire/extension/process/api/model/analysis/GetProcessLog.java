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
package tribefire.extension.process.api.model.analysis;

import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.pagination.HasPagination;
import com.braintribe.model.service.api.ServiceRequest;

import tribefire.extension.process.api.model.ProcessRequest;
import tribefire.extension.process.api.model.data.ProcessLog;
import tribefire.extension.process.api.model.data.ProcessLogFilter;

public interface GetProcessLog extends ProcessRequest, ProcessLogFilter, HasPagination {
	EntityType<GetProcessLog> T = EntityTypes.T(GetProcessLog.class);
	
	String descending = "descending";
	
	boolean getDescending();
	void setDescending(boolean descending);
	
	@Override
	EvalContext<? extends ProcessLog> eval(Evaluator<ServiceRequest> evaluator);
}
