package tribefire.extension.process.rx.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import tribefire.extension.process.model.configuration.Condition;
import tribefire.extension.process.model.configuration.Edge;
import tribefire.extension.process.model.configuration.Node;
import tribefire.extension.process.model.configuration.ProcessDefinition;
import tribefire.extension.process.model.configuration.ProcessDefinitionsConfiguration;

/**
 * Checks the configured process definitions once, at startup, so that a broken graph is not found only when the first
 * process runs into it. Every problem is collected, so one start reports all of them.
 * <p>
 * A node without an edge that has no condition is <b>not</b> a problem: its conditions may cover every case, or a
 * transition processor may choose the next state. A node with several such edges is one, because routing cannot choose
 * between them.
 */
public class ProcessDefinitionsValidator {

	public static void validate(ProcessDefinitionsConfiguration configuration) {
		List<String> problems = new ArrayList<>();
		Set<String> knownIds = new HashSet<>();

		for (ProcessDefinition definition : configuration.getDefinitions()) {
			String id = definition.getProcessDefinitionId();

			if (id == null || id.isBlank()) {
				problems.add("A process definition has no processDefinitionId. Its name is: " + definition.getName());
				continue;
			}

			if (!knownIds.add(id))
				problems.add("Duplicate process definition id: " + id);

			validateDefinition(definition, id, problems);
		}

		if (!problems.isEmpty())
			throw new IllegalStateException("Invalid process definitions:" + System.lineSeparator() + String.join(System.lineSeparator(), problems));
	}

	private static void validateDefinition(ProcessDefinition definition, String definitionId, List<String> problems) {
		Set<String> states = new HashSet<>();
		Set<String> edgeNames = new HashSet<>();

		for (Node node : definition.getNodes())
			if (!states.add(node.getState()))
				problems.add(definitionId + ": duplicate node state [" + node.getState() + "]");

		for (Node node : definition.getNodes()) {
			int defaultEdges = 0;

			for (Edge edge : node.getEdges()) {
				String edgeInfo = definitionId + ": edge [" + edge.getName() + "] from state [" + node.getState() + "]";

				if (edge.getName() == null || edge.getName().isBlank())
					problems.add(definitionId + ": an edge from state [" + node.getState() + "] has no name");
				else if (!edgeNames.add(edge.getName()))
					problems.add(definitionId + ": duplicate edge name [" + edge.getName() + "]");

				if (edge.getTo() == null)
					problems.add(edgeInfo + " has no target node");
				else if (!states.contains(edge.getTo().getState()))
					problems.add(edgeInfo + " leads to state [" + edge.getTo().getState() + "], which is not a node of this definition");

				Condition condition = edge.getCondition();

				if (condition == null)
					defaultEdges++;
				else
					validateCondition(condition, edgeInfo, problems);
			}

			if (defaultEdges > 1)
				problems.add(definitionId + ": state [" + node.getState() + "] has " + defaultEdges
						+ " edges without a condition, so routing cannot choose between them");
		}
	}

	private static void validateCondition(Condition condition, String edgeInfo, List<String> problems) {
		String expression = condition.getConditionExpression();
		boolean hasExpression = expression != null && !expression.isBlank();
		boolean hasProcessor = condition.getConditionProcessorId() != null && !condition.getConditionProcessorId().isBlank();

		if (hasExpression) {
			String trimmed = expression.trim();
			if (!Condition.TRUE_EXPRESSION.equals(trimmed) && !Condition.FALSE_EXPRESSION.equals(trimmed))
				problems.add(edgeInfo + " has the unsupported condition expression [" + expression + "]. Supported are ["
						+ Condition.TRUE_EXPRESSION + "] and [" + Condition.FALSE_EXPRESSION + "].");

		} else if (!hasProcessor) {
			problems.add(edgeInfo + " has a condition with neither an expression nor a condition processor id");
		}
	}
}
