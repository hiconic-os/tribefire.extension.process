package tribefire.extension.process.rx.test.expert;

import tribefire.extension.process.api.ConditionProcessor;
import tribefire.extension.process.api.ConditionProcessorContext;
import tribefire.extension.process.rx.test.model.RoutingTestProcess;

/** Matches when the process is approved. */
public class ApprovedCondition implements ConditionProcessor<RoutingTestProcess> {

	@Override
	public boolean matches(ConditionProcessorContext<RoutingTestProcess> context) {
		return context.getProcess().getApproved();
	}
}
