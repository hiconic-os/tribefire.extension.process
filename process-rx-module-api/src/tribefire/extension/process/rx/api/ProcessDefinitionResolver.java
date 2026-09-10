package tribefire.extension.process.rx.api;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.meta.cmd.CmdResolver;

import tribefire.extension.process.data.model.ProcessItem;
import tribefire.extension.process.model.configuration.ProcessDefinition;

/**
 * Resolves the {@link ProcessDefinition} of a process type, which the type names in its {@code ManageProcessWith} metadata.
 */
@FunctionalInterface
public interface ProcessDefinitionResolver {

	/**
	 * The definition for the given process type, or an unsatisfied {@link Maybe} with a
	 * {@code ProcessDefinitionNotFound} that says which id was missing.
	 */
	Maybe<ProcessDefinition> resolve(EntityType<? extends ProcessItem> processType, CmdResolver cmdResolver);
}
