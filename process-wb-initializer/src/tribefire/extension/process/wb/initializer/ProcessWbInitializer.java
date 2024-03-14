
package tribefire.extension.process.wb.initializer;

import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.wire.api.module.WireTerminalModule;

import tribefire.cortex.initializer.support.api.WiredInitializerContext;
import tribefire.cortex.initializer.support.impl.AbstractInitializer;

import tribefire.extension.process.wb.initializer.wire.ProcessWbInitializerWireModule;
import tribefire.extension.process.wb.initializer.wire.contract.ProcessWbInitializerContract;

public class ProcessWbInitializer extends AbstractInitializer<ProcessWbInitializerContract> {

	@Override
	public WireTerminalModule<ProcessWbInitializerContract> getInitializerWireModule() {
		return ProcessWbInitializerWireModule.INSTANCE;
	}

	@Override
	public void initialize(PersistenceInitializationContext context, WiredInitializerContext<ProcessWbInitializerContract> initializerContext,
			ProcessWbInitializerContract initializerContract) {

		initializerContract.initialize();

	}

}