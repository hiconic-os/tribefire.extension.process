package tribefire.extension.process.model.configuration;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * Decides whether the process engine may take the {@link Edge} that holds this condition.
 * <p>
 * A condition is either an <b>expression</b> or a reference to a <b>condition processor</b>. If both are given, the
 * {@link #getConditionExpression() expression} wins. That precedence exists so that an edge can be disabled or forced
 * temporarily while it keeps the {@link #getConditionProcessorId() processor} that it normally uses.
 * <p>
 * The expression currently supports the two values {@link #TRUE_EXPRESSION} and {@link #FALSE_EXPRESSION} only. Any other
 * value is rejected when the application starts, and it halts the process if it reaches the engine at all. A real
 * expression language is planned, which is why this is a string and not a boolean.
 * <p>
 * <b>An edge with the expression {@code "true"} is not the same as an edge without a condition.</b> A conditioned edge is
 * evaluated in the first routing pass, in the order of {@link Node#getEdges()}, so a {@code "true"} expression shadows every
 * condition that follows it on the same node. An edge without a condition is only taken when no condition matched at all.
 * See {@link ProcessDefinition} for the full routing rule.
 * <p>
 * Typical usages:
 * <ul>
 * <li>{@code Condition.processor("my-condition")} - the normal case, the decision is made by a condition processor
 * <li>{@code Condition.expression(false)} - the edge is never taken by routing, but a transition processor may still take it
 * with {@code TransitionProcessorContext.continueWithState(String)}. This replaces a condition processor that always returns
 * {@code false}.
 * <li>{@code Condition.expression(true)} - the edge is always taken. Meant for temporary use, e.g. to force a path while
 * testing.
 * </ul>
 */
public interface Condition extends GenericEntity {
	EntityType<Condition> T = EntityTypes.T(Condition.class);

	/** The only two values that {@link #getConditionExpression()} currently supports. */
	String TRUE_EXPRESSION = "true";
	String FALSE_EXPRESSION = "false";

	String conditionProcessorId = "conditionProcessorId";
	String conditionExpression = "conditionExpression";

	/**
	 * The id under which the condition processor was registered, e.g. with
	 * {@code ProcessRxContract.registerConditionProcessor(String, Supplier)}. Ignored if
	 * {@link #getConditionExpression()} is given.
	 */
	String getConditionProcessorId();
	void setConditionProcessorId(String conditionProcessorId);

	/**
	 * A condition given directly instead of by a processor. Supported values are {@link #TRUE_EXPRESSION} and
	 * {@link #FALSE_EXPRESSION}; any other value is rejected at startup, and halts the process should the engine still meet
	 * it. Wins over {@link #getConditionProcessorId()}.
	 */
	String getConditionExpression();
	void setConditionExpression(String conditionExpression);

	/** A condition that is decided by the condition processor registered under the given id. */
	static Condition processor(String conditionProcessorId) {
		Condition condition = Condition.T.create();
		condition.setConditionProcessorId(conditionProcessorId);
		return condition;
	}

	/** A condition that always yields the given value. See the note on {@code "true"} in {@link Condition}. */
	static Condition expression(boolean value) {
		Condition condition = Condition.T.create();
		condition.setConditionExpression(value ? TRUE_EXPRESSION : FALSE_EXPRESSION);
		return condition;
	}
}
