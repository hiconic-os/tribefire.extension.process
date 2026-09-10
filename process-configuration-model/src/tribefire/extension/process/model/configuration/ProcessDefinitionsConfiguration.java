package tribefire.extension.process.model.configuration;

import java.util.List;
import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * Configuration of all {@link ProcessDefinition}s, in one place.
 * <p>
 * The result is checked once at startup, and the application stops if it finds one of these:
 * <ul>
 * <li>a missing or duplicate {@link ProcessDefinition#getProcessDefinitionId() processDefinitionId}
 * <li>an {@link Edge} without a name, or with a name that another {@link Edge} of the same definition uses
 * <li>an {@link Edge} that leads to a state for which there is no {@link Node} (in the context of its definition)
 * <li>a {@link Node} with more than one {@link Edge} that has no {@link Edge#getCondition() condition}
 * <li>a {@link Condition} with neither a processor nor a supported expression
 * </ul>
 *
 * @see ProcessDefinition
 */
public interface ProcessDefinitionsConfiguration extends GenericEntity {

	EntityType<ProcessDefinitionsConfiguration> T = EntityTypes.T(ProcessDefinitionsConfiguration.class);

	String definitions = "definitions";
	String monitoredAccessIds = "monitoredAccessIds";

	/**
	 * All {@link ProcessDefinition}s of this application.
	 * <p>
	 * Each one identifies itself with its unique {@link ProcessDefinition#getProcessDefinitionId() processDefinitionId}.
	 */
	List<ProcessDefinition> getDefinitions();
	void setDefinitions(List<ProcessDefinition> definitions);

	/**
	 * The accesses in which the process manager searches for processes that need attention. Its revival worker evaluates a {@code ReviveProcesses}
	 * request per listed access, about once a minute, which finds two kinds of process and hands them back to the engine:
	 * <ul>
	 * <li>a process in activity {@code processing} whose {@code lastTransit} went stale, e.g. because the application was killed while it ran
	 * <li>a waiting process that passed its {@code overdueAt} deadline
	 * </ul>
	 * Nothing else depends on this list. Every process is started, resumed or recovered by a request that names its own access, so a process in an
	 * unlisted access runs normally - only nobody looks after it once it stops making progress.
	 */
	Set<String> getMonitoredAccessIds();
	void setMonitoredAccessIds(Set<String> monitoredAccessIds);

	/** The definition with the given id, or {@code null} if this configuration holds none. */
	default ProcessDefinition findDefinition(String processDefinitionId) {
		for (ProcessDefinition definition : getDefinitions())
			if (processDefinitionId.equals(definition.getProcessDefinitionId()))
				return definition;

		return null;
	}
}
