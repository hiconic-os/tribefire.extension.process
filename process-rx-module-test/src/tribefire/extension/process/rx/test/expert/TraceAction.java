package tribefire.extension.process.rx.test.expert;

import tribefire.extension.process.api.TransitionProcessor;
import tribefire.extension.process.api.TransitionProcessorContext;
import tribefire.extension.process.rx.test.model.TestProcess;

/** Records that it ran, by appending its own name to the trace of the process. */
public class TraceAction implements TransitionProcessor<TestProcess> {

	private final String entry;

	public TraceAction(String entry) {
		this.entry = entry;
	}

	@Override
	public void process(TransitionProcessorContext<TestProcess> context) {
		context.getProcess().trace(entry);
	}
}
