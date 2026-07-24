package tribefire.extension.process.rx.api;

import java.util.function.Supplier;

import hiconic.rx.module.api.wire.RxExportContract;
import tribefire.extension.process.api.ConditionProcessor;
import tribefire.extension.process.api.TransitionProcessor;
import tribefire.extension.process.model.configuration.ProcessDefinitionsConfiguration;

public interface ProcessRxContract extends RxExportContract, ProcessModelSymbols {
	void registerTransitionProcessor(String id, Supplier<? extends TransitionProcessor<?>> supplier);
	void registerConditionProcessor(String id, Supplier<? extends ConditionProcessor<?>> supplier);
	ProcessDefinitionsConfiguration definitions();
}
