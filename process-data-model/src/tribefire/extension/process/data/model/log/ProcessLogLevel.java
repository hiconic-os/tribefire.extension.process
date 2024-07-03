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
package tribefire.extension.process.data.model.log;

import com.braintribe.model.generic.base.EnumBase;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.EnumTypes;

/**
 * The log level of a {@link ProcessLogEntry} to differentiate informative from problematic events. 
 * 
 * @author Dirk Scheffler
 */
public enum ProcessLogLevel implements EnumBase {
	/**
	 * Used to mark the normal events happening for a process
	 */
	INFO, 
	
	/**
	 * Used to mark fatal events happening for a process
	 */
	ERROR, 
	
	/**
	 * Used to mark problematic events for a process that are non fatal and just should get attention for improvements
	 */
	WARN;

	public static final EnumType T = EnumTypes.T(ProcessLogLevel.class);
	
	@Override
	public EnumType type() {
		return T;
	}	
}
