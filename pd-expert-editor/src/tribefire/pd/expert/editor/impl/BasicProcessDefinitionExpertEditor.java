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
package tribefire.pd.expert.editor.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.braintribe.common.lcd.Pair;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.utils.collection.api.MultiMap;
import com.braintribe.utils.collection.impl.HashMultiMap;

import tribefire.extension.process.model.deployment.ConditionalEdge;
import tribefire.extension.process.model.deployment.Edge;
import tribefire.extension.process.model.deployment.Node;
import tribefire.extension.process.model.deployment.ProcessDefinition;
import tribefire.extension.process.model.deployment.ProcessElement;
import tribefire.extension.process.model.deployment.StandardNode;
import tribefire.pd.expert.editor.api.ConditionalEdgeEditor;
import tribefire.pd.expert.editor.api.EdgeEditor;
import tribefire.pd.expert.editor.api.NodeEditor;
import tribefire.pd.expert.editor.api.ProcessDefinitionExpertEditor;
import tribefire.pd.expert.editor.api.ProcessElementEditor;
import tribefire.pd.expert.editor.api.StandardNodeEditor;

public class BasicProcessDefinitionExpertEditor implements ProcessDefinitionExpertEditor {
	private ProcessDefinition processDefinition;

	private List<Node> nodes;
	private Map<Object, Node> nodeByState;
	private Map<Object, StandardNode> standardNodeByState;
	
	private List<Edge> edges; 
	private List<ConditionalEdge> conditionalEdges; 
	private List<StandardNode> standardNodes; 
	private MultiMap<Pair<Object, Object>, Edge> edgesByConnection;
	private Map<String, Edge> edgeByName;
	private MultiMap<Object, Edge> edgesByTo;
	private MultiMap<Object, Edge> edgesByFrom;

	private MultiMap<Pair<Object, Object>, ConditionalEdge> conditionalEdgesByConnection;
	private Map<String, ConditionalEdge> conditionalEdgeByName;
	private MultiMap<Object, ConditionalEdge> conditionalEdgesByTo;
	private MultiMap<Object, ConditionalEdge> conditionalEdgesByFrom;
	
	
	public List<Node> getNodes() {
		if (nodes == null) {
			nodes = processDefinition.getElements().stream() //
				.filter(e -> e instanceof Node) //
				.map(e -> (Node)e) //
				.collect(Collectors.toList());
		}

		return nodes;
	}
	
	public List<StandardNode> getStandardNodes() {
		if (standardNodes == null) {
			standardNodes = processDefinition.getElements().stream() //
					.filter(e -> e instanceof StandardNode) //
					.map(e -> (StandardNode)e) //
					.collect(Collectors.toList());
		}
		
		return standardNodes;
	}
	
	public List<Edge> getEdges() {
		if (edges == null) {
			edges = processDefinition.getElements().stream() //
					.filter(e -> e instanceof Edge) //
					.map(e -> (Edge)e) //
					.collect(Collectors.toList());
		}

		return edges;
	}
	
	public List<ConditionalEdge> getConditionalEdges() {
		if (conditionalEdges == null) {
			conditionalEdges = processDefinition.getElements().stream() //
					.filter(e -> e instanceof ConditionalEdge) //
					.map(e -> (ConditionalEdge)e) //
					.collect(Collectors.toList());
		}
		
		return conditionalEdges;
	}
	
	
	
	public Map<Object, Node> getNodeByState() {
		if (nodeByState == null) {
			nodeByState = getNodes().stream() //
					.collect(Collectors.toMap(Node::getState, Function.identity()));
		}

		return nodeByState;
	}
	
	public Map<Object, StandardNode> getStandardNodeByState() {
		if (standardNodeByState == null) {
			standardNodeByState = getStandardNodes().stream() //
					.collect(Collectors.toMap(Node::getState, Function.identity()));
		}
		
		return standardNodeByState;
	}
	
	public Map<String, Edge> getEdgeByName() {
		if (edgeByName == null) {
			edgeByName = getEdges().stream() //
					.collect(Collectors.toMap(BasicProcessDefinitionExpertEditor::getName, Function.identity()));
		}

		return edgeByName;
	}
	
	public Map<String, ConditionalEdge> getConditionalEdgeByName() {
		if (conditionalEdgeByName == null) {
			conditionalEdgeByName = getConditionalEdges().stream() //
					.collect(Collectors.toMap(BasicProcessDefinitionExpertEditor::getName, Function.identity()));
		}

		return conditionalEdgeByName;
	}

	
	private static String getName(Edge edge) {
		return edge.getName().value(LocalizedString.LOCALE_DEFAULT);
	}
	
	public MultiMap<Pair<Object, Object>, Edge> getEdgesByConnection() {
		if (edgesByConnection == null) {
			edgesByConnection = new HashMultiMap<>();
			
			for (Edge edge: getEdges()) {
				StandardNode from = edge.getFrom();
				Node to = edge.getTo();
				
				if (from != null && to != null)
					edgesByConnection.put(Pair.of(from.getState(), to.getState()), edge);

			}
		}

		return edgesByConnection;
	}
	
	public MultiMap<Pair<Object, Object>, ConditionalEdge> getConditionalEdgesByConnection() {
		if (conditionalEdgesByConnection == null) {
			conditionalEdgesByConnection = new HashMultiMap<>();
			
			for (ConditionalEdge edge: getConditionalEdges()) {
				StandardNode from = edge.getFrom();
				Node to = edge.getTo();
				
				if (from != null && to != null)
					conditionalEdgesByConnection.put(Pair.of(from.getState(), to.getState()), edge);
				
			}
		}
		
		return conditionalEdgesByConnection;
	}
	
	public MultiMap<Object, Edge> getEdgesByFrom() {
		if (edgesByFrom == null) {
			edgesByFrom = new HashMultiMap<>();
			
			for (Edge edge: getEdges()) {
				StandardNode from = edge.getFrom();
				
				if (from != null)
					edgesByFrom.put(from.getState(), edge);

			}
		}
		return edgesByFrom;
	}
	
	public MultiMap<Object, Edge> getEdgesByTo() {
		if (edgesByTo == null) {
			edgesByTo = new HashMultiMap<>();
			
			for (Edge edge: getEdges()) {
				Node to = edge.getTo();
				
				if (to != null)
					edgesByTo.put(to.getState(), edge);

			}
		}
		return edgesByTo;
	}
	
	public MultiMap<Object, ConditionalEdge> getConditionalEdgesByFrom() {
		if (conditionalEdgesByFrom == null) {
			conditionalEdgesByFrom = new HashMultiMap<>();
			
			for (ConditionalEdge edge: getConditionalEdges()) {
				StandardNode from = edge.getFrom();
				
				if (from != null)
					conditionalEdgesByFrom.put(from.getState(), edge);
				
			}
		}
		return conditionalEdgesByFrom;
	}
	
	public MultiMap<Object, ConditionalEdge> getConditionalEdgesByTo() {
		if (conditionalEdgesByTo == null) {
			conditionalEdgesByTo = new HashMultiMap<>();
			
			for (ConditionalEdge edge: getConditionalEdges()) {
				Node to = edge.getTo();
				
				if (to != null)
					conditionalEdgesByTo.put(to.getState(), edge);
				
			}
		}
		return conditionalEdgesByTo;
	}
	
	
	public BasicProcessDefinitionExpertEditor(ProcessDefinition processDefinition) {
		this.processDefinition = processDefinition;
	}

	@Override
	public ProcessDefinition definition() {
		return processDefinition;
	}

	@Override
	public ProcessElementEditor elements() {
		return elements(e -> true);
	}

	@Override
	public ProcessElementEditor elements(Predicate<? super ProcessElement> filter) {
		return new BasicProcessElementEditor(definition().getElements().stream().filter(filter).collect(Collectors.toList()));
	}

	@Override
	public NodeEditor node(Object state) {
		return new BasicNodeEditor(getNodeByState().get(state));
	}

	@Override
	public NodeEditor nodes() {
		return new BasicNodeEditor(getNodes());
	}
	
	@Override
	public NodeEditor nodes(Object... states) {
		List<Node> selectedNodes = Stream.of(states).map(getNodeByState()::get).collect(Collectors.toList());
		return new BasicNodeEditor(selectedNodes);
	}

	@Override
	public NodeEditor nodes(Predicate<? super Node> filter) {
		List<Node> selectedNodes = getNodes().stream().filter(filter).collect(Collectors.toList());
		return new BasicNodeEditor(selectedNodes);
	}
	@Override
	
	public StandardNodeEditor standardNode(Object state) {
		return new BasicStandardNodeEditor(getStandardNodeByState().get(state));
	}
	
	@Override
	public StandardNodeEditor standardNodes() {
		return new BasicStandardNodeEditor(getStandardNodes());
	}
	
	@Override
	public StandardNodeEditor standardNodes(Object... states) {
		List<StandardNode> selectedNodes = Stream.of(states).map(getStandardNodeByState()::get).collect(Collectors.toList());
		return new BasicStandardNodeEditor(selectedNodes);
	}
	
	@Override
	public StandardNodeEditor standardNodes(Predicate<? super Node> filter) {
		List<StandardNode> selectedNodes = getStandardNodes().stream().filter(filter).collect(Collectors.toList());
		return new BasicStandardNodeEditor(selectedNodes);
	}
	
	@Override
	public EdgeEditor edge(String name) {
		return new BasicEdgeEditor(getEdgeByName().get(name));
	}

	@Override
	public EdgeEditor edge(Object fromState, Object toState) {
		List<Edge> selectedEdges = new ArrayList<>(getEdgesByConnection().getAll(Pair.of(fromState, toState)));
		return new BasicEdgeEditor(selectedEdges);
	}

	@Override
	public EdgeEditor edgesFromState(Object fromState) {
		List<Edge> selectedEdges = new ArrayList<>(getEdgesByFrom().getAll(fromState));
		return new BasicEdgeEditor(selectedEdges);
	}

	@Override
	public EdgeEditor edgesToState(Object toState) {
		List<Edge> selectedEdges = new ArrayList<>(getEdgesByTo().getAll(toState));
		return new BasicEdgeEditor(selectedEdges);
	}

	@Override
	public EdgeEditor edges(Predicate<? super Edge> filter) {
		List<Edge> selectedEdges = getEdges().stream().filter(filter).collect(Collectors.toList());
		return new BasicEdgeEditor(selectedEdges);
	}

	@Override
	public EdgeEditor edges() {
		return new BasicEdgeEditor(getEdges());
	}
	
	@Override
	public ConditionalEdgeEditor conditionalEdge(String name) {
		return new BasicConditionalEdgeEditor(getConditionalEdgeByName().get(name));
	}
	
	@Override
	public ConditionalEdgeEditor conditionalEdges() {
		return new BasicConditionalEdgeEditor(getConditionalEdges());
	}
	
	@Override
	public ConditionalEdgeEditor conditionalEdges(Object fromState, Object toState) {
		List<ConditionalEdge> selectedEdges = new ArrayList<>(getConditionalEdgesByConnection().getAll(Pair.of(fromState, toState)));
		return new BasicConditionalEdgeEditor(selectedEdges);
	}
	
	@Override
	public ConditionalEdgeEditor conditionalEdges(Predicate<? super ConditionalEdge> filter) {
		List<ConditionalEdge> selectedEdges = getConditionalEdges().stream().filter(filter).collect(Collectors.toList());
		return new BasicConditionalEdgeEditor(selectedEdges);
	}
	
	@Override
	public ConditionalEdgeEditor conditionalEdgesFromState(Object fromState) {
		List<ConditionalEdge> selectedEdges = new ArrayList<>(getConditionalEdgesByFrom().getAll(fromState));
		return new BasicConditionalEdgeEditor(selectedEdges);
	}

	@Override
	public ConditionalEdgeEditor conditionalEdgesToState(Object toState) {
		List<ConditionalEdge> selectedEdges = new ArrayList<>(getConditionalEdgesByTo().getAll(toState));
		return new BasicConditionalEdgeEditor(selectedEdges);
	}
}
