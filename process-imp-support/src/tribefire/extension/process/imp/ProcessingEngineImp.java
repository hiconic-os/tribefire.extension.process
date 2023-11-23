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

import java.util.Set;
import java.util.stream.Collectors;

import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.product.rat.imp.AbstractImp;
import com.braintribe.product.rat.imp.ImpException;
import com.braintribe.product.rat.imp.impl.deployable.BasicDeployableImp;

import tribefire.extension.process.model.deployment.ProcessDefinition;
import tribefire.extension.process.model.deployment.ProcessingEngine;

/**
 * A {@link BasicDeployableImp} specialized in {@link ProcessingEngine}
 */
public class ProcessingEngineImp extends AbstractImp<ProcessingEngine> {
	private final ProcessingEngine processingEngine;

	public ProcessingEngineImp(PersistenceGmSession session, ProcessingEngine processingEngine) {
		super(session, processingEngine);
		this.processingEngine = processingEngine;
	}

	/**
	 * Scans all ProcessDefinitions of this imp's processing engine and returns an imp managing it (when it finds one)
	 *
	 * @param name
	 *            the name of the ProcessDefinition that you want to manage
	 * @throws ImpException
	 *             when no (or multiple) process definitions with provided name belong to this imp's ProcessingEngine
	 */
	public ProcessDefinitionImp definition(String name) {
		//@formatter:off
		Set<ProcessDefinition> foundDefinitions = processingEngine.getProcessDefinitions().stream()
			.filter(d -> d.getName().equals(name))
			.collect(Collectors.toSet());
		//@formatter:on

		if (foundDefinitions.size() != 1) {
			throw new ImpException("Expected to find exactly one process definition with name '" + name + "' but found " + foundDefinitions.size());
		}

		ProcessDefinition foundDefinition = foundDefinitions.iterator().next();

		return new ProcessDefinitionImp(session(), foundDefinition);
	}

	/**
	 * assumes that this imp's ProcessingEngine has exactly one ProcessDefinition and returns an imp managing it
	 *
	 * @throws ImpException
	 *             if no or multiple process definitions belong to this imp's ProcessingEngine
	 */
	public ProcessDefinitionImp definition() {
		Set<ProcessDefinition> foundDefinitions = processingEngine.getProcessDefinitions();

		if (foundDefinitions.size() != 1) {
			throw new ImpException("Expected to find exactly one process definition but found " + foundDefinitions.size());
		}

		ProcessDefinition foundDefinition = foundDefinitions.iterator().next();

		return new ProcessDefinitionImp(session(), foundDefinition);

	}

	public ProcessingEngineImp addDefinition(ProcessDefinition definition) {
		instance.getProcessDefinitions().add(definition);

		return this;
	}
}
