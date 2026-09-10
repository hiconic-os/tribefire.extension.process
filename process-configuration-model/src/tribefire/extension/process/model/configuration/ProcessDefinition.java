package tribefire.extension.process.model.configuration;

import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.time.TimeSpan;

/**
 * The state graph that the process engine follows for one kind of process. This type is the entry point of the process configuration model, so it
 * also describes the engine as a whole.
 *
 * <h4>What a process is</h4>
 *
 * A process is a (persisted) entity. It is an instance of a sub type of {@code ProcessItem}, and it carries the control properties of the state
 * machine: {@code state}, {@code previousState}, {@code nextState}, {@code activity} and {@code transitionPhase}. The engine moves that entity
 * through this graph and runs application code on the way.
 * <p>
 * Two properties follow from that:
 * <ul>
 * <li>a process outlives the application, so it may wait in one state for months
 * <li>it does not matter which instance of the application drives a process, so the engine also works in a cluster
 * </ul>
 * <p>
 * The engine writes a {@code ProcessLogEntry} for what it does - every state change, every processor it called, every condition it evaluated - which
 * gives a process an audit trail.
 *
 * <h4>How a process finds its definition</h4>
 *
 * The process definition for a given process is configured with {@code ManageProcessWith} metadata on its type, usually written as the annotation
 * {@code @ManageProcessWith("<processDefinitionId>")}. A sub type inherits it.
 * <p>
 * That metadata names a {@link #getProcessDefinitionId() processDefinitionId}, and the engine looks up the definition with that id in
 * {@link ProcessDefinitionsConfiguration}. Note that {@link #getName() name} is a display name, never an identifier.
 *
 * <h4>Nodes and edges</h4>
 *
 * Every state of the process is a {@link Node}, and every allowed state change is an {@link Edge} in {@link Node#getEdges()} of the node it leaves.
 * <b>Only a declared edge permits a state change.</b>
 * <p>
 * A process starts in the <b>root node</b>, the node with state {@code null}, and it ends when it reaches a <b>drain node</b>, a node without edges.
 *
 * <h4>How the engine routes</h4>
 *
 * A state change happens in two passes over {@link Node#getEdges()} of the current node:
 * <ol>
 * <li>Every edge that has a {@link Edge#getCondition() condition} is evaluated, in list order. The first matching condition wins.
 * <li>If no condition matched, the one edge without a condition is taken.
 * </ol>
 * So the list order is only relevant among conditions, and an edge without a condition acts as the default of its node, wherever it stands in the
 * list.
 * <p>
 * A node needs no default: conditions may well cover every case. But if none of them matched and the node has no edge without a condition, or has
 * more than one, the engine cannot decide. It then halts the process and logs {@code UNDETERMINED_NEXT_NODE}.
 * <p>
 * Both passes are skipped when a transition processor demanded a state with {@code TransitionProcessorContext.continueWithState(String)}. An edge to
 * that state must still exist, obviously.
 *
 * <h4>What runs on a state change</h4>
 *
 * After the state changed, the engine runs the transition processors of that transition in this order:
 * <ol>
 * <li>{@link #getOnTransit()} of this definition
 * <li>{@link Node#getOnLeft()} of the left node
 * <li>{@link Edge#getOnTransit()} of the taken edge
 * <li>{@link Node#getOnEntered()} of the entered node
 * </ol>
 * All of them run when the process is <b>already in the entered state</b>, {@code onLeft} included. The engine remembers how far it got through that
 * list, so a long chain of processors survives a restart of the application. A processor may influence the routing with {@code continueWithState},
 * and if several processors of one transition do so, the last call wins.
 *
 * <h4>Waiting, ending, failing</h4>
 *
 * <ul>
 * <li>A node with a {@link Node#getDecoupledInteraction() decoupled interaction} stops the process. Work then happens outside the engine, and that
 * party hands the process back with {@code ResumeProcess} or {@code ResumeProcessToState}. A {@link Node#getGracePeriod() grace period} turns the
 * waiting into a deadline.
 * <li>A drain node ends the process.
 * <li>A failing transition or condition processor halts the process, runs {@link Node#getOnError()} and {@link #getOnError()}, and leaves it for an
 * operator, who continues it with {@code RecoverProcess}.
 * </ul>
 *
 * <h4>Under the hood</h4>
 *
 * The engine does not run a process in one go. Whenever it took a process one step further - a state change, or one transition processor that ran -
 * it commits that step and sends itself a message which says: continue this process. Reading such a message is what triggers the next step.
 * <p>
 * Two things follow from that:
 * <ul>
 * <li>a request such as {@code StartProcess} or {@code ResumeProcess} returns once the process is on its way, not once it reached its end state
 * <li>the queue is shared by all instances of the application, so the next step of a process may be taken by any of them, and a lock on the process
 * keeps two instances from working on it at the same time
 * </ul>
 * After an interruption - a restart, a crash - the engine continues from the step it recorded on the process. The one case it does not repeat is a
 * transition processor that was interrupted while it ran: such a process halts, because the engine cannot know whether that processor is idempotent.
 *
 * @see Node
 * @see Edge
 * @see Condition
 * @see ProcessDefinitionsConfiguration
 */
@SelectiveInformation("${name}")
public interface ProcessDefinition extends GenericEntity {
	EntityType<ProcessDefinition> T = EntityTypes.T(ProcessDefinition.class);

	String processDefinitionId = "processDefinitionId";
	String name = "name";
	String nodes = "nodes";
	String errorNode = "errorNode";
	String gracePeriod = "gracePeriod";
	String onTransit = "onTransit";
	String onError = "onError";

	/**
	 * Identifies this definition. A process type refers to this id in its {@code ManageProcessWith} metadata, and an extension addresses the
	 * definition by this id to change it. Unique within {@link ProcessDefinitionsConfiguration#getDefinitions()}.
	 */
	@Mandatory
	String getProcessDefinitionId();
	void setProcessDefinitionId(String processDefinitionId);

	/** Human readable name of this definition, for a UI. Not an identifier, see {@link #getProcessDefinitionId()}. */
	@Mandatory
	String getName();
	void setName(String name);

	/** All states of this graph, including the root node with state {@code null}. Each node holds its own outgoing edges. */
	List<Node> getNodes();
	void setNodes(List<Node> nodes);

	/**
	 * TODO: the RX engine does not evaluate this property. On an error it halts the process and runs {@link #getOnError()}. Either implement the
	 * routing to this node, or remove the property.
	 */
	Node getErrorNode();
	void setErrorNode(Node errorNode);

	/** The grace period that a waiting node uses when it declares none itself. See {@link Node#getGracePeriod()}. */
	TimeSpan getGracePeriod();
	void setGracePeriod(TimeSpan gracePeriod);

	/**
	 * Transition processors that run on every state change of this process, and first of all four processor lists.
	 * <p>
	 * They do not run when a process starts, because starting is not yet a state change over an edge. Only the {@link Node#getOnEntered() onEntered}
	 * processors of the root node run there.
	 */
	List<TransitionProcessorReference> getOnTransit();
	void setOnTransit(List<TransitionProcessorReference> onTransit);

	/**
	 * Transition processors that run whenever a process of this definition halts because of an error, after {@link Node#getOnError()} of the state it
	 * was in. They cannot prevent the halt.
	 */
	List<TransitionProcessorReference> getOnError();
	void setOnError(List<TransitionProcessorReference> onError);
}
