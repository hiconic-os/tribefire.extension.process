package tribefire.extension.process.rx.processing.oracle;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import com.braintribe.common.lcd.Pair;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reasons;

import tribefire.extension.process.model.configuration.Edge;
import tribefire.extension.process.model.configuration.Node;
import tribefire.extension.process.model.configuration.ProcessDefinition;
import tribefire.extension.process.reason.model.EdgeNotFound;
import tribefire.extension.process.reason.model.NodeNotFound;

/** Indexes one {@link ProcessDefinition} for the engine. The graph is the nodes and the edges each node holds. */
public class ProcessOracle {
	public final ProcessDefinition processDefinition;
	public final Map<String, Node> nodeByState = new HashMap<>();
	public final Map<Pair<String, String>, Edge> edgesByStateChange = new HashMap<>();
	public final Map<String, List<Edge>> outgoingEdgesByState = new HashMap<>();
	public final Set<Node> drainNodes = new HashSet<>();

	public ProcessOracle(ProcessDefinition definition) {
		this.processDefinition = definition;

		for (Node node : definition.getNodes()) {
			if (nodeByState.put(node.getState(), node) != null)
				throw new IllegalArgumentException("Duplicate node state in process '" + definition.getName() + "': " + node.getState());
		}

		for (Node node : definition.getNodes()) {
			String fromState = node.getState();
			List<Edge> edges = node.getEdges();

			if (edges.isEmpty()) {
				drainNodes.add(node);
				continue;
			}

			outgoingEdgesByState.put(fromState, edges);

			for (Edge edge : edges)
				edgesByStateChange.put(Pair.of(fromState, edge.getTo().getState()), edge);
		}
	}

	public boolean hasState(String state) {
		return nodeByState.containsKey(state);
	}

	public boolean hasEdge(String fromState, String toState) {
		return edgesByStateChange.containsKey(Pair.of(fromState, toState));
	}

	/** The outgoing edges of the given state, in evaluation order. */
	public List<Edge> outgoingEdges(String state) {
		return outgoingEdgesByState.getOrDefault(state, List.of());
	}

	/** TODO: unused. Either use it in {@code StartProcessProcessor}, or remove it. */
	public Maybe<Edge> getInitialDefaultEdge() {
		if (!nodeByState.containsKey(null))
			return Reasons.build(NodeNotFound.T).text("Initial process node not found").toMaybe();
		List<Edge> defaults = outgoingEdges(null).stream().filter(edge -> edge.getCondition() == null).toList();
		return switch (defaults.size()) {
			case 0 -> Reasons.build(EdgeNotFound.T).text("No default edge found on initial node").toMaybe();
			case 1 -> Maybe.complete(defaults.get(0));
			default -> Reasons.build(EdgeNotFound.T).text("Ambiguous default edges on initial node").toMaybe();
		};
	}

	public TransitionOracle transitionOracle(String fromState, String toState) {
		return transitionOracle(fromState, toState, false);
	}

	public TransitionOracle transitionOracle(String fromState, String toState, boolean allowImplicitTransition) {
		Edge edge = edgesByStateChange.get(Pair.of(fromState, toState));
		if (edge != null)
			return new TransitionOracle(this, processDefinition, nodeByState.get(fromState), edge);
		if (allowImplicitTransition)
			return new TransitionOracle(this, processDefinition, nodeByState.get(fromState), nodeByState.get(toState));
		throw new NoSuchElementException("No edge found from state " + fromState + " to state " + toState);
	}

	/** TODO: unused, {@link #drainNodes} is read directly. Either use it, or remove it. */
	public boolean isTerminal(Node node) {
		return drainNodes.contains(node);
	}
}
