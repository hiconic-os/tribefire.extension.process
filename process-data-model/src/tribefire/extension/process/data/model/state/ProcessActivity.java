// ============================================================================
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package tribefire.extension.process.data.model.state;

import com.braintribe.model.generic.base.EnumBase;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.EnumTypes;

import tribefire.extension.process.data.model.ProcessItem;

/**
 * The {@link ProcessItem#getActivity() ProcessActivity} is used differentiate {@link ProcessItem processes} in different situations such as:
 * 
 * <ul>
 * 		<li>validity of api operation (e.g start, resume, recover)
 * 		<li>detection and revival of unattended processes
 * 		<li>detection ended processes
 * </ul>
 * @author Dirk Scheffler
 */
public enum ProcessActivity implements EnumBase {
	/**
	 * The process is currently handled by the process manager. If a process is in that activity and {@link ProcessItem#getLastTransit()} is stale
	 * it is assumed that this process is unattended and can be revived. {@link ProcessItem#getLastTransit()} is actively kept fresh during long running
	 * transition processor executions to have a tight and safe recognition of stale processes.
	 */
	processing, 
	
	/**
	 * The process waits in a state of a node in the state graph that has a decoupled interaction. It can be resumed by that decoupled interaction. 
	 */
	waiting,
	
	/**
	 * The process came into a problem (e.g by some failing transition processor or condition processor) that needs administrative attention
	 */
	halted, 
	
	/**
	 * The process reached a drain node in the state graph and therefore ended
	 */
	ended;

	public static final EnumType T = EnumTypes.T(ProcessActivity.class);
	
	@Override
	public EnumType type() {
		return T;
	}	
}
