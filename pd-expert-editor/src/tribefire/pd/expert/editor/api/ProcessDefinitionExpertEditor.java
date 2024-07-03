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
package tribefire.pd.expert.editor.api;

import java.util.function.Predicate;

import tribefire.extension.process.model.deployment.ConditionalEdge;
import tribefire.extension.process.model.deployment.Edge;
import tribefire.extension.process.model.deployment.Node;
import tribefire.extension.process.model.deployment.ProcessDefinition;
import tribefire.extension.process.model.deployment.ProcessElement;
import tribefire.pd.expert.editor.impl.BasicProcessDefinitionExpertEditor;

public interface ProcessDefinitionExpertEditor {
	
	ProcessDefinition definition();
	
	ProcessElementEditor elements();
	
	ProcessElementEditor elements(Predicate<? super ProcessElement> filter);
	
	NodeEditor node(Object state);
	
	NodeEditor nodes();

	NodeEditor nodes(Object... states);
	
	NodeEditor nodes(Predicate<? super Node> filter);
	
	StandardNodeEditor standardNode(Object state);
	
	StandardNodeEditor standardNodes();
	
	StandardNodeEditor standardNodes(Object... states);
	
	StandardNodeEditor standardNodes(Predicate<? super Node> filter);
	
	EdgeEditor edge(String name);
	
	EdgeEditor edge(Object fromState, Object toState);
	
	EdgeEditor edgesFromState(Object fromState);

	EdgeEditor edgesToState(Object toState);
	
	EdgeEditor edges(Predicate<? super Edge> filter);
	
	EdgeEditor edges();
	
	ConditionalEdgeEditor conditionalEdge(String name);
	
	ConditionalEdgeEditor conditionalEdges(Object fromState, Object toState);
	
	ConditionalEdgeEditor conditionalEdgesFromState(Object fromState);
	
	ConditionalEdgeEditor conditionalEdgesToState(Object toState);
	
	ConditionalEdgeEditor conditionalEdges(Predicate<? super ConditionalEdge> filter);
	
	ConditionalEdgeEditor conditionalEdges();
	
	static ProcessDefinitionExpertEditor of(ProcessDefinition processDefinition) {
		return new BasicProcessDefinitionExpertEditor(processDefinition);
	}
}
