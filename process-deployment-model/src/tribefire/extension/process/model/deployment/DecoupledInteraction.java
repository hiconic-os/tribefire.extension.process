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
package tribefire.extension.process.model.deployment;

import java.util.Set;

import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.descriptive.HasDescription;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface DecoupledInteraction extends HasDescription {

	EntityType<DecoupledInteraction> T = EntityTypes.T(DecoupledInteraction.class);
	
	String userInteraction = "userInteraction";
	String workers = "workers";
	
	boolean getUserInteraction();
	void setUserInteraction(boolean userInteraction);
	
	Set<Deployable> getWorkers();
	void setWorkers(Set<Deployable> workers);
}
