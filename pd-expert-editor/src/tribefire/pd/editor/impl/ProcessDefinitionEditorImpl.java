// ============================================================================
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
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
