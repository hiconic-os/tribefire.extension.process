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
