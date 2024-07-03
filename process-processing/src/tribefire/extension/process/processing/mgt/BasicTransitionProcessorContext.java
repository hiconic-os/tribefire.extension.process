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
