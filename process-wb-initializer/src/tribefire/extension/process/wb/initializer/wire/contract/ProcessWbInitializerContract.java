
package tribefire.extension.process.wb.initializer.wire.contract;

import com.braintribe.wire.api.space.WireSpace;
import tribefire.extension.process.wb.initializer.wire.space.ProcessWbInitializerSpace;

public interface ProcessWbInitializerContract extends WireSpace {

	/** @see ProcessWbInitializerSpace#initialize()  */
	void initialize();

}
