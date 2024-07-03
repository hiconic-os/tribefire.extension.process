// ============================================================================
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ============================================================================
package tribefire.extension.process.scripting;

import java.util.HashMap;
import java.util.Map;

import tribefire.extension.process.api.TransitionProcessor;
import tribefire.extension.process.api.TransitionProcessorContext;
import tribefire.extension.process.data.model.ProcessItem;
import tribefire.extension.scripting.common.CommonScriptedProcessor;

public class ScriptedTransitionProcessor extends CommonScriptedProcessor implements TransitionProcessor<ProcessItem> {

	@Override
	public void process(TransitionProcessorContext<ProcessItem> context) {
		Map<String, Object> bindings = new HashMap<>();
		bindings.put("$context", context);

		processReasonedScripted(bindings).get();
	}

}
