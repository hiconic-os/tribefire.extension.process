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

import java.util.List;

import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.time.TimeSpan;

@SelectiveInformation("node: ${state}")
public interface StandardNode extends Node {

	EntityType<StandardNode> T = EntityTypes.T(StandardNode.class);
	
	String gracePeriod = "gracePeriod";
	String conditionalEdges = "conditionalEdges";
	String isRestingNode = "isRestingNode";
	String onLeft = "onLeft";
	String decoupledInteraction = "decoupledInteraction";

	TimeSpan getGracePeriod();
	void setGracePeriod(TimeSpan gracePeriod);
	
	List<ConditionalEdge> getConditionalEdges();
	void setConditionalEdges(List<ConditionalEdge> conditionalEdges);

	@Deprecated
	boolean getIsRestingNode();
	@Deprecated
	void setIsRestingNode(boolean isRestingNode);
	
	List<TransitionProcessor> getOnLeft();
	void setOnLeft(List<TransitionProcessor> onLeft);
	
	DecoupledInteraction getDecoupledInteraction();
	void setDecoupledInteraction(DecoupledInteraction decoupledInteraction);
}
