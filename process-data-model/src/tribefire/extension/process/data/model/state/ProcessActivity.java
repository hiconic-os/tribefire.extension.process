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
