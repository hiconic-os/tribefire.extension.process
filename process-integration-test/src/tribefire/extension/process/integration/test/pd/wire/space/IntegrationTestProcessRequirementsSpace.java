// ============================================================================
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package tribefire.extension.process.integration.test.pd.wire.space;

import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.extension.process.integration.test.TestEntityQueries;
import tribefire.extension.process.integration.test.pd.wire.contract.IntegrationTestProcessDefinitionRequirementsContract;
import tribefire.extension.process.model.deployment.ConditionProcessor;
import tribefire.extension.process.model.deployment.TransitionProcessor;
import tribefire.extension.process.processing.base.pd.wire.contract.TestProcessRequirementsContract;
import tribefire.extension.process.processing.experts.FailingByExceptionTransitionProcessor;
import tribefire.extension.process.processing.experts.FailingByReasonTransitionProcessor;
import tribefire.extension.process.processing.experts.Lt10000ConditionProcessor;
import tribefire.extension.process.processing.experts.Lt1000ConditionProcessor;
import tribefire.extension.process.processing.experts.SelfRoutingTransitionProcessor;
import tribefire.extension.process.processing.experts.StateTaggingTransitionProcessor;
import tribefire.extension.process.processing.experts.TestProcessor;
import tribefire.pd.editor.api.ProcessDefinitionEditor;

@Managed
public class IntegrationTestProcessRequirementsSpace implements TestProcessRequirementsContract {

	@Import
	private IntegrationTestProcessDefinitionRequirementsContract requirements;
	
	@Override
	public ProcessDefinitionEditor newProcessEditor() {
		return ProcessDefinitionEditor.create(requirements.session());
	}

	@Override
	public <T extends GenericEntity> T create(EntityType<T> type) {
		return requirements.session().create(type);
	}

	@Managed
	@Override
	public TransitionProcessor stateTaggingProcessor() {
		return acquireDeployable(TransitionProcessor.T, StateTaggingTransitionProcessor.class);
	}

	@Managed
	@Override
	public TransitionProcessor failingByReasonTransitionProcessor() {
		return acquireDeployable(TransitionProcessor.T, FailingByReasonTransitionProcessor.class);
	}

	@Managed
	@Override
	public TransitionProcessor failingByExceptionTransitionProcessor() {
		return acquireDeployable(TransitionProcessor.T, FailingByExceptionTransitionProcessor.class);
	}

	@Managed
	@Override
	public TransitionProcessor selfRoutingProcessor() {
		return acquireDeployable(TransitionProcessor.T, SelfRoutingTransitionProcessor.class);
	}

	@Managed
	@Override
	public ConditionProcessor lt1000ConditionProcessor() {
		return acquireDeployable(ConditionProcessor.T, Lt1000ConditionProcessor.class);
	}

	@Managed
	@Override
	public ConditionProcessor lt10000ConditionProcessor() {
		return acquireDeployable(ConditionProcessor.T, Lt10000ConditionProcessor.class);
	}
	
	private <D extends Deployable> D acquireDeployable(EntityType<D> deployableType, Class<?> expertClass) {
		String externalId = TestProcessor.externalId(deployableType, expertClass);
		D deployable = requirements.session().query().entities(TestEntityQueries.deployableByExternalId(deployableType, externalId)).first();
		
		if (deployable == null) {
			deployable = requirements.session().create(deployableType);
			deployable.setExternalId(externalId);
			deployable.setName(TestProcessor.name(expertClass));
		}
		
		return deployable;
	}
}
