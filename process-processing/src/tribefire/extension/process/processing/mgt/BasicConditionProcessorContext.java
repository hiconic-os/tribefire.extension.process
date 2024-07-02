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
