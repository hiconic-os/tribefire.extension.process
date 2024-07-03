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
package tribefire.pd.editor.api;

import java.util.stream.Stream;

import com.braintribe.model.generic.session.GmSession;

import tribefire.extension.process.model.deployment.ConditionalEdge;
import tribefire.extension.process.model.deployment.Edge;
import tribefire.extension.process.model.deployment.ProcessDefinition;
import tribefire.extension.process.model.deployment.StandardNode;
import tribefire.pd.editor.impl.ProcessDefinitionEditorImpl;

public interface ProcessDefinitionEditor {
	ProcessDefinition definition();
	
	default StandardNode rootNode() { return acquireNode((String)null); }
	default StandardNode acquireNode(Enum<?> state) {  return acquireNode(state.name()); }
	StandardNode acquireNode(String state);
	StandardNode node(String state, String name);
	default StandardNode node(Enum<?> state, String name) { return node(state.name(), name); }
	
	Edge edge(String from, String to, String name);
	default Edge edge(Enum<?> from, Enum<?> to, String name) { return edge(from.name(), to.name(), name); }
	default Edge rootEdge(Enum<?> to, String name) { return edge(null, to.name(), name); }
	default Edge rootEdge(String to, String name) { return edge(null, to, name); }
	
	ConditionalEdge conditionalEdge(String from, String to, String name);
	default ConditionalEdge conditionalEdge(Enum<?> from, Enum<?> to, String name) { return conditionalEdge(from.name(), to.name(), name); }
	default ConditionalEdge conditionalRootEdge(Enum<?> to, String name) { return conditionalEdge(null, to.name(), name); }
	default ConditionalEdge conditionalRootEdge(String to, String name) { return conditionalEdge(null, to, name); }
	
	void errorEdge(String from, String to);
	default void errorEdge(Enum<?> from, Enum<?> to) { errorEdge(from.name(), to.name()); }
	void overdueEdge(String from, String to);
	default void overdueEdge(Enum<?> from, Enum<?> to) { overdueEdge(from.name(), to.name()); }
	
	Stream<StandardNode> acquireNodes(Enum<?>... enumConstants);
	Stream<StandardNode> acquireNodes(String... states);
	
	static ProcessDefinitionEditor create(GmSession session) {
		return new ProcessDefinitionEditorImpl(session);
	}
	
	static ProcessDefinitionEditor create() {
		return new ProcessDefinitionEditorImpl();
	}
}
