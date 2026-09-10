package tribefire.extension.process.model.configuration;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * Marks a {@link Node} as a waiting state: the process stops there instead of routing on, because something outside the
 * engine has to act - a user, a worker, an external system. That party hands the process back with {@code ResumeProcess} or
 * {@code ResumeProcessToState}.
 * <p>
 * This type carries no behaviour. It only says that the node waits, and it names the interaction for a reader and for a UI.
 *
 * @see Node#getDecoupledInteraction()
 * @see Node#getGracePeriod()
 */
@SelectiveInformation("${name}")
public interface DecoupledInteraction extends GenericEntity {
	EntityType<DecoupledInteraction> T = EntityTypes.T(DecoupledInteraction.class);

	String name = "name";
	String description = "description";

	/** Human readable name of the expected interaction, e.g. "Four eyes approval". */
	String getName();
	void setName(String name);

	String getDescription();
	void setDescription(String description);
}
