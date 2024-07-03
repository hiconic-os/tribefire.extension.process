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
