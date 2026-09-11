package tribefire.extension.process.rx.test.expert;

import tribefire.extension.process.api.TransitionProcessor;
import tribefire.extension.process.api.TransitionProcessorContext;
import tribefire.extension.process.rx.test.model.RoutingTestProcess;

/** Demands the state that the process asks for, if it asks for one. Otherwise it leaves the routing alone. */
public class DemandStateAction implements TransitionProcessor<RoutingTestProcess> {

	@Override
	public void process(TransitionProcessorContext<RoutingTestProcess> context) {
		RoutingTestProcess process = context.getProcess();
		process.trace("demand");

		String demandedState = process.getDemandedState();
		if (demandedState != null)
			context.continueWithState(demandedState);
	}
}
