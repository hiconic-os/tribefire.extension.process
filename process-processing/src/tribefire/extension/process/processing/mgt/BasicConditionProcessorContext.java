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
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

import tribefire.extension.process.api.ConditionProcessorContext;
import tribefire.extension.process.data.model.ProcessItem;

public class BasicConditionProcessorContext<T extends ProcessItem> implements ConditionProcessorContext<T> {

	private T process;
	private Reason error;
	private PersistenceGmSession session;
	
	public BasicConditionProcessorContext(PersistenceGmSession session, T process) {
		super();
		this.session = session;
		this.process = process;
	}
	
	@Override
	public void setError(Reason reason) {
		this.error = reason;
	}

	public Reason getError() {
		return error;
	}


	@Override
	public T getProcess() {
		return process;
	}


	@Override
	public PersistenceGmSession getSession() {
		return session;
	}


	@Override
	public ConditionProcessorContext<T> system() {
		return this;
	}


	@Override
	public ConditionProcessorContext<T> request() {
		return this;
	}
}
