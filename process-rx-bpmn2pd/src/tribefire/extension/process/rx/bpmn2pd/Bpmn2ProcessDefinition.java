package tribefire.extension.process.rx.bpmn2pd;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.Gateway;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;
import org.camunda.bpm.model.bpmn.instance.Task;

import com.braintribe.common.lcd.Pair;
import com.braintribe.model.generic.session.InputStreamProvider;
import com.braintribe.model.resource.Resource;

import tribefire.extension.process.model.configuration.ProcessDefinition;
import tribefire.extension.process.rx.api.ProcessDefinitionEditor;

/** Translates BPMN task graphs directly into the RX process configuration model. */
public final class Bpmn2ProcessDefinition {

	private final BpmnModelInstance model;
	private final ProcessDefinitionEditor editor;
	private final Set<String> edgeNames = new HashSet<>();

	private Bpmn2ProcessDefinition(String processDefinitionId, BpmnModelInstance model) {
		this.model = model;
		this.editor = ProcessDefinitionEditor.create(processDefinitionId, processName(model));
	}

	public static ProcessDefinition translate(String processDefinitionId, Resource resource) {
		return translate(processDefinitionId, resource::openStream);
	}

	public static ProcessDefinition translate(String processDefinitionId, InputStreamProvider inputProvider) {
		try (InputStream input = inputProvider.openInputStream()) {
			return translate(processDefinitionId, input);
		} catch (IOException e) {
			throw new UncheckedIOException("Error while translating BPMN model", e);
		}
	}

	public static ProcessDefinition translate(String processDefinitionId, InputStream input) {
		return translate(processDefinitionId, Bpmn.readModelFromStream(input));
	}

	public static ProcessDefinition translate(String processDefinitionId, BpmnModelInstance model) {
		return new Bpmn2ProcessDefinition(processDefinitionId, model).translate();
	}

	private ProcessDefinition translate() {
		for (Task task : model.getModelElementsByType(Task.class)) {
			String state = state(task);
			editor.node(state, displayName(task));
			editor.decoupledInteraction(state, displayName(task));
		}

		for (SequenceFlow flow : model.getModelElementsByType(SequenceFlow.class)) {
			FlowNode source = flow.getSource();
			FlowNode target = flow.getTarget();
			if (!(source instanceof Task sourceTask))
				continue;

			if (target instanceof Task targetTask) {
				editor.edge(state(sourceTask), state(targetTask), uniqueFlowName(flow, sourceTask, targetTask));
			} else if (target instanceof Gateway gateway) {
				for (Pair<SequenceFlow, Task> targetPair : scanTargetTasks(gateway)) {
					SequenceFlow conditionalFlow = targetPair.first();
					Task targetTask = targetPair.second();
					String edgeName = uniqueFlowName(conditionalFlow, sourceTask, targetTask);
					editor.conditionedEdge(state(sourceTask), state(targetTask), edgeName, edgeName);
				}
			}
		}

		return editor.definition();
	}

	private List<Pair<SequenceFlow, Task>> scanTargetTasks(Gateway gateway) {
		List<Pair<SequenceFlow, Task>> tasks = new ArrayList<>();
		scanTargetTasks(gateway, tasks, new HashSet<>());
		return tasks;
	}

	private void scanTargetTasks(Gateway gateway, List<Pair<SequenceFlow, Task>> tasks, Set<Gateway> visited) {
		if (!visited.add(gateway))
			throw new IllegalArgumentException("Cyclic gateway graph at BPMN element: " + gateway.getId());

		List<Gateway> nextGateways = new LinkedList<>();
		List<Pair<SequenceFlow, Task>> localTasks = new LinkedList<>();
		for (SequenceFlow flow : gateway.getOutgoing()) {
			FlowNode target = flow.getTarget();
			if (target instanceof Task task)
				localTasks.add(Pair.of(flow, task));
			else if (target instanceof Gateway nextGateway)
				nextGateways.add(nextGateway);
		}

		localTasks.sort((a, b) -> flowName(a.first()).compareTo(flowName(b.first())));
		tasks.addAll(localTasks);
		for (Gateway nextGateway : nextGateways)
			scanTargetTasks(nextGateway, tasks, visited);
	}

	private String uniqueFlowName(SequenceFlow flow, Task source, Task target) {
		String base = flowName(flow);
		if (base.isBlank())
			base = state(source) + " -> " + state(target);
		String candidate = base;
		for (int suffix = 2; !edgeNames.add(candidate); suffix++)
			candidate = base + " (" + suffix + ")";
		return candidate;
	}

	private static String processName(BpmnModelInstance model) {
		return model.getDefinitions() == null || blank(model.getDefinitions().getName())
				? "BPMN process"
				: model.getDefinitions().getName();
	}

	private static String displayName(Task task) {
		return blank(task.getName()) ? state(task) : task.getName();
	}

	private static String state(Task task) {
		if (!blank(task.getName()))
			return task.getName();
		if (!blank(task.getId()))
			return task.getId();
		throw new IllegalArgumentException("A BPMN task needs a name or id");
	}

	private static String flowName(SequenceFlow flow) {
		return blank(flow.getName()) ? "" : flow.getName();
	}

	private static boolean blank(String value) {
		return value == null || value.isBlank();
	}
}
