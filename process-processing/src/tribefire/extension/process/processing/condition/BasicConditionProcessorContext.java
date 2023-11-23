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
package tribefire.extension.process.processing.condition;

import java.util.HashMap;
import java.util.Map;

import com.braintribe.logging.Logger;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

import tribefire.extension.process.api.ConditionProcessorContext;
import tribefire.extension.process.model.data.Process;

public class BasicConditionProcessorContext<S extends Process> implements ConditionProcessorContext<S> {
	private static final Logger logger = Logger.getLogger(BasicConditionProcessorContext.class);
	private enum Axis { request, system }
	private S process;
	private PersistenceGmSession session;
	private Map<Axis, BasicConditionProcessorContext<S>> axises; 
	
	public BasicConditionProcessorContext(PersistenceGmSession session, PersistenceGmSession systemSession, S subject, S systemSubject) {
		this(session, subject);
		BasicConditionProcessorContext<S> systemContext = new BasicConditionProcessorContext<S>(systemSession, systemSubject);

		axises = new HashMap<Axis, BasicConditionProcessorContext<S>>();
		axises.put(Axis.request, this);
		axises.put(Axis.system, systemContext);

		systemContext.axises = axises;
	}
	
	private BasicConditionProcessorContext(PersistenceGmSession session, S subject) {
		this.session = session;
		this.process = subject;
	}

	@Override
	public S getProcess() {
		return process;
	}

	@Override
	public PersistenceGmSession getSession() {
		return session;
	}

	@Override
	public ConditionProcessorContext<S> system() {
		return axises.get(Axis.system);
	}

	@Override
	public ConditionProcessorContext<S> request() {
		return axises.get(Axis.request);
	}

	protected void commitIfNecessary( PersistenceGmSession session) throws GmSessionException {
		if (session.getTransaction().hasManipulations()) {
			session.commit();
		}
	}

	public void commitIfNecessary() throws GmSessionException{
		for (Axis axis: new Axis[]{Axis.system, Axis.request}) {
			BasicConditionProcessorContext<S> context = axises.get(axis);
			try {
				commitIfNecessary(context.session);
			} catch (GmSessionException e) {
				String msg = "cannot commit " + axis + " session for subject " + process;
				logger.error( msg, e);
				throw e;
			}
		}
	}

}
