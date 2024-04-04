package tribefire.extension.process.bmpn2pd;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.Gateway;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;
import org.camunda.bpm.model.bpmn.instance.ServiceTask;
import org.camunda.bpm.model.bpmn.instance.Task;
import org.camunda.bpm.model.bpmn.instance.UserTask;

import com.braintribe.common.lcd.Pair;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.session.InputStreamProvider;
import com.braintribe.model.resource.Resource;

import tribefire.extension.process.model.deployment.DecoupledInteraction;
import tribefire.extension.process.model.deployment.ProcessDefinition;
import tribefire.extension.process.model.deployment.StandardNode;
import tribefire.pd.editor.api.ProcessDefinitionEditor;


public class Bpmn2Pd {
	private BpmnModelInstance modelInstance;
	private ProcessDefinitionEditor pde = ProcessDefinitionEditor.create();

	public static ProcessDefinition translate(Resource resource) {
		return translate(resource::openStream);
	}
	
	public static ProcessDefinition translate(InputStreamProvider inProvider) {
		try (InputStream in = inProvider.openInputStream()) {
			return translate(in);
		} catch (IOException e) {
			throw new UncheckedIOException("Error while translating BPMN model from input stream", e);
		}
	}
	
	public static ProcessDefinition translate(InputStream in) {
		BpmnModelInstance modelInstance = Bpmn.readModelFromStream(in);
		return translate(modelInstance);
	}
	
	public static ProcessDefinition translate(BpmnModelInstance modelInstance) {
		Bpmn2Pd translater = new Bpmn2Pd(modelInstance);
		return translater.buildDefintion();
	}
	
	public Bpmn2Pd(BpmnModelInstance modelInstance) {
		super();
		this.modelInstance = modelInstance;
	}
	
	private <T extends GenericEntity> T create(EntityType<T> type) {
		return type.create();
	}
	
	public ProcessDefinition buildDefintion() {
		for (Task task: modelInstance.getModelElementsByType(Task.class)) {
			String name = task.getName();
			StandardNode node = pde.node(name, name);
			
			if (task instanceof UserTask) {
				DecoupledInteraction di = create(DecoupledInteraction.T);;
				di.setUserInteraction(true);
				node.setDecoupledInteraction(di);
			}
			else if (task instanceof ServiceTask) {
				node.setDecoupledInteraction(create(DecoupledInteraction.T));
			}
		}
		
		for (SequenceFlow node: modelInstance.getModelElementsByType(SequenceFlow.class)) {
			FlowNode source = node.getSource();
			FlowNode target = node.getTarget();
			
			if (source instanceof Task sourceTask) {
				if (target instanceof Task targetTask) {
					pde.edge(getState(sourceTask), getState(targetTask), node.getName());
				}
				else if (target instanceof Gateway gateway) {
					for (Pair<String, Task> targetPair: scanTargetTasks(gateway)) {
						String edgeName = targetPair.first();
						Task targetTask = targetPair.second();
						pde.conditionalEdge(getState(sourceTask), getState(targetTask), edgeName);
					}
				}
			}
		}
		
		return pde.definition();
	}
	
	private List<Pair<String, Task>> scanTargetTasks(Gateway gateway) {
		List<Pair<String, Task>> tasks = new ArrayList<>();
		
		scanTargetTasks(gateway, tasks);
		return tasks;
	}
	
	private void scanTargetTasks(Gateway gateway, List<Pair<String, Task>> tasks) {
		List<Gateway> nextGateways = new LinkedList<>();
		List<Pair<String, Task>> localTasks = new LinkedList<>();
		
		for (SequenceFlow flow: gateway.getOutgoing()) {
			FlowNode flowNode = flow.getTarget();
			
			if (flowNode instanceof Task task) {
				localTasks.add(Pair.of(flow.getName(), task));
			}
			else if (flowNode instanceof Gateway targetGateway) {
				nextGateways.add(targetGateway);
			}
		}
		
		localTasks.sort((p1, p2) -> compareEdgeNames(p1.first(), p2.first()));
		
		tasks.addAll(localTasks);
		
		for (Gateway nextGateway: nextGateways) {
			scanTargetTasks(nextGateway, tasks);
		}
	}
	
	private int compareEdgeNames(String n1, String n2) {
		if (n1 == n2)
			return 0;
		
		if (n1 == null)
			return 1;
		
		if (n2 == null)
			return -1;

		return n1.compareTo(n2);
	}

	private String getState(Task e) {
		return e.getName();
	}
}
