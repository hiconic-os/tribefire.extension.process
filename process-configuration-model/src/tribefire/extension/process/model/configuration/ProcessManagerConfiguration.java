package tribefire.extension.process.model.configuration;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.time.TimeSpan;

/**
 * Timing of the search for processes that need attention, i.e. of the worker described by
 * {@link ProcessDefinitionsConfiguration#getMonitoredAccessIds()}. Both properties have a default, so an application that is
 * happy with it configures nothing.
 */
public interface ProcessManagerConfiguration extends GenericEntity {
	EntityType<ProcessManagerConfiguration> T = EntityTypes.T(ProcessManagerConfiguration.class);

	String monitorInterval = "monitorInterval";
	String unattendedThreshold = "unattendedThreshold";

	/** How long the worker waits between two searches. Default: one minute. */
	TimeSpan getMonitorInterval();
	void setMonitorInterval(TimeSpan monitorInterval);

	/**
	 * How long a process may stay in activity {@code processing} without progress before the worker treats it as
	 * <b>unattended</b> and hands it back to the engine. Default: one minute.
	 * <p>
	 * A process that runs a long transition processor is not unattended: the engine refreshes the progress date every 15
	 * seconds while a processor works. A threshold below that refresh interval therefore lets the worker chase healthy
	 * processes, which does no damage, as the lock on the process rejects the second attempt, but it fills the log.
	 */
	TimeSpan getUnattendedThreshold();
	void setUnattendedThreshold(TimeSpan unattendedThreshold);
}
