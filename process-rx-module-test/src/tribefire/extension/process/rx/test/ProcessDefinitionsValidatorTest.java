package tribefire.extension.process.rx.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;

import org.junit.Test;

import tribefire.extension.process.model.configuration.Condition;
import tribefire.extension.process.model.configuration.Edge;
import tribefire.extension.process.model.configuration.Node;
import tribefire.extension.process.model.configuration.ProcessDefinition;
import tribefire.extension.process.model.configuration.ProcessDefinitionsConfiguration;
import tribefire.extension.process.rx.api.ProcessDefinitionEditor;
import tribefire.extension.process.rx.impl.ProcessDefinitionsValidator;

/**
 * The startup check of the configuration. One test per rejection, plus the cases that must stay accepted, because they are
 * the ones a graph author is most likely to fear needlessly.
 */
public class ProcessDefinitionsValidatorTest {

	@Test
	public void wellFormedConfigurationPasses() {
		ProcessDefinitionEditor editor = editor("test-process");
		editor.rootEdge("check", "root-check");
		editor.conditionedEdge("check", "cancelled", "check-cancelled", "condition.cancelled");
		editor.edge("check", "done", "check-done");

		ProcessDefinitionsValidator.validate(configuration(editor.definition()));
	}

	@Test
	public void nodeWithoutADefaultEdgeIsAccepted() {
		ProcessDefinitionEditor editor = editor("test-process");
		editor.rootEdge("check", "root-check");
		editor.conditionedEdge("check", "yes", "check-yes", "condition.yes");
		editor.conditionedEdge("check", "no", "check-no", "condition.no");

		ProcessDefinitionsValidator.validate(configuration(editor.definition()));
	}

	@Test
	public void edgeThatOnlyAProcessorMayTakeIsAccepted() {
		ProcessDefinitionEditor editor = editor("test-process");
		editor.rootEdge("check", "root-check");
		editor.conditionedEdge("check", "manual", "check-manual", Condition.expression(false));
		editor.edge("check", "done", "check-done");

		ProcessDefinitionsValidator.validate(configuration(editor.definition()));
	}

	@Test
	public void missingProcessDefinitionIdIsRejected() {
		ProcessDefinition definition = editor("test-process").definition();
		definition.setProcessDefinitionId(null);

		assertThatThrownBy(() -> ProcessDefinitionsValidator.validate(configuration(definition))) //
				.isInstanceOf(IllegalStateException.class) //
				.hasMessageContaining("processDefinitionId");
	}

	@Test
	public void duplicateProcessDefinitionIdIsRejected() {
		ProcessDefinition one = editor("same-id").definition();
		ProcessDefinition other = editor("same-id").definition();

		assertThat(problems(configuration(one, other))).contains("Duplicate process definition id: same-id");
	}

	@Test
	public void edgeWithoutANameIsRejected() {
		ProcessDefinitionEditor editor = editor("test-process");
		Edge edge = editor.edge("check", "done", "check-done");
		edge.setName(null);

		assertThat(problems(configuration(editor.definition()))).contains("has no name");
	}

	@Test
	public void duplicateEdgeNameIsRejected() {
		ProcessDefinitionEditor editor = editor("test-process");
		editor.edge("check", "done", "check-done");
		Edge clash = Edge.T.create();
		clash.setName("check-done");
		clash.setTo(editor.requireNode("done"));
		editor.acquireNode("other").getEdges().add(clash);

		assertThat(problems(configuration(editor.definition()))).contains("duplicate edge name [check-done]");
	}

	@Test
	public void edgeToAnUnknownStateIsRejected() {
		ProcessDefinitionEditor editor = editor("test-process");
		Edge edge = editor.edge("check", "done", "check-done");
		Node foreign = Node.T.create();
		foreign.setState("elsewhere");
		edge.setTo(foreign);

		assertThat(problems(configuration(editor.definition()))).contains("elsewhere");
	}

	@Test
	public void edgeWithoutATargetIsRejected() {
		ProcessDefinitionEditor editor = editor("test-process");
		editor.edge("check", "done", "check-done").setTo(null);

		assertThat(problems(configuration(editor.definition()))).contains("has no target node");
	}

	@Test
	public void twoEdgesWithoutAConditionAreRejected() {
		ProcessDefinitionEditor editor = editor("test-process");
		editor.edge("check", "done", "check-done");
		editor.edge("check", "cancelled", "check-cancelled");

		assertThat(problems(configuration(editor.definition()))).contains("edges without a condition");
	}

	@Test
	public void unsupportedConditionExpressionIsRejected() {
		ProcessDefinitionEditor editor = editor("test-process");
		editor.conditionedEdge("check", "done", "check-done", expression("maybe"));

		assertThat(problems(configuration(editor.definition()))).contains("maybe");
	}

	@Test
	public void emptyConditionIsRejected() {
		ProcessDefinitionEditor editor = editor("test-process");
		editor.conditionedEdge("check", "done", "check-done", Condition.T.create());

		assertThat(problems(configuration(editor.definition()))).contains("neither an expression nor a condition processor id");
	}

	@Test
	public void allProblemsAreReportedAtOnce() {
		ProcessDefinitionEditor editor = editor("test-process");
		editor.edge("check", "done", "check-done");
		editor.edge("check", "cancelled", "check-cancelled");
		editor.conditionedEdge("done", "check", "done-check", expression("maybe"));

		String message = problems(configuration(editor.definition()));

		assertThat(message).contains("edges without a condition");
		assertThat(message).contains("maybe");
	}

	private static Condition expression(String value) {
		Condition condition = Condition.T.create();
		condition.setConditionExpression(value);
		return condition;
	}

	private static String problems(ProcessDefinitionsConfiguration configuration) {
		Throwable thrown = catchThrowable(() -> ProcessDefinitionsValidator.validate(configuration));

		assertThat(thrown).isInstanceOf(IllegalStateException.class);
		return thrown.getMessage();
	}

	private static ProcessDefinitionsConfiguration configuration(ProcessDefinition... definitions) {
		ProcessDefinitionsConfiguration configuration = ProcessDefinitionsConfiguration.T.create();
		for (ProcessDefinition definition : definitions)
			configuration.getDefinitions().add(definition);

		return configuration;
	}

	private static ProcessDefinitionEditor editor(String processDefinitionId) {
		return ProcessDefinitionEditor.create(processDefinitionId, "Test Process");
	}
}
