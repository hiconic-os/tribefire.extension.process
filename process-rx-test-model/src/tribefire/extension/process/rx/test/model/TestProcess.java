package tribefire.extension.process.rx.test.model;

import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

import tribefire.extension.process.data.model.ProcessItem;

/**
 * Common base of the processes that the engine tests run. {@link #getTrace()} is how a test sees which processors ran, in
 * which order: each processor appends its own name to it.
 */
@Abstract
public interface TestProcess extends ProcessItem {

	EntityType<TestProcess> T = EntityTypes.T(TestProcess.class);

	String trace = "trace";

	String getTrace();
	void setTrace(String trace);

	/** Appends one entry to the trace, which is how a processor records that it ran. */
	default void trace(String entry) {
		String current = getTrace();
		setTrace(current == null || current.isEmpty() ? entry : current + "," + entry);
	}
}
