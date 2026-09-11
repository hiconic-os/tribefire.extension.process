package tribefire.extension.process.rx.test.model;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

import tribefire.extension.process.annotation.ManageProcessWith;

/** Drives the failure tests. The properties choose how the transition processor of the first node fails. */
@ManageProcessWith(FailingTestProcess.DEFINITION_ID)
public interface FailingTestProcess extends TestProcess {

	EntityType<FailingTestProcess> T = EntityTypes.T(FailingTestProcess.class);

	String DEFINITION_ID = "test.failing";

	String failByReason = "failByReason";
	String failByException = "failByException";

	/** The processor sets an error reason on its context. */
	boolean getFailByReason();
	void setFailByReason(boolean failByReason);

	/** The processor throws. */
	boolean getFailByException();
	void setFailByException(boolean failByException);
}
