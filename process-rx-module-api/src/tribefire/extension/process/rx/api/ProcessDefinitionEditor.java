package tribefire.extension.process.rx.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import tribefire.extension.process.model.configuration.ConditionProcessorReference;
import tribefire.extension.process.model.configuration.DecoupledInteraction;
import tribefire.extension.process.model.configuration.Edge;
import tribefire.extension.process.model.configuration.Node;
import tribefire.extension.process.model.configuration.ProcessDefinition;
import tribefire.extension.process.model.configuration.TransitionProcessorReference;

/** Session-free editor for RX process definitions. Nodes are identified by state and edges by name. */
public final class ProcessDefinitionEditor {

	private final ProcessDefinition definition;
	private final Map<String, Node> nodesByState = new HashMap<>();
	private final Map<String, Edge> edgesByName = new HashMap<>();

	private ProcessDefinitionEditor(ProcessDefinition definition) {
		this.definition = Objects.requireNonNull(definition, "definition");
		definition.getNodes().forEach(this::indexNode);
		definition.getEdges().forEach(this::indexEdge);
	}

	public static ProcessDefinitionEditor create(String name) {
		ProcessDefinition definition = ProcessDefinition.T.create();
		definition.setName(name);
		return new ProcessDefinitionEditor(definition);
	}

	public static ProcessDefinitionEditor edit(ProcessDefinition definition) {
		return new ProcessDefinitionEditor(definition);
	}

	public ProcessDefinition definition() {
		return definition;
	}

	public Node rootNode() {
		return acquireNode((String) null);
	}

	public Node acquireNode(Enum<?> state) {
		return acquireNode(state.name());
	}

	public Node acquireNode(String state) {
		return nodesByState.computeIfAbsent(state, key -> {
			Node node = Node.T.create();
			node.setState(key);
			definition.getNodes().add(node);
			return node;
		});
	}

	public Node node(String state, String name) {
		Node node = acquireNode(state);
		node.setName(name);
		return node;
	}

	public Node requireNode(String state) {
		Node node = nodesByState.get(state);
		if (node == null)
			throw new IllegalArgumentException("Unknown process node state: " + state);
		return node;
	}

	public Edge edge(String fromState, String toState, String name) {
		Edge existing = edgesByName.get(name);
		if (existing != null) {
			assertEndpoints(existing, fromState, toState);
			return existing;
		}

		Edge edge = Edge.T.create();
		edge.setName(requireText(name, "edge name"));
		edge.setFrom(acquireNode(fromState));
		edge.setTo(acquireNode(toState));
		definition.getEdges().add(edge);
		edgesByName.put(name, edge);
		return edge;
	}

	public Edge rootEdge(String toState, String name) {
		return edge(null, toState, name);
	}

	public Edge conditionedEdge(String fromState, String toState, String name, String conditionProcessorId) {
		Edge edge = edge(fromState, toState, name);
		edge.setCondition(conditionProcessor(conditionProcessorId));
		return edge;
	}

	public Edge requireEdge(String name) {
		Edge edge = edgesByName.get(name);
		if (edge == null)
			throw new IllegalArgumentException("Unknown process edge: " + name);
		return edge;
	}

	public void removeEdge(String name) {
		Edge edge = edgesByName.remove(name);
		if (edge == null)
			throw new IllegalArgumentException("Unknown process edge: " + name);
		definition.getEdges().remove(edge);
	}

	public ProcessDefinitionEditor errorNode(String state, String errorState) {
		acquireNode(state).setErrorNode(acquireNode(errorState));
		return this;
	}

	public ProcessDefinitionEditor overdueNode(String state, String overdueState) {
		acquireNode(state).setOverdueNode(acquireNode(overdueState));
		return this;
	}

	public DecoupledInteraction decoupledInteraction(String state, String name) {
		Node node = acquireNode(state);
		DecoupledInteraction interaction = node.getDecoupledInteraction();
		if (interaction == null) {
			interaction = DecoupledInteraction.T.create();
			node.setDecoupledInteraction(interaction);
		}
		interaction.setName(name);
		return interaction;
	}

	public ProcessDefinitionEditor onEnter(String state, String processorId) {
		acquireNode(state).getOnEntered().add(transitionProcessor(processorId));
		return this;
	}

	public ProcessDefinitionEditor onLeave(String state, String processorId) {
		acquireNode(state).getOnLeft().add(transitionProcessor(processorId));
		return this;
	}

	public ProcessDefinitionEditor onTransit(String edgeName, String processorId) {
		requireEdge(edgeName).getOnTransit().add(transitionProcessor(processorId));
		return this;
	}

	public static TransitionProcessorReference transitionProcessor(String processorId) {
		TransitionProcessorReference reference = TransitionProcessorReference.T.create();
		reference.setProcessorId(requireText(processorId, "transition processor id"));
		return reference;
	}

	public static ConditionProcessorReference conditionProcessor(String processorId) {
		ConditionProcessorReference reference = ConditionProcessorReference.T.create();
		reference.setProcessorId(requireText(processorId, "condition processor id"));
		return reference;
	}

	private void indexNode(Node node) {
		Node previous = nodesByState.putIfAbsent(node.getState(), node);
		if (previous != null)
			throw new IllegalArgumentException("Duplicate process node state: " + node.getState());
	}

	private void indexEdge(Edge edge) {
		String name = requireText(edge.getName(), "edge name");
		Edge previous = edgesByName.putIfAbsent(name, edge);
		if (previous != null)
			throw new IllegalArgumentException("Duplicate process edge name: " + name);
	}

	private static void assertEndpoints(Edge edge, String fromState, String toState) {
		if (!Objects.equals(edge.getFrom().getState(), fromState) || !Objects.equals(edge.getTo().getState(), toState))
			throw new IllegalArgumentException("Edge '" + edge.getName() + "' already exists with different endpoints");
	}

	private static String requireText(String value, String label) {
		if (value == null || value.isBlank())
			throw new IllegalArgumentException(label + " must not be blank");
		return value;
	}
}
