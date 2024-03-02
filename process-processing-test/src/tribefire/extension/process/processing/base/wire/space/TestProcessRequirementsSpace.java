// ============================================================================
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
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
