// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
// 
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package tribefire.extension.process.api;

import com.braintribe.model.generic.manipulation.EntityProperty;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import tribefire.extension.process.model.data.Process;

public interface TransitionProcessorContext<T extends Process> {

	EntityProperty getProcessProperty();
	T getProcess();
	T getProcessFromSystemSession();
	Object getLeftState();
	Object getEnteredState();	
	PersistenceGmSession getSession();
	PersistenceGmSession getSystemSession();
	void notifyInducedManipulation(Manipulation manipulation);
	
	/**
	 * only by calling this a {@link TransitionProcessor} will be allowed to influence state changes of a process
	 */
	void continueWithState(Object value);
}
