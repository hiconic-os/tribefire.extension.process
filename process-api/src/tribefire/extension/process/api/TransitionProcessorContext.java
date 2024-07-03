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
package tribefire.extension.process.api;

import com.braintribe.gm.model.reason.Reason;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.EntityProperty;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

public interface TransitionProcessorContext<T extends GenericEntity> {

	EntityProperty getProcessProperty();
	T getProcess();
	T getProcessFromSystemSession();
	String getLeftState();
	String getEnteredState();	
	PersistenceGmSession getSession();
	PersistenceGmSession getSystemSession();
	void notifyInducedManipulation(Manipulation manipulation);
	
	/**
	 * only by calling this a {@link TransitionProcessor} will be allowed to influence state changes of a process
	 */
	void continueWithState(String value);
	
	default void setError(Reason reason) {
		
	}
}
