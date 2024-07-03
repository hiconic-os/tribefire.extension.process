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
/**
 * 
 * Copyright by Braintribe Technologies 
 * autogenerated code 
 *
 */

package tribefire.extension.process.model.deployment;

import java.util.List;

import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * an edge declares the transition from one node to another node. 
 * <br/><br/>
 * it has a from {@link Node} and a to {@link Node}.<br/>
 * additionally, it has an error {@link Node}. <br/><br/>
 * edges are required for the logic of the processes as the processing engine only allows
 * transitions if an appropriate edge exists.   
 * 
 * @author pit
 *
 */
@SelectiveInformation("edge: ${from.state} - ${to.state}")
public interface Edge extends ProcessElement {

	EntityType<Edge> T = EntityTypes.T(Edge.class);
	
	String from = "from";
	String to = "to";
	String onTransit = "onTransit";
	String overdueNode = "overdueNode";

	StandardNode getFrom();
	void setFrom(StandardNode from);

	Node getTo();
	void setTo(Node to);
	
	List<TransitionProcessor> getOnTransit();
	void setOnTransit(List<TransitionProcessor> onTransit);

	/**
	 * @deprecated only here because cortex synchronization and weaving order is currently wrong
	 */
	@Deprecated
	Node getOverdueNode();
	/**
	 * @deprecated only here because cortex synchronization and weaving order is currently wrong
	 */
	@Deprecated
	void setOverdueNode(Node node);
}
