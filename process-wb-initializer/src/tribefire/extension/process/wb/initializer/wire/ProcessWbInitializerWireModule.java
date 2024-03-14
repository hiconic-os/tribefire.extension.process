
package tribefire.extension.process.wb.initializer.wire;

import static com.braintribe.wire.api.util.Lists.list;

import java.util.List;

import com.braintribe.wire.api.module.WireModule;
import com.braintribe.wire.api.module.WireTerminalModule;

import tribefire.cortex.initializer.support.integrity.wire.CoreInstancesWireModule;

import tribefire.extension.process.wb.initializer.wire.contract.ProcessWbInitializerContract;

public enum ProcessWbInitializerWireModule implements WireTerminalModule<ProcessWbInitializerContract> {

	INSTANCE;

	@Override
	public List<WireModule> dependencies() {
		return list(CoreInstancesWireModule.INSTANCE);
	}

}
