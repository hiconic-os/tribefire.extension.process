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
// ============================================================================
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package tribefire.extension.process.scripting.wire.space;

import com.braintribe.model.processing.deployment.api.ExpertContext;
import com.braintribe.model.processing.deployment.api.binding.DenotationBindingBuilder;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.extension.process.module.wire.contract.ProcessBindersContract;
import tribefire.extension.process.scripting.ScriptedConditionProcessor;
import tribefire.extension.process.scripting.ScriptedTransitionProcessor;
import tribefire.extension.scripting.module.wire.contract.ScriptingContract;
import tribefire.module.wire.contract.TribefireModuleContract;
import tribefire.module.wire.contract.TribefireWebPlatformContract;

@Managed
public class ProcessScriptingModuleSpace implements TribefireModuleContract {

	@Import
	private TribefireWebPlatformContract tfPlatform;
	
	@Import
	private ProcessBindersContract processBinders;
	
	@Import
	private ScriptingContract scripting;

	@Override
	public void bindDeployables(DenotationBindingBuilder bindings) {
		//@formatter:off
		bindings
		.bind(tribefire.extension.process.model.scripting.deployment.ScriptedConditionProcessor.T)
		.component(processBinders.conditionProcessor())
		.expertFactory(this::scriptedConditionProcessor);
		
		bindings
		.bind(tribefire.extension.process.model.scripting.deployment.ScriptedTransitionProcessor.T)
		.component(processBinders.transitionProcessor())
		.expertFactory(this::scriptedTransitionProcessor);
		//@formatter:on
	}
	
	@Managed
	private ScriptedConditionProcessor scriptedConditionProcessor(ExpertContext<tribefire.extension.process.model.scripting.deployment.ScriptedConditionProcessor> context) {
		ScriptedConditionProcessor bean = new ScriptedConditionProcessor();
		
		tribefire.extension.process.model.scripting.deployment.ScriptedConditionProcessor deployable = context.getDeployable();
		
		bean.setDeployable(deployable);
		bean.setScript(deployable.getScript());
		bean.setEngineResolver(scripting.scriptingEngineResolver());
		bean.setSystemSessionFactory(tfPlatform.systemUserRelated().sessionFactory());
		bean.setRequestSessionFactory(tfPlatform.requestUserRelated().sessionFactory());
		bean.setPropertyLookup(tfPlatform.platformReflection()::getProperty);
		
		return bean;
	}
	
	@Managed
	private ScriptedTransitionProcessor scriptedTransitionProcessor(ExpertContext<tribefire.extension.process.model.scripting.deployment.ScriptedTransitionProcessor> context) {
		ScriptedTransitionProcessor bean = new ScriptedTransitionProcessor();
		
		tribefire.extension.process.model.scripting.deployment.ScriptedTransitionProcessor deployable = context.getDeployable();
		
		bean.setDeployable(deployable);
		bean.setScript(deployable.getScript());
		bean.setEngineResolver(scripting.scriptingEngineResolver());
		bean.setSystemSessionFactory(tfPlatform.systemUserRelated().sessionFactory());
		bean.setRequestSessionFactory(tfPlatform.requestUserRelated().sessionFactory());
		bean.setPropertyLookup(tfPlatform.platformReflection()::getProperty);
		
		return bean;
	}

}
