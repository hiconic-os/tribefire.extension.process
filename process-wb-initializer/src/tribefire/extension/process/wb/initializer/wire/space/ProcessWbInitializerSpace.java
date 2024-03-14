
package tribefire.extension.process.wb.initializer.wire.space;

import java.util.List;

import com.braintribe.model.folder.Folder;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.resource.AdaptiveIcon;
import com.braintribe.model.workbench.SimpleQueryAction;
import com.braintribe.model.workbench.WorkbenchPerspective;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.util.Sets;

import tribefire.cortex.initializer.support.wire.space.AbstractInitializerSpace;
import tribefire.extension.process.model.deployment.ConditionProcessor;
import tribefire.extension.process.model.deployment.ProcessDefinition;
import tribefire.extension.process.model.deployment.TransitionProcessor;
import tribefire.extension.process.wb.initializer.wire.contract.ProcessWbInitializerContract;
import tribefire.extension.process.wb.initializer.wire.contract.ProcessWbLookupContract;

@Managed
public class ProcessWbInitializerSpace extends AbstractInitializerSpace implements ProcessWbInitializerContract {

	@Import
	private ProcessWbLookupContract processWbLookup;

	/* To ensure beans are initialized simply reference them here (i.e. invoke their defining methods).  */
	@Override
	public void initialize() {
		WorkbenchPerspective homePerspectivce = processWbLookup.homePerspectivce();
		
		homePerspectivce.getFolders().addAll(List.of(definitionsFolder(), transitionProcessorsFolder(), conditionProcessorsFolder()));

		Folder processingFolder = processWbLookup.processingFolder();
		
		addSubFolder(processingFolder, definitionsFolder());
		addSubFolder(processingFolder, transitionProcessorsFolder());
		addSubFolder(processingFolder, conditionProcessorsFolder());
	}
	
	private static void addSubFolder(Folder parent, Folder child) {
		parent.getSubFolders().add(child);
		child.setParent(parent);
	}
	
	@Managed
	private Folder definitionsFolder() {
		Folder bean = session().createRaw(Folder.T, "1460769f-87a7-4fcd-bc6c-f86bc0fff14f");
		bean.setDisplayName(ls("Definitions"));
		bean.setIcon(processDefinitionIcon());
		bean.setName("Definitions");
		bean.setContent(processDefinitionQueryAction());
		bean.setTags(Sets.set("homeFolder"));
		return bean;
	}
	
	@Managed
	private Folder transitionProcessorsFolder() {
		Folder bean = session().createRaw(Folder.T, "65db72d6-371a-43e8-b1b6-7acdb13521f4");
		bean.setDisplayName(ls("Transition Processors"));
		bean.setIcon(transitionProcessorIcon());
		bean.setName("Transition Processors");
		bean.setContent(transitionProcessorQueryAction());
		return bean;
	}
	
	@Managed
	private Folder conditionProcessorsFolder() {
		Folder bean = session().createRaw(Folder.T, "7f4ab9e0-782c-4720-8c48-5eea0c9ebdb7");
		bean.setDisplayName(ls("Condition Processors"));
		bean.setIcon(conditionProcessorIcon());
		bean.setName("Condition Processors");
		bean.setContent(conditionProcessorQueryAction());
		return bean;
	}
	
	@Managed
	private SimpleQueryAction processDefinitionQueryAction() {
		SimpleQueryAction bean = create(SimpleQueryAction.T);
		bean.setTypeSignature(ProcessDefinition.T.getTypeSignature());
		bean.setDisplayName(ls("Process Definitions Query"));
		return bean;
	}
	
	@Managed
	private SimpleQueryAction transitionProcessorQueryAction() {
		SimpleQueryAction bean = create(SimpleQueryAction.T);
		bean.setTypeSignature(TransitionProcessor.T.getTypeSignature());
		bean.setDisplayName(ls("Transition Processors Query"));
		return bean;
	}
	
	@Managed
	private SimpleQueryAction conditionProcessorQueryAction() {
		SimpleQueryAction bean = create(SimpleQueryAction.T);
		bean.setTypeSignature(ConditionProcessor.T.getTypeSignature());
		bean.setDisplayName(ls("Condition Processors Query"));
		return bean;
	}
	
	@Managed
	private AdaptiveIcon processDefinitionIcon() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "3691dc8b-6ecc-45ed-92ba-fbabf9312382");
		bean.setName("ProcessDefinitionIcon");
		bean.setRepresentations(Sets.set(processWbLookup.processDefinitionIcon16x16(), processWbLookup.processDefinitionIcon24x24(), processWbLookup.processDefinitionIcon32x32(), processWbLookup.processDefinitionIcon64x64()));
		return bean;
	}

	@Managed
	private AdaptiveIcon transitionProcessorIcon() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "c2f6f3d9-7f45-43fd-84e2-3cdbe2136ed0");
		bean.setName("TransitionProcessorIcon");
		bean.setRepresentations(Sets.set(processWbLookup.transitionProcessorIcon16x16(), processWbLookup.transitionProcessorIcon24x24(), processWbLookup.transitionProcessorIcon32x32(), processWbLookup.transitionProcessorIcon64x64()));
		return bean;
	}


	@Managed
	private AdaptiveIcon conditionProcessorIcon() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "7be7cf82-7701-4eba-83e6-53dc4cb0be08");
		bean.setName("ConditionIcon");
		bean.setRepresentations(Sets.set(processWbLookup.conditionProcessorIcon16x16(), processWbLookup.conditionProcessorIcon24x24(), processWbLookup.conditionProcessorIcon32x32(), processWbLookup.conditionProcessorIcon64x64()));
		return bean;
	}

	private LocalizedString ls(String value) {
		LocalizedString ls = create(LocalizedString.T);
		ls.putDefault(value);
		return ls;
	}
}
