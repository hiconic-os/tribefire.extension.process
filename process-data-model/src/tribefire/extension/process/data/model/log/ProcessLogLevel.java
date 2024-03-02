// ============================================================================
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
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
