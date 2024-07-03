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
