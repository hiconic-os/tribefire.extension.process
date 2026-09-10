package tribefire.extension.process.model.configuration;

import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * One permitted state change of a process, from the {@link Node} that holds this edge in {@link Node#getEdges()} to the node
 * given by {@link #getTo()}.
 * <p>
 * Only a declared edge permits a state change, and a process that would need a missing one halts. That holds for both ways
 * a state change comes about:
 * <ul>
 * <li>automatic routing, which chooses among the edges of the current node
 * <li>a state that a transition processor demanded with {@code TransitionProcessorContext.continueWithState(String)}
 * </ul>
 * <p>
 * An edge without a {@link #getCondition() condition} is the default of its node - it is taken when no condition matched. An
 * edge with a condition takes part in the first routing pass. See {@link ProcessDefinition} for the routing rule, and
 * {@link Condition} for the difference between an absent condition and the expression {@code "true"}.
 */
@SelectiveInformation("edge: ${from.state} - ${to.state}")
public interface Edge extends GenericEntity {
	EntityType<Edge> T = EntityTypes.T(Edge.class);

	String name = "name";
	String description = "description";
	String from = "from";
	String to = "to";
	String condition = "condition";
	String onTransit = "onTransit";

	/**
	 * Identifies this edge within its {@link ProcessDefinition}, which is how an extension addresses it later, e.g. to add a
	 * transition processor to it or to remove it.
	 */
	@Mandatory
	String getName();
	void setName(String name);

	String getDescription();
	void setDescription(String description);

	/**
	 * The node this edge leaves. Redundant, because the edge is reached through {@link Node#getEdges()} of that very node.
	 * <p>
	 * TODO: check whether this property is needed at all. Its only known use is the {@link SelectiveInformation} of this type.
	 */
	Node getFrom();
	void setFrom(Node from);

	/** The node this edge leads to, i.e. the state the process gets when the engine takes this edge. */
	Node getTo();
	void setTo(Node to);

	/** Decides whether routing may take this edge. An edge without a condition is the default of its node. */
	Condition getCondition();
	void setCondition(Condition condition);

	/**
	 * Transition processors that run when the process takes this edge. They run after the processors of the definition and of
	 * the left node, and before those of the entered node.
	 */
	List<TransitionProcessorReference> getOnTransit();
	void setOnTransit(List<TransitionProcessorReference> onTransit);
}
