package tribefire.extension.process.model.configuration;

import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.time.TimeSpan;

/**
 * One state of a process, together with the state changes that are allowed from it ({@link #getEdges()}) and the transition
 * processors that run when the process enters or leaves it.
 * <p>
 * Two nodes have a special role:
 * <ul>
 * <li>the <b>root node</b> is the node whose {@link #getState() state} is {@code null}. A process starts there, and its
 * {@link #getEdges() edges} decide where the process enters the graph. Like any other node it may carry
 * {@link #getOnEntered() onEntered} processors and a {@link #getDecoupledInteraction() decoupled interaction}.
 * <li>a <b>drain node</b> is a node without edges. The process ends when it enters such a node.
 * </ul>
 *
 * @see ProcessDefinition
 */
@SelectiveInformation("node: ${state}")
public interface Node extends GenericEntity {
	EntityType<Node> T = EntityTypes.T(Node.class);

	String state = "state";
	String name = "name";
	String description = "description";
	String edges = "edges";
	String decoupledInteraction = "decoupledInteraction";
	String gracePeriod = "gracePeriod";
	String errorNode = "errorNode";
	String overdueNode = "overdueNode";
	String onEntered = "onEntered";
	String onLeft = "onLeft";
	String onError = "onError";

	/**
	 * The state that a process has while it is in this node, i.e. the value of {@code ProcessItem.state}. Usually the name of
	 * an enum constant. {@code null} marks the root node.
	 */
	String getState();
	void setState(String state);

	/** Human readable name of this node, for a UI. Not an identifier - {@link #getState()} identifies a node. */
	String getName();
	void setName(String name);

	String getDescription();
	void setDescription(String description);

	/**
	 * The state changes that are allowed from this node, in evaluation order.
	 * <p>
	 * The order is relevant among the edges that have a {@link Edge#getCondition() condition}, because the first matching
	 * condition wins. The position of an edge without a condition does not matter, as such an edge is only considered when no
	 * condition matched. An empty list makes this node a drain node, which ends the process.
	 */
	List<Edge> getEdges();
	void setEdges(List<Edge> edges);

	/**
	 * If set, the process stops in this node and waits, instead of routing on. Something outside the engine is then
	 * responsible, and it hands the process back with {@code ResumeProcess} or {@code ResumeProcessToState}.
	 */
	DecoupledInteraction getDecoupledInteraction();
	void setDecoupledInteraction(DecoupledInteraction decoupledInteraction);

	/**
	 * How long this node may wait for its {@link #getDecoupledInteraction() decoupled interaction}. When a process enters
	 * this node, the engine turns the period into a deadline on the process, and it resumes the process by itself once that
	 * deadline passed - over {@link #getOverdueNode()} if one is given, else by normal routing.
	 * <p>
	 * Without a decoupled interaction a grace period has no effect, as the process does not wait here. Falls back to
	 * {@link ProcessDefinition#getGracePeriod()}.
	 * <p>
	 * Note that a passed deadline is noticed by the search that
	 * {@link ProcessDefinitionsConfiguration#getMonitoredAccessIds() monitored accesses} describes. In an access that is not
	 * monitored, a process waits for its interaction forever.
	 */
	TimeSpan getGracePeriod();
	void setGracePeriod(TimeSpan gracePeriod);

	/**
	 * TODO: the RX engine does not evaluate this property. On an error it halts the process and runs
	 * {@link #getOnError()}. Either implement the routing to this node, or remove the property.
	 */
	Node getErrorNode();
	void setErrorNode(Node errorNode);

	/**
	 * Where the process continues when it waited in this node longer than its {@link #getGracePeriod() grace period}.
	 * Without an overdue node the process continues by normal routing.
	 * <p>
	 * This node must have an edge to it, as this is a state change like any other. The engine does not create one, and a
	 * process that becomes overdue without it halts.
	 */
	Node getOverdueNode();
	void setOverdueNode(Node overdueNode);

	/**
	 * Transition processors that run when the process entered this node, over any edge. They run last within a transition,
	 * after the processors of the definition, of the left node and of the edge.
	 */
	List<TransitionProcessorReference> getOnEntered();
	void setOnEntered(List<TransitionProcessorReference> onEntered);

	/**
	 * Transition processors that run when the process leaves this node, over any edge.
	 * <p>
	 * Note that they run <b>after</b> the state already changed to the entered node, because the engine runs all processors
	 * of a transition once the state change is recorded.
	 */
	List<TransitionProcessorReference> getOnLeft();
	void setOnLeft(List<TransitionProcessorReference> onLeft);

	/**
	 * Transition processors that run when the process halted because of an error in this node. They run in addition to
	 * {@link ProcessDefinition#getOnError()}, and they cannot prevent the halt.
	 */
	List<TransitionProcessorReference> getOnError();
	void setOnError(List<TransitionProcessorReference> onError);
}
