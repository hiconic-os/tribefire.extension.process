package tribefire.extension.process.model.configuration;

import java.util.Map;
import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/** Application-scoped process definitions. ProcessItem types refer to these definitions through metadata. */
public interface ProcessDefinitionsConfiguration extends GenericEntity {

	EntityType<ProcessDefinitionsConfiguration> T = EntityTypes.T(ProcessDefinitionsConfiguration.class);

	String definitions = "definitions";
	String monitoredAccessIds = "monitoredAccessIds";

	Map<String, ProcessDefinition> getDefinitions();
	void setDefinitions(Map<String, ProcessDefinition> definitions);

	Set<String> getMonitoredAccessIds();
	void setMonitoredAccessIds(Set<String> monitoredAccessIds);
}
