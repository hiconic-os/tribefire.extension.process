package tribefire.extension.process.rx.bpmn2pd;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.Gateway;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;
import org.camunda.bpm.model.bpmn.instance.ServiceTask;
import org.camunda.bpm.model.bpmn.instance.Task;
import org.camunda.bpm.model.bpmn.instance.UserTask;

import com.braintribe.common.lcd.Pair;
import com.braintribe.model.generic.session.InputStreamProvider;
import com.braintribe.model.resource.Resource;

import tribefire.extension.process.model.configuration.ProcessDefinition;
import tribefire.extension.process.rx.api.ProcessDefinitionEditor;

/**
 * Translates a BPMN diagram into the RX process configuration model.
 * <p>
 * A box of the diagram becomes a {@link tribefire.extension.process.model.configuration.Node node}, named after the
 * label of the box, and an arrow becomes an {@link tribefire.extension.process.model.configuration.Edge edge}. Two rules
 * are worth knowing:
 * <ul>
 * <li>the box <b>without a label</b> becomes the node without a state, which is where a process starts.
 * <li>a box that stands for work outside the engine - a user task or a service task in BPMN - becomes a node that
 * {@link tribefire.extension.process.model.configuration.Node#getDecoupledInteraction() waits}. Every other kind of box
 * becomes a node that the engine passes through.
 * </ul>
 * A diamond of the diagram, a BPMN gateway, is not a node. It becomes the several edges that leave the box before it,
 * in the order in which the engine considers them: the labelled arrows first, sorted by label, then the unlabelled one.
 * A chain of diamonds behind one box is flattened, diamond after diamond.
 * <p>
 * The label of an arrow is a question for the reader of the diagram, not the code that answers it. So no edge gets a
 * {@link tribefire.extension.process.model.configuration.Edge#getCondition() condition} here. The application attaches
 * the condition processors afterwards, addressing an edge by its endpoints or by its name.
 * <p>
 * This is the RX counterpart of {@code Bpmn2Pd}, and it answers for a diagram in the same way. The difference in the
 * models is that cortex has two kinds of edge, one with a condition and one without, while RX has one kind with an
 * optional condition. An edge without a condition is the one the engine takes when no condition matched.
 */
public final class Bpmn2ProcessDefinition {

	/** How the node without a state is written where a name is needed. */
	private static final String ROOT = "root";

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
			editor.node(state, task.getName());

			if (task instanceof UserTask || task instanceof ServiceTask)
				editor.decoupledInteraction(state, task.getName());
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
					SequenceFlow gatewayFlow = targetPair.first();
					Task targetTask = targetPair.second();
					editor.edge(state(sourceTask), state(targetTask), uniqueFlowName(gatewayFlow, sourceTask, targetTask));
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

		localTasks.sort(Comparator.comparing((Pair<SequenceFlow, Task> pair) -> label(pair.first()), UNLABELLED_LAST));
		tasks.addAll(localTasks);

		for (Gateway nextGateway : nextGateways)
			scanTargetTasks(nextGateway, tasks, visited);
	}

	/**
	 * The labelled arrows of one diamond come first, sorted by label, and the unlabelled arrow comes last. The
	 * unlabelled arrow is the one the engine takes when no condition matched, so its place is the end of the list.
	 */
	private static final Comparator<String> UNLABELLED_LAST = (label1, label2) -> {
		if (label1 == null)
			return label2 == null ? 0 : 1;
		if (label2 == null)
			return -1;
		return label1.compareTo(label2);
	};

	/**
	 * RX identifies an edge by its name, so every edge needs one: the label of the arrow, or the two states it connects
	 * when the arrow carries no label.
	 */
	private String uniqueFlowName(SequenceFlow flow, Task source, Task target) {
		String label = label(flow);
		String base = label != null ? label : name(state(source)) + " -> " + name(state(target));

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

	/** The state of the node that this box becomes. A box without a label becomes the node without a state. */
	private static String state(Task task) {
		return blank(task.getName()) ? null : task.getName();
	}

	private static String name(String state) {
		return state == null ? ROOT : state;
	}

	private static String label(SequenceFlow flow) {
		return blank(flow.getName()) ? null : flow.getName();
	}

	private static boolean blank(String value) {
		return value == null || value.isBlank();
	}
}
