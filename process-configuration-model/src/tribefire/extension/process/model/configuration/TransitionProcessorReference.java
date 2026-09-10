package tribefire.extension.process.model.configuration;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * Points to a transition processor, i.e. to the application code that the engine runs on a state change.
 * <p>
 * The engine resolves the id when it runs the transition, not when the definition is built. A definition may therefore
 * reference a processor that another module registers, and a wrong id shows up only when a process takes that transition.
 *
 * @see ProcessDefinition
 */
public interface TransitionProcessorReference extends GenericEntity {
	EntityType<TransitionProcessorReference> T = EntityTypes.T(TransitionProcessorReference.class);

	String processorId = "processorId";

	/**
	 * The id under which the processor was registered, e.g. with
	 * {@code ProcessRxContract.registerTransitionProcessor(String, Supplier)}. Nothing compares the two sides, so the graph
	 * and the registration must agree on the string. A constant shared by both is the safer way to write it.
	 */
	@Mandatory
	String getProcessorId();
	void setProcessorId(String processorId);
}
