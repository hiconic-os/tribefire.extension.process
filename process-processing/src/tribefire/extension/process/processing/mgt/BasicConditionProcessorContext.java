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
