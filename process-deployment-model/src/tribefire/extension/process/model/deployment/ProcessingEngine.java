// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
// 
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package tribefire.extension.process.model.deployment;

import java.util.Set;

import com.braintribe.model.extensiondeployment.StateChangeProcessorRule;
import com.braintribe.model.extensiondeployment.Worker;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.time.TimeSpan;

public interface ProcessingEngine extends StateChangeProcessorRule, Worker {

	EntityType<ProcessingEngine> T = EntityTypes.T(ProcessingEngine.class);
	
	String processDefinitions = "processDefinitions";
	String monitorInveral = "monitorInveral";
	String processingDefaultClient = "processingDefaultClient";
	
	Set<ProcessDefinition> getProcessDefinitions();
	void setProcessDefinitions( Set<ProcessDefinition> processDefinitions);
	
	TimeSpan getMonitorInterval();
	void setMonitorInterval(TimeSpan monitorInveral);
	
}
