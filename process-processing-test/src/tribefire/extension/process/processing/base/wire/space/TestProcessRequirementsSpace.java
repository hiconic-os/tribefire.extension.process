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
package tribefire.extension.process.processing.base.wire.space;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.context.WireContextConfiguration;

import tribefire.extension.process.model.deployment.ConditionProcessor;
import tribefire.extension.process.model.deployment.TransitionProcessor;
import tribefire.extension.process.processing.base.pd.wire.contract.TestProcessRequirementsContract;
import tribefire.extension.process.processing.base.wire.contract.TestPlatformContract;
import tribefire.extension.process.processing.experts.FailingByExceptionTransitionProcessor;
import tribefire.extension.process.processing.experts.FailingByReasonTransitionProcessor;
import tribefire.extension.process.processing.experts.Lt10000ConditionProcessor;
import tribefire.extension.process.processing.experts.Lt1000ConditionProcessor;
import tribefire.extension.process.processing.experts.SelfRoutingTransitionProcessor;
import tribefire.extension.process.processing.experts.StateTaggingTransitionProcessor;
import tribefire.extension.process.processing.experts.TestProcessor;
import tribefire.pd.editor.api.ProcessDefinitionEditor;

@Managed
public class TestProcessRequirementsSpace implements TestProcessRequirementsContract {
	
	@Import
	TestPlatformContract testPlatform;
	
	@Override
	public void onLoaded(WireContextConfiguration configuration) {
		TestProcessor testProcessors[] = {
				stateTaggingProcessorExpert(),
				selfRoutingProcessorExpert(),
				failingByExceptionTransitionProcessorExpert(),
				failingByReasonTransitionProcessorExpert(),
				lt1000ConditionProcessorExpert(),
				lt10000ConditionProcessorExpert()
		};
		
		for (TestProcessor testProcessor: testProcessors) {
			testPlatform.deploy(testProcessor.getDeployable(), testProcessor.getComponentType(), testProcessor);
		}
	}
	
	@Managed
	public StateTaggingTransitionProcessor stateTaggingProcessorExpert() {
		StateTaggingTransitionProcessor bean = new StateTaggingTransitionProcessor();
		return bean;
	}
	
	@Managed
	public SelfRoutingTransitionProcessor selfRoutingProcessorExpert() {
		SelfRoutingTransitionProcessor bean = new SelfRoutingTransitionProcessor();
		return bean;
	}
	
	@Managed
	public FailingByReasonTransitionProcessor failingByReasonTransitionProcessorExpert() {
		FailingByReasonTransitionProcessor bean = new FailingByReasonTransitionProcessor();
		return bean;
	}
	
	@Managed
	public FailingByExceptionTransitionProcessor failingByExceptionTransitionProcessorExpert() {
		FailingByExceptionTransitionProcessor bean = new FailingByExceptionTransitionProcessor();
		return bean;
	}
	
	@Managed
	public Lt1000ConditionProcessor lt1000ConditionProcessorExpert() {
		Lt1000ConditionProcessor bean = new Lt1000ConditionProcessor();
		return bean;
	}
	
	@Managed
	public Lt10000ConditionProcessor lt10000ConditionProcessorExpert() {
		Lt10000ConditionProcessor bean = new Lt10000ConditionProcessor();
		return bean;
	}



	@Override
	public ProcessDefinitionEditor newProcessEditor() {
		return ProcessDefinitionEditor.create();
	}



	@Override
	public <T extends GenericEntity> T create(EntityType<T> type) {
		return type.create();
	}



	@Override
	public TransitionProcessor stateTaggingProcessor() {
		return stateTaggingProcessorExpert().getDeployable();
	}



	@Override
	public TransitionProcessor failingByReasonTransitionProcessor() {
		return failingByReasonTransitionProcessorExpert().getDeployable();
	}



	@Override
	public TransitionProcessor failingByExceptionTransitionProcessor() {
		return failingByExceptionTransitionProcessorExpert().getDeployable();
	}



	@Override
	public TransitionProcessor selfRoutingProcessor() {
		return selfRoutingProcessorExpert().getDeployable();
	}



	@Override
	public ConditionProcessor lt1000ConditionProcessor() {
		return lt1000ConditionProcessorExpert().getDeployable();
	}



	@Override
	public ConditionProcessor lt10000ConditionProcessor() {
		return lt10000ConditionProcessorExpert().getDeployable();
	}
	
}
