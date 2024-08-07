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
package tribefire.extension.process.initializer.wire.space;

import com.braintribe.model.extensiondeployment.meta.ProcessWith;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.data.constraint.Modifiable;
import com.braintribe.model.meta.selector.RoleSelector;
import com.braintribe.model.processing.meta.editor.EntityTypeMetaDataEditor;
import com.braintribe.model.processing.meta.editor.ModelMetaDataEditor;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.cortex.initializer.support.wire.space.AbstractInitializerSpace;
import tribefire.extension.process._ProcessApiModel_;
import tribefire.extension.process._ProcessDataModel_;
import tribefire.extension.process.api.model.ProcessManagerRequest;
import tribefire.extension.process.data.model.ProcessItem;
import tribefire.extension.process.initializer.exports.ProcessInitializerExportsContract;
import tribefire.extension.process.model.deployment.ProcessManager;

@Managed
public class ProcessModelsSpace extends AbstractInitializerSpace implements ProcessInitializerExportsContract {

	@Managed
	@Override
	public GmMetaModel configuredApiModel() {
		GmMetaModel bean = buildConfiguredModelFor(_ProcessApiModel_.reflection).get();
		
		ModelMetaDataEditor editor = modelApi.newMetaDataEditor(bean).done();
		editor.onEntityType(ProcessManagerRequest.T).addMetaData(processWithProcessManager());
		
		return bean;
	}

	@Managed
	@Override
	public GmMetaModel configuredDataModel() {
		GmMetaModel bean = buildConfiguredModelFor(_ProcessDataModel_.reflection).get();
		ModelMetaDataEditor editor = modelApi.newMetaDataEditor(bean).done();
		EntityTypeMetaDataEditor onProcessItemType = editor.onEntityType(ProcessItem.T);

		String[] priviledgedProperties = { //
			ProcessItem.state, ProcessItem.previousState, ProcessItem.activity, ProcessItem.transitionPhase, ProcessItem.transitionProcessorId, //
			ProcessItem.startedAt, ProcessItem.lastTransit, ProcessItem.endedAt, ProcessItem.logSequence //
		};

		Modifiable internallyModifiable = internallyModifiable();
		
		for (String priviledgedProperty: priviledgedProperties) {
			onProcessItemType.addPropertyMetaData(priviledgedProperty, internallyModifiable);
		}
		return bean;
	}
	
	@Managed
	private Modifiable internallyModifiable() {
		Modifiable bean = create(Modifiable.T);
		bean.setSelector(internalRoleSelector());
		return bean;
	}
	
	@Managed
	private RoleSelector internalRoleSelector() {
		RoleSelector bean = create(RoleSelector.T);
		bean.getRoles().add("tf-internal");
		return bean;
	}

	
	@Managed
	private ProcessManager processManager() {
		ProcessManager bean = create(ProcessManager.T);
		bean.setName("Process Manager");
		bean.setExternalId("processor.processManager");
		return bean;
	}
	
	@Managed
	private ProcessWith processWithProcessManager() {
		ProcessWith bean = create(ProcessWith .T);
		bean.setProcessor(processManager());
		return bean;
	}


}
