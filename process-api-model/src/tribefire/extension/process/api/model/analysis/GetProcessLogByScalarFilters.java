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
