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
package tribefire.extension.process.test.wire.space;

import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.processing.deployment.api.binding.DenotationBindingBuilder;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.extension.process.model.deployment.ConditionProcessor;
import tribefire.extension.process.model.deployment.TransitionProcessor;
import tribefire.extension.process.module.wire.contract.ProcessBindersContract;
import tribefire.extension.process.processing.experts.FailingByExceptionTransitionProcessor;
import tribefire.extension.process.processing.experts.FailingByReasonTransitionProcessor;
import tribefire.extension.process.processing.experts.Lt10000ConditionProcessor;
import tribefire.extension.process.processing.experts.Lt1000ConditionProcessor;
import tribefire.extension.process.processing.experts.SelfRoutingTransitionProcessor;
import tribefire.extension.process.processing.experts.StateTaggingTransitionProcessor;
import tribefire.extension.process.processing.experts.TestConditionProcessor;
import tribefire.extension.process.processing.experts.TestTransitionProcessor;
import tribefire.module.api.InitializerBindingBuilder;
import tribefire.module.api.WireContractBindingBuilder;
import tribefire.module.wire.contract.TribefireModuleContract;
import tribefire.module.wire.contract.TribefireWebPlatformContract;

/**
 * This module's javadoc is yet to be written.
 */
@Managed
public class ProcessTestModuleSpace implements TribefireModuleContract {

	@Import
	private TribefireWebPlatformContract tfPlatform;
	
	@Import
	private ProcessBindersContract processBinders;

	//
	// WireContracts
	//

	@Override
	public void bindWireContracts(WireContractBindingBuilder bindings) {
		// Bind wire contracts to make them available for other modules.
		// Note that the Contract class cannot be defined in this module, but must be in a gm-api artifact.
	}

	//
	// Hardwired deployables
	//

	@Override
	public void bindHardwired() {
		// Bind hardwired deployables here.
	}

	//
	// Initializers
	//

	@Override
	public void bindInitializers(InitializerBindingBuilder bindings) {
	}
	
	//
	// Deployables
	//

	@Override
	public void bindDeployables(DenotationBindingBuilder bindings) {
		bindTransitionProcessor(bindings, failingByExceptionTransitionProcessor());
		bindTransitionProcessor(bindings, failingByReasonTransitionProcessor());
		bindTransitionProcessor(bindings, selfRoutingTransitionProcessor());
		
		bindConditionProcessor(bindings, lt10000ConditionProcessor());
		bindConditionProcessor(bindings, lt1000ConditionProcessor());
	}
	
	private void bindTransitionProcessor(DenotationBindingBuilder bindings, TestTransitionProcessor<?> testProcessor) {
		Deployable deployable = testProcessor.getDeployable();
		bindings.bind(TransitionProcessor.T, deployable.getExternalId()).component(processBinders.transitionProcessor()).expert(testProcessor);
	}
	
	private void bindConditionProcessor(DenotationBindingBuilder bindings, TestConditionProcessor<?> testProcessor) {
		Deployable deployable = testProcessor.getDeployable();
		bindings.bind(ConditionProcessor.T, deployable.getExternalId()).component(processBinders.conditionProcessor()).expert(testProcessor);
	}
	
	@Managed
	private FailingByExceptionTransitionProcessor failingByExceptionTransitionProcessor() {
		FailingByExceptionTransitionProcessor bean = new FailingByExceptionTransitionProcessor();
		return bean;
	}
	
	@Managed
	private FailingByReasonTransitionProcessor failingByReasonTransitionProcessor() {
		FailingByReasonTransitionProcessor bean = new FailingByReasonTransitionProcessor();
		return bean;
	}
	
	@Managed
	private Lt10000ConditionProcessor lt10000ConditionProcessor() {
		Lt10000ConditionProcessor bean = new Lt10000ConditionProcessor();
		return bean;
	}
	
	@Managed
	private Lt1000ConditionProcessor lt1000ConditionProcessor() {
		Lt1000ConditionProcessor bean = new Lt1000ConditionProcessor();
		return bean;
	}
	
	@Managed
	private SelfRoutingTransitionProcessor selfRoutingTransitionProcessor() {
		SelfRoutingTransitionProcessor bean = new SelfRoutingTransitionProcessor();
		return bean;
	}
	
	@Managed
	private StateTaggingTransitionProcessor stateTaggingTransitionProcessor() {
		StateTaggingTransitionProcessor bean = new StateTaggingTransitionProcessor();
		return bean;
	}
	
}