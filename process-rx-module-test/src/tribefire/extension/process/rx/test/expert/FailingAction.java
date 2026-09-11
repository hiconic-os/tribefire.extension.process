package tribefire.extension.process.rx.test.expert;

import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.InvalidArgument;

import tribefire.extension.process.api.TransitionProcessor;
import tribefire.extension.process.api.TransitionProcessorContext;
import tribefire.extension.process.rx.test.model.FailingTestProcess;

/** Fails in the way the process asks for, and otherwise does its work. */
public class FailingAction implements TransitionProcessor<FailingTestProcess> {

	public static final String REASON_TEXT = "the test asked this processor to fail";

	@Override
	public void process(TransitionProcessorContext<FailingTestProcess> context) {
		FailingTestProcess process = context.getProcess();

		if (process.getFailByException())
			throw new IllegalStateException(REASON_TEXT);

		if (process.getFailByReason()) {
			context.setError(Reasons.build(InvalidArgument.T).text(REASON_TEXT).toReason());
			return;
		}

		process.trace("worked");
	}
}
