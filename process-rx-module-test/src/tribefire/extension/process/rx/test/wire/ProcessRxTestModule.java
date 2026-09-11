package tribefire.extension.process.rx.test.wire;

import hiconic.rx.module.api.wire.RxModule;
import tribefire.extension.process.rx.test.wire.space.ProcessRxTestModuleSpace;

/** The application under test. It exports nothing, as the tests reach the engine through the process API. */
public enum ProcessRxTestModule implements RxModule<ProcessRxTestModuleSpace> {

	INSTANCE;

}
