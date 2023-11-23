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
package tribefire.extension.process.processing;

import com.braintribe.model.generic.manipulation.EntityProperty;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.sp.api.ProcessStateChangeContext;

import tribefire.extension.process.api.TransitionProcessorContext;
import tribefire.extension.process.model.ContinueWithState;
import tribefire.extension.process.model.data.Process;

public class TransitionProcessorContextImpl<T extends Process> implements TransitionProcessorContext<T>{
	
	private Object leftState;
	private Object enteredState;
	private EntityProperty processProperty;
	private ProcessStateChangeContext<T> processStateChangeContext;
	private ContinueWithState continueWithState;
	
	@Override
	public EntityProperty getProcessProperty() {
		return processProperty;
	}
	
	@Override
	public T getProcess() {
		return processStateChangeContext.getProcessEntity();
	}	

	@Override
	public PersistenceGmSession getSession() {
		return processStateChangeContext.getSession();
	}
	@Override
	public Object getLeftState() {
		return leftState;
	}
	
	@Override
	public Object getEnteredState() {
		return enteredState;
	}
	
	public TransitionProcessorContextImpl(EntityProperty processProperty, Object leftState, Object enteredState) {		
		this.processProperty = processProperty;
		this.leftState = leftState;
		this.enteredState = enteredState;
	}
	
	public TransitionProcessorContextImpl(ProcessStateChangeContext<T> context, Object leftState, Object enteredState) {		
		this.processProperty = context.getEntityProperty();
		this.processStateChangeContext = context;
		this.leftState = leftState;
		this.enteredState = enteredState;
	}


	@Override
	public T getProcessFromSystemSession() {
		return processStateChangeContext.getSystemProcessEntity();
	}


	@Override
	public PersistenceGmSession getSystemSession() {
		return processStateChangeContext.getSystemSession();
	}


	@Override
	public void notifyInducedManipulation(Manipulation manipulation) {
		processStateChangeContext.notifyInducedManipulation(manipulation);
	}

	@Override
	public void continueWithState(Object value) {
		this.continueWithState = ContinueWithState.T.create();
		continueWithState.setState(value);
	}
	
	public ContinueWithState getContinueWithState() {
		return continueWithState;
	}
}
