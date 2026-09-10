package tribefire.extension.process.rx.processing.oracle;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import com.braintribe.model.time.TimeSpan;

import tribefire.extension.process.data.model.ProcessItem;
import tribefire.extension.process.data.model.state.TransitionPhase;
import tribefire.extension.process.model.configuration.Edge;
import tribefire.extension.process.model.configuration.Node;
import tribefire.extension.process.model.configuration.ProcessDefinition;
import tribefire.extension.process.model.configuration.TransitionProcessorReference;

public class TransitionOracle {
	private final ProcessDefinition definition;
	private final Node fromNode;
	private final Node toNode;
	private final Edge edge;
	private final ProcessOracle processOracle;
	private List<TransitionProcessorReference> resolvedProcessors;

	public TransitionOracle(ProcessOracle processOracle, ProcessDefinition definition, Node fromNode, Node toNode) {
		this.processOracle = processOracle;
		this.definition = definition;
		this.fromNode = fromNode;
		this.toNode = toNode;
		this.edge = null;
	}

	/** The transition along the given edge, which the given node holds. */
	public TransitionOracle(ProcessOracle processOracle, ProcessDefinition definition, Node fromNode, Edge edge) {
		this.processOracle = processOracle;
		this.definition = definition;
		this.fromNode = fromNode;
		this.toNode = edge.getTo();
		this.edge = edge;
	}

	public String getFromState() { return fromNode.getState(); }
	public String getToState() { return toNode.getState(); }
	public Node getFrom() { return fromNode; }
	public Node getTo() { return toNode; }
	public Edge getEdge() { return edge; }

	/** TODO: unused, {@code ProcessOracle.drainNodes} is read directly. Either use it, or remove it. */
	public boolean isTerminal() { return processOracle.isTerminal(toNode); }

	/**
	 * TODO: unused, and so are the {@code errorNode} properties it reads. The engine only halts a failed process. Either
	 * implement the error routing, or remove this method and both properties.
	 */
	public Node getErrorNode() {
		return toNode.getErrorNode() != null ? toNode.getErrorNode() : definition.getErrorNode();
	}

	public void initTransition(ProcessItem processItem) {
		processItem.setPreviousState(processItem.getState());
		processItem.setState(toNode.getState());
		processItem.setLastTransit(new Date());
		processItem.setTransitionPhase(TransitionPhase.CHANGED_STATE);
		processItem.setTransitionProcessorId(null);
		processItem.setNextState(null);
		processItem.setOverdueAt(determineOverdue());
	}

	private Date determineOverdue() {
		if (toNode.getDecoupledInteraction() == null)
			return null;
		TimeSpan gracePeriod = toNode.getGracePeriod() != null ? toNode.getGracePeriod() : definition.getGracePeriod();
		return gracePeriod == null ? null : new Date(System.currentTimeMillis() + gracePeriod.toLongMillies());
	}

	public Iterator<TransitionProcessorReference> getSuccessiveTransitionProcessors(String processorId) {
		Iterator<TransitionProcessorReference> iterator = getTransitionProcessors().iterator();
		if (processorId == null)
			return iterator;
		while (iterator.hasNext())
			if (processorId.equals(iterator.next().getProcessorId()))
				break;
		return iterator;
	}

	public TransitionProcessorReference getPredecessorTransitionProcessor(String processorId) {
		List<TransitionProcessorReference> processors = getTransitionProcessors();
		for (int i = 0; i < processors.size(); i++)
			if (processorId.equals(processors.get(i).getProcessorId()))
				return i == 0 ? null : processors.get(i - 1);
		return null;
	}

	public List<TransitionProcessorReference> getTransitionProcessors() {
		if (resolvedProcessors == null) {
			resolvedProcessors = new ArrayList<>();
			if (edge != null) {
				resolvedProcessors.addAll(definition.getOnTransit());
				resolvedProcessors.addAll(fromNode.getOnLeft());
				resolvedProcessors.addAll(edge.getOnTransit());
			}
			resolvedProcessors.addAll(toNode.getOnEntered());
		}
		return resolvedProcessors;
	}

	public List<TransitionProcessorReference> getErrorHandlers() {
		return Stream.concat(toNode.getOnError().stream(), definition.getOnError().stream()).toList();
	}
}
