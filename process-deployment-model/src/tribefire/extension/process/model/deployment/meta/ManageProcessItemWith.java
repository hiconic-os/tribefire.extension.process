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
package tribefire.extension.process.model.deployment.meta;

import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.meta.data.EntityTypeMetaData;

import tribefire.extension.process.model.deployment.ProcessDefinition;

public interface ManageProcessItemWith extends EntityTypeMetaData {

	EntityType<ManageProcessItemWith> T = EntityTypes.T(ManageProcessItemWith.class);

	String processDefinition = "processDefinition";

	@Mandatory
	ProcessDefinition getProcessDefinition();
	void setProcessDefinition(ProcessDefinition processDefinition);
}
