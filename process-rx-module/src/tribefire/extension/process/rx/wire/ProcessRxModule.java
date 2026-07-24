package tribefire.extension.process.rx.wire;

import hiconic.rx.module.api.wire.Exports;
import hiconic.rx.module.api.wire.RxModule;
import tribefire.extension.process.rx.api.ProcessRxContract;
import tribefire.extension.process.rx.wire.space.ProcessRxModuleSpace;

public enum ProcessRxModule implements RxModule<ProcessRxModuleSpace> {
	INSTANCE;

	@Override
	public void bindExports(Exports exports) {
		exports.bind(ProcessRxContract.class, ProcessRxModuleSpace.class);
	}
}
