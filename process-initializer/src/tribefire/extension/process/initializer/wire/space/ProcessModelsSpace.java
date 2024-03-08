// ============================================================================
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
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
