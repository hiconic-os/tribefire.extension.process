// ============================================================================
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
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
