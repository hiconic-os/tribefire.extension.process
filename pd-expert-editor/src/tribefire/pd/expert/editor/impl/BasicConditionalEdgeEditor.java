// ============================================================================
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package tribefire.pd.expert.editor.impl;

import java.util.Collections;
import java.util.List;

import tribefire.extension.process.model.deployment.ConditionProcessor;
import tribefire.extension.process.model.deployment.ConditionalEdge;
import tribefire.extension.process.model.deployment.TransitionProcessor;
import tribefire.pd.expert.editor.api.ConditionalEdgeEditor;

public class BasicConditionalEdgeEditor extends AbstractProcessElementEditor<ConditionalEdge> implements ConditionalEdgeEditor {


	public BasicConditionalEdgeEditor(ConditionalEdge edge) {
		this(Collections.singletonList(edge));
	}
	
	public BasicConditionalEdgeEditor(List<ConditionalEdge> elements) {
		super(elements);
	}

	@Override
	public void addOnTransit(TransitionProcessor transitionProcessor){
		elements().forEach(e -> e.getOnTransit().add(transitionProcessor));
	}

	@Override
	public void setCondition(ConditionProcessor condition) {
		elements().forEach(e -> e.setCondition(condition));
	}
}
