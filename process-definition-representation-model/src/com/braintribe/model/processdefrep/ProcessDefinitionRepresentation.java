// ============================================================================
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package com.braintribe.model.processdefrep;

import java.util.Map;
import java.util.Set;

import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.meta.data.MetaData;

import tribefire.extension.process.model.deployment.ProcessDefinition;
import tribefire.extension.process.model.deployment.ProcessElement;

 @SelectiveInformation("${name}-${processDefinition.name}")
public interface ProcessDefinitionRepresentation extends HasDimension, MetaData{

	EntityType<ProcessDefinitionRepresentation> T = EntityTypes.T(ProcessDefinitionRepresentation.class);
	
	public ProcessDefinition getProcessDefinition();
	public void setProcessDefinition(ProcessDefinition processDefinition);
	
	public String getName();
	public void setName(String name);
	
	public Set<ProcessElementRepresentation> getProcessElementRepresentations();
	public void setProcessElementRepresentations(Set<ProcessElementRepresentation> processElementRepresentations);
	
	public Map<ProcessElement, ProcessElementRepresentation> getProcessElements();
	public void setProcessElements(Map<ProcessElement, ProcessElementRepresentation> processElements);
	
	public Set<SwimLaneRepresentation> getSwimLanes();
	public void setSwimLanes(Set<SwimLaneRepresentation> swimLanes);

}
