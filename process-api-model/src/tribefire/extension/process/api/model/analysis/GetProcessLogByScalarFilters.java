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

import java.util.List;

import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.pagination.HasPagination;
import com.braintribe.model.service.api.ServiceRequest;

import tribefire.extension.process.api.model.ProcessRequest;
import tribefire.extension.process.data.model.log.ProcessLogEntry;

public interface GetProcessLogByScalarFilters extends ProcessRequest, HasPagination {

	EntityType<GetProcessLogByScalarFilters> T = EntityTypes.T(GetProcessLogByScalarFilters.class);

	String process = "process";

	boolean getIncludeTransitionEvents();
	void setIncludeTransitionEvents(boolean includeTransitionEvents);

	boolean getIncludeConditionEvents();
	void setIncludeConditionEvents(boolean includeConditionEvents);

	boolean getIncludeTransitionProcessorEvents();
	void setIncludeTransitionProcessorEvents(boolean includeTransitionProcessorEvents);

	boolean getIncludeConditionProcessorEvents();
	void setIncludeConditionProcessorEvents(boolean includeConditionProcessorEvents);

	boolean getIncludeControlEvents();
	void setIncludeControlEvents(boolean includeControlEvents);

	boolean getIncludeProblemEvents();
	void setIncludeProblemEvents(boolean includeOtherEvents);

	String getIncludeEvents();
	void setIncludeEvents(String includeEvents);

	String getState();
	void setState(String state);

	@Override
	EvalContext<List<ProcessLogEntry>> eval(Evaluator<ServiceRequest> evaluator);

}
