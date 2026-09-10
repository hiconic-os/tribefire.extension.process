package tribefire.extension.process.reason.model;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * The condition of an edge holds an expression that the engine cannot evaluate. Only {@code "true"} and {@code "false"} are
 * supported today.
 * <p>
 * The engine treats this like a failing condition processor: the process halts, and an operator continues it with
 * {@code RecoverProcess} once the configuration is fixed.
 */
public interface InvalidConditionExpression extends ProcessReason {
	EntityType<InvalidConditionExpression> T = EntityTypes.T(InvalidConditionExpression.class);

	String expression = "expression";

	/** The value that was found on the condition. */
	String getExpression();
	void setExpression(String expression);
}
