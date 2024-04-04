package tribefire.extension.process.processing.base;

import java.io.File;
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

import tribefire.extension.process.model.deployment.ConditionalEdge;
import tribefire.extension.process.model.deployment.DecoupledInteraction;
import tribefire.extension.process.model.deployment.Edge;
import tribefire.extension.process.model.deployment.ProcessDefinition;
import tribefire.extension.process.model.deployment.ProcessElement;
import tribefire.extension.process.model.deployment.StandardNode;
import tribefire.pd.editor.api.ProcessDefinitionEditor;

public class BpmnLab {
	public static void main(String[] args) {
		BpmnModelInstance modelInstance = Bpmn.readModelFromFile(new File("res/diagram.bpmn"));
		ProcessDefinition definition = new BpmnPdTranslation(modelInstance).buildDefintion();
		
		for (ProcessElement element : definition.getElements()) {
			if (element instanceof StandardNode node) {
				StringBuilder builder = new StringBuilder();
				builder.append("Node [");
				builder.append(node.getState());
				builder.append("]");
				
				DecoupledInteraction decoupledInteraction = node.getDecoupledInteraction();
				
				if (decoupledInteraction != null) {
					if (decoupledInteraction.getUserInteraction())						
						builder.append(" Decoupled User Interaction");
					else
						builder.append(" Decoupled Interaction");
				}
				System.out.println(builder);
				
				for (ConditionalEdge edge: node.getConditionalEdges()) {
					String name = edge.getName().value();
					if (name == null)
						name = "else";
		
					System.out.println("  ConditionalEdge " + name + " to [" + edge.getTo().getState() + "]");
				}
			}
		}
		
		System.out.println();
		
		for (ProcessElement element : definition.getElements()) {
			if (element instanceof Edge edge) {
				if (edge instanceof ConditionalEdge)
					continue;
				
				System.out.println("Edge [" + edge.getFrom().getState() + "] -> [" + edge.getTo().getState() + "]");
			}
		}
	}
}

class BpmnPdTranslation {
	private BpmnModelInstance modelInstance;
	private ProcessDefinitionEditor pde = ProcessDefinitionEditor.create();

	public BpmnPdTranslation(BpmnModelInstance modelInstance) {
		super();
		this.modelInstance = modelInstance;
	}
	

	public ProcessDefinition buildDefintion() {
		for (Task task: modelInstance.getModelElementsByType(Task.class)) {
			String name = task.getName();
			StandardNode node = pde.node(name, name);
			
			if (task instanceof UserTask) {
				DecoupledInteraction di = DecoupledInteraction.T.create();
				di.setUserInteraction(true);
				node.setDecoupledInteraction(di);
			}
			else if (task instanceof ServiceTask) {
				node.setDecoupledInteraction(DecoupledInteraction.T.create());
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
