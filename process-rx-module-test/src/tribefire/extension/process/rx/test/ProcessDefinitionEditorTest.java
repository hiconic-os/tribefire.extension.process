package tribefire.extension.process.rx.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.Test;

import tribefire.extension.process.model.configuration.Condition;
import tribefire.extension.process.model.configuration.Edge;
import tribefire.extension.process.model.configuration.Node;
import tribefire.extension.process.model.configuration.ProcessDefinition;
import tribefire.extension.process.rx.api.ProcessDefinitionEditor;

/**
 * The editor, without any platform. What matters here is where an edge ends up, because the position in
 * {@link Node#getEdges()} decides the routing priority among conditions.
 */
public class ProcessDefinitionEditorTest {

	private static final String DEFINITION_ID = "test-process";

	@Test
	public void createdDefinitionCarriesIdAndName() {
		ProcessDefinition definition = ProcessDefinitionEditor.create(DEFINITION_ID, "Test Process").definition();

		assertThat(definition.getProcessDefinitionId()).isEqualTo(DEFINITION_ID);
		assertThat(definition.getName()).isEqualTo("Test Process");
	}

	@Test
	public void definitionCreatedWithIdOnlyUsesIdAsName() {
		ProcessDefinition definition = ProcessDefinitionEditor.create(DEFINITION_ID).definition();

		assertThat(definition.getName()).isEqualTo(DEFINITION_ID);
	}

	@Test
	public void edgeIsCreatedOnTheNodeItLeaves() {
		ProcessDefinitionEditor editor = editor();
		editor.edge("a", "b", "a-b");

		assertThat(states(editor, "a")).containsExactly("b");
		assertThat(editor.requireNode("b").getEdges()).isEmpty();
	}

	@Test
	public void rootEdgeLeavesTheNodeWithoutState() {
		ProcessDefinitionEditor editor = editor();
		editor.rootEdge("a", "root-a");

		assertThat(editor.rootNode().getEdges()).extracting(Edge::getName).containsExactly("root-a");
	}

	@Test
	public void edgeIsAppendedAndPrependedEdgeComesFirst() {
		ProcessDefinitionEditor editor = editor();
		editor.edge("a", "b", "a-b");
		editor.edge("a", "c", "a-c");
		editor.prependEdge("a", "d", "a-d");

		assertThat(states(editor, "a")).containsExactly("d", "b", "c");
	}

	@Test
	public void prependedConditionIsEvaluatedFirst() {
		ProcessDefinitionEditor editor = editor();
		editor.conditionedEdge("a", "b", "a-b", "condition.b");
		editor.prependConditionedEdge("a", "cancel", "a-cancel", "condition.cancel");

		assertThat(editor.requireNode("a").getEdges()) //
				.extracting(edge -> edge.getCondition().getConditionProcessorId()) //
				.containsExactly("condition.cancel", "condition.b");
	}

	@Test
	public void conditionedEdgeTakesAProcessorIdOrACondition() {
		ProcessDefinitionEditor editor = editor();
		Edge byProcessor = editor.conditionedEdge("a", "b", "a-b", "condition.b");
		Edge byExpression = editor.conditionedEdge("a", "c", "a-c", Condition.expression(false));

		assertThat(byProcessor.getCondition().getConditionProcessorId()).isEqualTo("condition.b");
		assertThat(byExpression.getCondition().getConditionExpression()).isEqualTo(Condition.FALSE_EXPRESSION);
	}

	@Test
	public void sameEdgeNameReturnsTheExistingEdge() {
		ProcessDefinitionEditor editor = editor();
		Edge first = editor.edge("a", "b", "a-b");
		Edge second = editor.edge("a", "b", "a-b");

		assertThat(second).isSameAs(first);
		assertThat(editor.requireNode("a").getEdges()).hasSize(1);
	}

	@Test
	public void sameEdgeNameWithOtherEndpointsIsRejected() {
		ProcessDefinitionEditor editor = editor();
		editor.edge("a", "b", "a-b");

		assertThatThrownBy(() -> editor.edge("a", "c", "a-b")) //
				.isInstanceOf(IllegalArgumentException.class) //
				.hasMessageContaining("a-b");
	}

	@Test
	public void removedEdgeIsGoneFromItsNode() {
		ProcessDefinitionEditor editor = editor();
		editor.edge("a", "b", "a-b");
		editor.edge("a", "c", "a-c");

		editor.removeEdge("a-b");

		assertThat(states(editor, "a")).containsExactly("c");
		assertThatThrownBy(() -> editor.requireEdge("a-b")).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public void removedEdgeNameCanBeUsedAgain() {
		ProcessDefinitionEditor editor = editor();
		editor.edge("a", "b", "a-b");
		editor.removeEdge("a-b");

		editor.edge("a", "c", "a-b");

		assertThat(states(editor, "a")).containsExactly("c");
	}

	@Test
	public void editOfAnExistingDefinitionFindsItsNodesAndEdges() {
		ProcessDefinitionEditor owner = editor();
		owner.edge("a", "b", "a-b");

		ProcessDefinitionEditor extension = ProcessDefinitionEditor.edit(owner.definition());
		extension.prependConditionedEdge("a", "c", "a-c", "condition.c");

		assertThat(extension.requireEdge("a-b")).isNotNull();
		assertThat(states(extension, "a")).containsExactly("c", "b");
	}

	@Test
	public void nodesAndProcessorsAreAcquiredOnce() {
		ProcessDefinitionEditor editor = editor();
		editor.onEnter("a", "processor.entered");
		editor.onLeave("a", "processor.left");
		editor.edge("a", "b", "a-b");
		editor.onTransit("a-b", "processor.transit");

		Node a = editor.requireNode("a");
		assertThat(editor.acquireNode("a")).isSameAs(a);
		assertThat(a.getOnEntered()).extracting(reference -> reference.getProcessorId()).containsExactly("processor.entered");
		assertThat(a.getOnLeft()).extracting(reference -> reference.getProcessorId()).containsExactly("processor.left");
		assertThat(editor.requireEdge("a-b").getOnTransit()).extracting(reference -> reference.getProcessorId())
				.containsExactly("processor.transit");
	}

	@Test
	public void blankEdgeNameIsRejected() {
		ProcessDefinitionEditor editor = editor();

		assertThatThrownBy(() -> editor.edge("a", "b", " ")).isInstanceOf(IllegalArgumentException.class);
	}

	private static ProcessDefinitionEditor editor() {
		return ProcessDefinitionEditor.create(DEFINITION_ID, "Test Process");
	}

	private static Iterable<String> states(ProcessDefinitionEditor editor, String fromState) {
		return editor.requireNode(fromState).getEdges().stream().map(edge -> edge.getTo().getState()).toList();
	}
}
