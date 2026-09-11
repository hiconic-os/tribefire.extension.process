package tribefire.extension.process.rx.test.model;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

import tribefire.extension.process.annotation.ManageProcessWith;

/** Drives the tests around a decoupled interaction: waiting, resuming, and the overdue deadline. */
@ManageProcessWith(WaitingTestProcess.DEFINITION_ID)
public interface WaitingTestProcess extends TestProcess {

	EntityType<WaitingTestProcess> T = EntityTypes.T(WaitingTestProcess.class);

	String DEFINITION_ID = "test.waiting";
}
