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

import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

import tribefire.extension.process.model.deployment.Node;

@SelectiveInformation("nodeRep: ${node.state}")
public interface NodeRepresentation extends ProcessElementRepresentation, HasDimension, HasSize, HasColor {

	EntityType<NodeRepresentation> T = EntityTypes.T(NodeRepresentation.class);

	public Node getNode();
	public void setNode(Node node);

	public String getName();
	public void setName(String name);

	public Boolean getIsInitNode();
	public void setIsInitNode(Boolean isInitNode);

}
