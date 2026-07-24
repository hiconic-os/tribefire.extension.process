package tribefire.extension.process.rx.api;

import tribefire.extension.process.api.ConditionProcessor;
import tribefire.extension.process.api.TransitionProcessor;

public interface ProcessExpertResolver {
	TransitionProcessor<?> resolveTransitionProcessor(String processorId);
	ConditionProcessor<?> resolveConditionProcessor(String processorId);
}
