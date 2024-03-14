
package tribefire.extension.process.wb.initializer.wire.contract;

import com.braintribe.model.folder.Folder;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.workbench.WorkbenchPerspective;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.cortex.initializer.support.impl.lookup.GlobalId;
import tribefire.cortex.initializer.support.impl.lookup.InstanceLookup;

@InstanceLookup(lookupOnly = true)
public interface ProcessWbLookupContract extends WireSpace {

	String GROUP_ID = "tribefire.extension.process";
	String PROCESS_WB_RESOURCES_PREFIX = "asset-resource://tribefire.extension.process:process-wb-resources/";

//  EXAMPLE:
//
//	@GlobalId("model:com.braintribe.gm:root-model")
//	GmMetaModel rootModel();
//
//  MAKE SURE TO IMPORT [tribefire.cortex.initializer.support.impl.lookup.GlobalId]

	@GlobalId("7b9c77a7-70dc-4707-8d7b-adc071bbc734")
	Folder processingFolder();
	
	@GlobalId("6c87b267-e7ff-4d0d-8c27-b40aa2622a5d")
	WorkbenchPerspective homePerspectivce();
	
	@GlobalId(PROCESS_WB_RESOURCES_PREFIX + "transition-processor-16x16.png") 
	Resource transitionProcessorIcon16x16();
	
	@GlobalId(PROCESS_WB_RESOURCES_PREFIX + "transition-processor-24x24.png") 
	Resource transitionProcessorIcon24x24();
	
	@GlobalId(PROCESS_WB_RESOURCES_PREFIX + "transition-processor-32x32.png") 
	Resource transitionProcessorIcon32x32();
	
	@GlobalId(PROCESS_WB_RESOURCES_PREFIX + "transition-processor-64x64.png") 
	Resource transitionProcessorIcon64x64();
	
	@GlobalId(PROCESS_WB_RESOURCES_PREFIX + "condition-processor-16x16.png") 
	Resource conditionProcessorIcon16x16();
	
	@GlobalId(PROCESS_WB_RESOURCES_PREFIX + "condition-processor-24x24.png") 
	Resource conditionProcessorIcon24x24();
	
	@GlobalId(PROCESS_WB_RESOURCES_PREFIX + "condition-processor-32x32.png") 
	Resource conditionProcessorIcon32x32();
	
	@GlobalId(PROCESS_WB_RESOURCES_PREFIX + "condition-processor-64x64.png") 
	Resource conditionProcessorIcon64x64();
	
	@GlobalId(PROCESS_WB_RESOURCES_PREFIX + "process-definition-16x16.png") 
	Resource processDefinitionIcon16x16();
	
	@GlobalId(PROCESS_WB_RESOURCES_PREFIX + "process-definition-24x24.png") 
	Resource processDefinitionIcon24x24();
	
	@GlobalId(PROCESS_WB_RESOURCES_PREFIX + "process-definition-32x32.png") 
	Resource processDefinitionIcon32x32();
	
	@GlobalId(PROCESS_WB_RESOURCES_PREFIX + "process-definition-64x64.png") 
	Resource processDefinitionIcon64x64();
}
