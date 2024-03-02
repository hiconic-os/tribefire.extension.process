// ============================================================================
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
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
