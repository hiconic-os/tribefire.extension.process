package tribefire.extension.process.rx.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import tribefire.extension.process.model.configuration.Condition;
import tribefire.extension.process.model.configuration.DecoupledInteraction;
import tribefire.extension.process.model.configuration.Edge;
import tribefire.extension.process.model.configuration.Node;
import tribefire.extension.process.model.configuration.ProcessDefinition;
import tribefire.extension.process.model.configuration.TransitionProcessorReference;

/**
 * Session-free editor for RX process definitions. Nodes are identified by state, edges by name, and an edge is created on
 * the node it leaves.
 * <p>
 * An edge is appended to {@link Node#getEdges()}, which is the evaluation order. Use the {@code prepend} variants when an
 * edge must be evaluated before the conditions that a node already has.
 */
public final class ProcessDefinitionEditor {

	private final ProcessDefinition definition;
	private final Map<String, Node> nodesByState = new HashMap<>();
	private final Map<String, Edge> edgesByName = new HashMap<>();
	/** The node that holds an edge, by edge name. Kept here rather than read from {@link Edge#getFrom()}, which is redundant. */
	private final Map<String, Node> edgeOwnersByName = new HashMap<>();

	private ProcessDefinitionEditor(ProcessDefinition definition) {
		this.definition = Objects.requireNonNull(definition, "definition");
		definition.getNodes().forEach(this::indexNode);
		definition.getNodes().forEach(node -> node.getEdges().forEach(edge -> indexEdge(node, edge)));
	}

	/** A new definition with the given id, using the id as its name. */
	public static ProcessDefinitionEditor create(String processDefinitionId) {
		return create(processDefinitionId, processDefinitionId);
	}

	public static ProcessDefinitionEditor create(String processDefinitionId, String name) {
		ProcessDefinition definition = ProcessDefinition.T.create();
		definition.setProcessDefinitionId(requireText(processDefinitionId, "process definition id"));
		definition.setName(requireText(name, "process definition name"));
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

	/** An edge from one state to another, appended to the edges of the source node. */
	public Edge edge(String fromState, String toState, String name) {
		return acquireEdge(fromState, toState, name, false);
	}

	/** Like {@link #edge(String, String, String)}, but evaluated before the edges that the source node already has. */
	public Edge prependEdge(String fromState, String toState, String name) {
		return acquireEdge(fromState, toState, name, true);
	}

	public Edge rootEdge(String toState, String name) {
		return edge(null, toState, name);
	}

	/** An edge that is taken when the condition processor with the given id matches. */
	public Edge conditionedEdge(String fromState, String toState, String name, String conditionProcessorId) {
		return conditionedEdge(fromState, toState, name, Condition.processor(conditionProcessorId));
	}

	public Edge conditionedEdge(String fromState, String toState, String name, Condition condition) {
		Edge edge = edge(fromState, toState, name);
		edge.setCondition(condition);
		return edge;
	}

	/**
	 * Like {@link #conditionedEdge(String, String, String, String)}, but evaluated before the conditions that the source
	 * node already has. This is how an extension gives its own condition priority over an existing graph.
	 */
	public Edge prependConditionedEdge(String fromState, String toState, String name, String conditionProcessorId) {
		return prependConditionedEdge(fromState, toState, name, Condition.processor(conditionProcessorId));
	}

	public Edge prependConditionedEdge(String fromState, String toState, String name, Condition condition) {
		Edge edge = prependEdge(fromState, toState, name);
		edge.setCondition(condition);
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
		edgeOwnersByName.remove(name).getEdges().remove(edge);
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

	private Edge acquireEdge(String fromState, String toState, String name, boolean prepend) {
		Edge existing = edgesByName.get(name);
		if (existing != null) {
			assertEndpoints(existing, fromState, toState);
			return existing;
		}

		Node fromNode = acquireNode(fromState);

		Edge edge = Edge.T.create();
		edge.setName(requireText(name, "edge name"));
		edge.setFrom(fromNode);
		edge.setTo(acquireNode(toState));

		if (prepend)
			fromNode.getEdges().add(0, edge);
		else
			fromNode.getEdges().add(edge);

		edgesByName.put(name, edge);
		edgeOwnersByName.put(name, fromNode);
		return edge;
	}

	private void indexNode(Node node) {
		Node previous = nodesByState.putIfAbsent(node.getState(), node);
		if (previous != null)
			throw new IllegalArgumentException("Duplicate process node state: " + node.getState());
	}

	private void indexEdge(Node owner, Edge edge) {
		String name = requireText(edge.getName(), "edge name");
		Edge previous = edgesByName.putIfAbsent(name, edge);
		if (previous != null)
			throw new IllegalArgumentException("Duplicate process edge name: " + name);
		edgeOwnersByName.put(name, owner);
	}

	private void assertEndpoints(Edge edge, String fromState, String toState) {
		Node owner = edgeOwnersByName.get(edge.getName());
		if (!Objects.equals(owner.getState(), fromState) || !Objects.equals(edge.getTo().getState(), toState))
			throw new IllegalArgumentException("Edge '" + edge.getName() + "' already exists with different endpoints");
	}

	private static String requireText(String value, String label) {
		if (value == null || value.isBlank())
			throw new IllegalArgumentException(label + " must not be blank");
		return value;
	}
}
