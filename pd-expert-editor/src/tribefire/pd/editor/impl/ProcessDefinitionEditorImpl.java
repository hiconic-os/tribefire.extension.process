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
package tribefire.pd.editor.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import com.braintribe.common.lcd.Pair;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.session.GmSession;

import tribefire.extension.process.model.deployment.ConditionalEdge;
import tribefire.extension.process.model.deployment.Edge;
import tribefire.extension.process.model.deployment.ProcessDefinition;
import tribefire.extension.process.model.deployment.StandardNode;
import tribefire.pd.editor.api.ProcessDefinitionEditor;

public class ProcessDefinitionEditorImpl implements ProcessDefinitionEditor {
	private GmSession session;
	
	private Map<String, StandardNode> nodes = new HashMap<>();
	private Map<Pair<String, String>, Edge> edges = new HashMap<>();
	private Map<ConditionalEdgeKey, ConditionalEdge> conditionalEdges = new HashMap<>();

	private ProcessDefinition definition;
	
	public ProcessDefinitionEditorImpl(GmSession session) {
		this.session = session;
		this.definition = create(ProcessDefinition.T);
	}
	
	public ProcessDefinitionEditorImpl() {
		this(null);
	}
	
	
	private static class ConditionalEdgeKey {
		String from;
		String to;
		String name;
		
		public ConditionalEdgeKey(String from, String to, String name) {
			super();
			this.from = from;
			this.to = to;
			this.name = name;
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(from, name, to);
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ConditionalEdgeKey other = (ConditionalEdgeKey) obj;
			return Objects.equals(from, other.from) && Objects.equals(name, other.name) && Objects.equals(to, other.to);
		}
	}
	

	@Override
	public ProcessDefinition definition() {
		return definition;
	}

	@Override
	public StandardNode node(String state, String name) {
		StandardNode node = acquireNode(state);
		node.setName(LocalizedString.create(name));
		return node;
	}

	@Override
	public Edge edge(String from, String to, String name) {
		return edges.computeIfAbsent(Pair.of(from, to), k -> {
			Edge edge = create(Edge.T);
			StandardNode fromNode = acquireNode(from);
			StandardNode toNode = acquireNode(to);
			
			edge.setFrom(fromNode);
			edge.setTo(toNode);
			edge.setName(LocalizedString.create(name));
			
			definition().getElements().add(edge);
			
			return edge;
		});
	}

	@Override
	public ConditionalEdge conditionalEdge(String from, String to, String name) {
		return conditionalEdges.computeIfAbsent(new ConditionalEdgeKey(from, to, name), k -> {
			ConditionalEdge edge = create(ConditionalEdge.T);
			StandardNode fromNode = acquireNode(from);
			StandardNode toNode = acquireNode(to);
			
			edge.setFrom(fromNode);
			edge.setTo(toNode);
			edge.setName(LocalizedString.create(name));
			
			definition().getElements().add(edge);
			
			fromNode.getConditionalEdges().add(edge);
			
			return edge;
		});
	}
	
	public StandardNode acquireNode(String state) {
		return nodes.computeIfAbsent(state, k -> {
			StandardNode standardNode = create(StandardNode.T);
			standardNode.setState(state);
			definition().getElements().add(standardNode);
			return standardNode;
		});
	}

	@Override
	public void errorEdge(String from, String to) {
		acquireNode(from).setErrorNode(acquireNode(to));
	}

	@Override
	public void overdueEdge(String from, String to) {
		acquireNode(from).setOverdueNode(acquireNode(to));
	}
	
	private <T extends GenericEntity> T create(EntityType<T> type) {
		return session != null? session.create(type): type.create();
	}
	
	@Override
	public Stream<StandardNode> acquireNodes(String... states) {
		return Stream.of(states).map(this::acquireNode);
	}
	
	@Override
	public Stream<StandardNode> acquireNodes(Enum<?>... enumConstants) {
		return Stream.of(enumConstants).map(this::acquireNode);
	}
}
