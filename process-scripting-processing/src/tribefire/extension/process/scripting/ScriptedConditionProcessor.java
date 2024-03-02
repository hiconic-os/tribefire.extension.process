// ============================================================================
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package tribefire.extension.process.scripting;

import java.util.HashMap;
import java.util.Map;

import tribefire.extension.process.api.ConditionProcessor;
import tribefire.extension.process.api.ConditionProcessorContext;
import tribefire.extension.process.data.model.ProcessItem;
import tribefire.extension.scripting.common.CommonScriptedProcessor;

public class ScriptedConditionProcessor extends CommonScriptedProcessor implements ConditionProcessor<ProcessItem> {

	@Override
	public boolean matches(ConditionProcessorContext<ProcessItem> context) {
		Map<String, Object> bindings = new HashMap<>();
		bindings.put("$context", context);

		Object result = processReasonedScripted(bindings).get();
		
		if (result instanceof Boolean)
			return (boolean) result;
		
		return result != null; 
	}

}
