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
package tribefire.extension.process.processing.definition;

import java.util.HashMap;
import java.util.Map;

public interface RoutingMaps {
	static String state(int l, int n) {
		return "S_" + l + "_" + n;
	}
	
	static Map<String, String> routingMap(int... vc) {
		Map<String, String> routingMap = new HashMap<>();
		
		for (int l = 0; l < vc.length; l++) {
			String state = state(l, vc[l]);
			final String prevState;

			if (l > 0) {
				prevState = state(l - 1, vc[l - 1]);
			}
			else {
				prevState = null;
			}
			
			routingMap.put(prevState, state);
		}
		
		return routingMap;
	}
}
