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

import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.product.rat.imp.AbstractImpCave;

import tribefire.extension.process.model.deployment.ProcessingEngine;
import tribefire.extension.process.model.scripting.deployment.ScriptedConditionProcessor;
import tribefire.extension.process.model.scripting.deployment.ScriptedTransitionProcessor;
import tribefire.extension.scripting.imp.ScriptedProcessorImp;

/**
 * An {@link AbstractImpCave} specialized in {@link ProcessingEngine} related deployables.
 */
public class ProcessingEngineImpCave extends AbstractImpCave<ProcessingEngine, ProcessingEngineImp> {

	public ProcessingEngineImpCave(PersistenceGmSession session) {
		super(session, "externalId", ProcessingEngine.T);
	}

	public ProcessingEngineImp createProcessingEngine(String externalId) {
		ProcessingEngine processingEngine = session().create(ProcessingEngine.T);
		processingEngine.setExternalId(externalId);
		processingEngine.setName(externalId);

		return buildImp(processingEngine);
	}

	public ProcessDefinitionImpCave definition() {
		return new ProcessDefinitionImpCave(session());
	}

	@Override
	public ProcessingEngineImp buildImp(ProcessingEngine instance) {
		return new ProcessingEngineImp(session(), instance);
	}

	public ScriptedProcessorImp<ScriptedTransitionProcessor> createScriptedTransitionProcessor(String name, String externalId) {
		ScriptedTransitionProcessor scriptedTransitionProcessor = session().create(ScriptedTransitionProcessor.T);
		scriptedTransitionProcessor.setName(name);
		scriptedTransitionProcessor.setExternalId(externalId);
		return new ScriptedProcessorImp<ScriptedTransitionProcessor>(session(), scriptedTransitionProcessor);
	}

	public ScriptedProcessorImp<ScriptedConditionProcessor> createScriptedCondition(String name, String externalId) {
		ScriptedConditionProcessor scriptedCondition = session().create(ScriptedConditionProcessor.T);
		scriptedCondition.setName(name);
		scriptedCondition.setExternalId(externalId);
		return new ScriptedProcessorImp<ScriptedConditionProcessor>(session(), scriptedCondition);
	}
}
