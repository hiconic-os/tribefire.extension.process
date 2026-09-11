package tribefire.extension.process.rx.test.model;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/** A process type without {@code ManageProcessWith} metadata, to see what the engine answers for it. */
public interface UnmappedTestProcess extends TestProcess {

	EntityType<UnmappedTestProcess> T = EntityTypes.T(UnmappedTestProcess.class);
}
