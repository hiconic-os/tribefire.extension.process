// ============================================================================
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package tribefire.extension.process.processing.mgt;

import com.braintribe.gm.model.reason.Reason;
import com.braintribe.model.generic.manipulation.EntityProperty;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

import tribefire.extension.process.api.TransitionProcessorContext;
import tribefire.extension.process.data.model.ProcessItem;

public class BasicTransitionProcessorContext<T extends ProcessItem> implements TransitionProcessorContext<T> {

	private String leftState;
	private String enteredState;
	private T process;
	private String state;
	private Reason error;
	private PersistenceGmSession session;
	
	public BasicTransitionProcessorContext(PersistenceGmSession session, T process) {
		super();
		this.session = session;
		this.process = process;
		this.leftState = process.getPreviousState();
		this.enteredState = process.getNextState();
	}

	@Override
	public EntityProperty getProcessProperty() {
		return null;
	}

	@Override
	public T getProcess() {
		if (process == null) {
			process = getSession().query().entity(process).require();
		}

		return process;
	}

	@Override
	public T getProcessFromSystemSession() {
		return process;
	}

	@Override
	public String getLeftState() {
		return leftState;
	}

	@Override
	public String getEnteredState() {
		return enteredState;
	}

	@Override
	public PersistenceGmSession getSession() {
		return session;
	}

	@Override
	public PersistenceGmSession getSystemSession() {
		return session;
	}

	@Override
	public void notifyInducedManipulation(Manipulation manipulation) {
		
	}

	@Override
	public void continueWithState(String state) {
		this.state = state;
	}
	
	public String getContinueWithState() {
		return state;
	}
	
	@Override
	public void setError(Reason reason) {
		this.error = reason;
	}

	public Reason getError() {
		return error;
	}
}