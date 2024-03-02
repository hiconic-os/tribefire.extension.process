// ============================================================================
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package tribefire.extension.process.api.model.crtl;

import java.util.Map;
import java.util.Set;

import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.time.TimeSpan;

import tribefire.extension.process.api.model.ProcessManagerRequest;
import tribefire.extension.process.api.model.data.ProcessAndActivities;
import tribefire.extension.process.api.model.data.ProcessIdentification;
import tribefire.extension.process.data.model.state.ProcessActivity;

public interface WaitForProcesses extends ProcessManagerRequest {
	EntityType<WaitForProcesses> T = EntityTypes.T(WaitForProcesses.class);
	
	String maxWait = "maxWait";
	
	TimeSpan getMaxWait();
	void setMaxWait(TimeSpan maxWait);
	
	Set<ProcessAndActivities> getProcesses();
	void setProcesses(Set<ProcessAndActivities> processes);
	
	@Override
	EvalContext<Map<ProcessIdentification, ProcessActivity>> eval(Evaluator<ServiceRequest> evaluator);
}
