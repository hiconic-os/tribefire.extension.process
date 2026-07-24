package tribefire.extension.process.rx.api;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.meta.cmd.CmdResolver;

import tribefire.extension.process.data.model.ProcessItem;
import tribefire.extension.process.model.configuration.ProcessDefinition;

@FunctionalInterface
public interface ProcessDefinitionResolver {
	ProcessDefinition resolve(EntityType<? extends ProcessItem> processType, CmdResolver cmdResolver);
}
