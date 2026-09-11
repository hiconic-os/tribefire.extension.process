package tribefire.extension.process.rx.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.NoSuchElementException;

import org.junit.Test;

import tribefire.extension.process.model.configuration.Edge;
import tribefire.extension.process.model.configuration.Node;
import tribefire.extension.process.rx.api.ProcessDefinitionEditor;
import tribefire.extension.process.rx.processing.oracle.ProcessOracle;
import tribefire.extension.process.rx.processing.oracle.TransitionOracle;

/** The index that the engine builds over a definition, in particular the edge order and the drain nodes. */
public class ProcessOracleTest {

	@Test
	public void outgoingEdgesKeepTheOrderOfTheNode() {
		ProcessDefinitionEditor editor = editor();
		editor.conditionedEdge("check", "received", "check-received", "condition.received");
		editor.edge("check", "wait", "check-wait");
		editor.prependConditionedEdge("check", "cancelled", "check-cancelled", "condition.cancelled");

		ProcessOracle oracle = new ProcessOracle(editor.definition());

		assertThat(oracle.outgoingEdges("check")).extracting(edge -> edge.getTo().getState()) //
				.containsExactly("cancelled", "received", "wait");
	}

	@Test
	public void drainNodesAreTheNodesWithoutEdges() {
		ProcessDefinitionEditor editor = editor();
		editor.rootEdge("check", "root-check");
		editor.edge("check", "done", "check-done");

		ProcessOracle oracle = new ProcessOracle(editor.definition());

		assertThat(oracle.drainNodes).extracting(Node::getState).containsExactly("done");
		assertThat(oracle.outgoingEdges("done")).isEmpty();
	}

	@Test
	public void rootNodeIsIndexedUnderTheNullState() {
		ProcessDefinitionEditor editor = editor();
		editor.rootEdge("check", "root-check");

		ProcessOracle oracle = new ProcessOracle(editor.definition());

		assertThat(oracle.hasState(null)).isTrue();
		assertThat(oracle.hasEdge(null, "check")).isTrue();
		assertThat(oracle.nodeByState.get(null).getState()).isNull();
	}

	@Test
	public void declaredStateChangesAreFound() {
		ProcessDefinitionEditor editor = editor();
		editor.edge("check", "done", "check-done");

		ProcessOracle oracle = new ProcessOracle(editor.definition());

		assertThat(oracle.hasEdge("check", "done")).isTrue();
		assertThat(oracle.hasEdge("done", "check")).isFalse();
		assertThat(oracle.hasState("nowhere")).isFalse();
	}

	@Test
	public void transitionOracleOfADeclaredEdgeKnowsBothNodes() {
		ProcessDefinitionEditor editor = editor();
		Edge edge = editor.edge("check", "done", "check-done");

		TransitionOracle transition = new ProcessOracle(editor.definition()).transitionOracle("check", "done");

		assertThat(transition.getFromState()).isEqualTo("check");
		assertThat(transition.getToState()).isEqualTo("done");
		assertThat(transition.getEdge()).isSameAs(edge);
	}

	@Test
	public void transitionOracleOfAnUndeclaredChangeIsRefused() {
		ProcessOracle oracle = new ProcessOracle(editor().definition());

		assertThatThrownBy(() -> oracle.transitionOracle("check", "done")).isInstanceOf(NoSuchElementException.class);
	}

	@Test
	public void implicitTransitionIsAllowedWhenAsked() {
		ProcessDefinitionEditor editor = editor();
		editor.acquireNode("check");

		TransitionOracle transition = new ProcessOracle(editor.definition()).transitionOracle("check", "check", true);

		assertThat(transition.getEdge()).isNull();
		assertThat(transition.getToState()).isEqualTo("check");
	}

	@Test
	public void transitionProcessorsRunInTheDocumentedOrder() {
		ProcessDefinitionEditor editor = editor();
		editor.edge("check", "done", "check-done");
		editor.definition().getOnTransit().add(ProcessDefinitionEditor.transitionProcessor("processor.definition"));
		editor.onLeave("check", "processor.left");
		editor.onTransit("check-done", "processor.edge");
		editor.onEnter("done", "processor.entered");

		TransitionOracle transition = new ProcessOracle(editor.definition()).transitionOracle("check", "done");

		assertThat(transition.getTransitionProcessors()).extracting(reference -> reference.getProcessorId()) //
				.containsExactly("processor.definition", "processor.left", "processor.edge", "processor.entered");
	}

	@Test
	public void onlyTheEnteredNodeContributesToAnImplicitTransition() {
		ProcessDefinitionEditor editor = editor();
		editor.definition().getOnTransit().add(ProcessDefinitionEditor.transitionProcessor("processor.definition"));
		editor.onEnter(null, "processor.root");

		TransitionOracle transition = new ProcessOracle(editor.definition()).transitionOracle(null, null, true);

		assertThat(transition.getTransitionProcessors()).extracting(reference -> reference.getProcessorId()) //
				.containsExactly("processor.root");
	}

	@Test
	public void duplicateNodeStateIsRejected() {
		ProcessDefinitionEditor editor = editor();
		Node duplicate = Node.T.create();
		duplicate.setState("check");
		editor.acquireNode("check");
		editor.definition().getNodes().add(duplicate);

		assertThatThrownBy(() -> new ProcessOracle(editor.definition())) //
				.isInstanceOf(IllegalArgumentException.class) //
				.hasMessageContaining("check");
	}

	private static ProcessDefinitionEditor editor() {
		return ProcessDefinitionEditor.create("test-process", "Test Process");
	}
}
