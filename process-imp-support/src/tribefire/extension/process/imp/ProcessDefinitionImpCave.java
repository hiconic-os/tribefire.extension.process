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
package tribefire.extension.process.imp;
// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================



import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.product.rat.imp.AbstractImpCave;

import tribefire.extension.process.model.deployment.ProcessDefinition;

/**
 * An {@link AbstractImpCave} specialized in {@link ProcessDefinition}
 */
public class ProcessDefinitionImpCave extends AbstractImpCave<ProcessDefinition, ProcessDefinitionImp> {

	public ProcessDefinitionImpCave(PersistenceGmSession session) {
		super(session, "globalId", ProcessDefinition.T);
	}

	/**
	 * @param name
	 *            the name for the ProcessDefinition to be created
	 */
	public ProcessDefinitionImp create(String name) {
		ProcessDefinition processDefinition = session().create(ProcessDefinition.T);
		processDefinition.setName(name);

		return buildImp(processDefinition);
	}

	@Override
	protected ProcessDefinitionImp buildImp(ProcessDefinition instance) {
		return new ProcessDefinitionImp(session(), instance);
	}

}
