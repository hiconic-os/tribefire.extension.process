package tribefire.extension.process.rx.impl;

import java.util.function.Supplier;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.meta.cmd.CmdResolver;

import tribefire.extension.process.data.model.ProcessItem;
import tribefire.extension.process.model.configuration.ProcessDefinition;
import tribefire.extension.process.model.configuration.ProcessDefinitionsConfiguration;
import tribefire.extension.process.model.meta.ManageProcessWith;
import tribefire.extension.process.reason.model.ProcessDefinitionNotFound;
import tribefire.extension.process.rx.api.ProcessDefinitionResolver;

/** Resolves the effective ProcessItem mapping in the configured model of the owning access. */
public class CmdProcessDefinitionResolver implements ProcessDefinitionResolver {

	private final Supplier<ProcessDefinitionsConfiguration> definitionsSupplier;

	public CmdProcessDefinitionResolver(Supplier<ProcessDefinitionsConfiguration> definitionsSupplier) {
		this.definitionsSupplier = definitionsSupplier;
	}

	@Override
	public Maybe<ProcessDefinition> resolve(EntityType<? extends ProcessItem> processType, CmdResolver cmdResolver) {
		ManageProcessWith mapping = cmdResolver.getMetaData().entityType(processType) //
				.meta(ManageProcessWith.T).exclusive();

		if (mapping == null)
			return Reasons.build(ProcessDefinitionNotFound.T) //
					.text("Process type " + processType.getTypeSignature() + " has no " + ManageProcessWith.T.getShortName() + " metadata") //
					.toMaybe();

		String processDefinitionId = mapping.getProcessDefinitionId();
		ProcessDefinition definition = definitionsSupplier.get().findDefinition(processDefinitionId);

		if (definition == null)
			return Reasons.build(ProcessDefinitionNotFound.T) //
					.text("No process definition [" + processDefinitionId + "] configured, as mapped on process type "
							+ processType.getTypeSignature()) //
					.assign(ProcessDefinitionNotFound::setProcessDefinitionId, processDefinitionId) //
					.toMaybe();

		return Maybe.complete(definition);
	}
}
