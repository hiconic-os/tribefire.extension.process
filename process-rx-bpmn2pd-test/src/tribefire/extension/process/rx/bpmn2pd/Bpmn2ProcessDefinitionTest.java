package tribefire.extension.process.rx.bpmn2pd;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.BeforeClass;
import org.junit.Test;

import tribefire.extension.process.model.configuration.Edge;
import tribefire.extension.process.model.configuration.Node;
import tribefire.extension.process.model.configuration.ProcessDefinition;

/**
 * What {@link Bpmn2ProcessDefinition} makes of a BPMN diagram. The translation needs nothing but the file, so these are
 * plain unit tests.
 * <p>
 * The diagram and the assertions are the ones of {@code Bpmn2PdTest} in {@code process-bpmn2pd-test}, which writes down
 * what the cortex translator does. The two translators must answer for the same diagram in the same way, so a failure
 * here is a difference to cortex.
 * <p>
 * Two facts of the cortex test have no counterpart here:
 * <ul>
 * <li>cortex marks a node that waits for a person apart from one that waits for a system, with
 * {@code DecoupledInteraction.userInteraction}. The RX model has no such property, and the cortex engine does not read
 * it either - only a UI does.
 * <li>cortex has two kinds of edge, one with a condition and one without. RX has one kind with an optional condition,
 * and the edge without a condition is the one the engine takes when no condition matched. Both say the same thing, so
 * the rendered graph below is the same.
 * </ul>
 */
public class Bpmn2ProcessDefinitionTest {

	private static final String DEFINITION_ID = "translator.lab";

	/** How the node without a state is written in the rendered graph. That node is the one a process starts in. */
	private static final String ROOT = "root";

	/**
	 * The graph of {@code res/lab.bpmn}: every node, which of them wait, and the states a node may change to, in the
	 * order in which the engine considers them. Character for character the text that the cortex test expects.
	 */
	private static final String EXPECTED_GRAPH = """
			root
			  -> AUTH
			  -> REVIEW
			AUTH
			  -> COMMUNICATE
			COMMUNICATE
			  -> SINK_RECEIVED
			  -> SINK_MAX_FOLLOW_UPS
			  -> FOLLOW_UP
			  -> WAIT
			FOLLOW_UP (waits)
			  -> COMMUNICATE
			REVIEW (waits)
			  -> AUTH
			  -> SINK_CANCELLED
			  -> SINK_UNDECIDED
			SINK_CANCELLED
			SINK_MAX_FOLLOW_UPS
			SINK_RECEIVED
			SINK_UNDECIDED
			WAIT (waits)
			  -> COMMUNICATE
			""";

	/** The root first, the rest by state, so that the rendering is stable. */
	private static final Comparator<Node> BY_STATE = Comparator //
			.comparing((Node node) -> node.getState() != null) //
			.thenComparing((Node node) -> state(node));

	private static ProcessDefinition definition;

	@BeforeClass
	public static void translate() throws IOException {
		try (InputStream in = new FileInputStream(new File("res/lab.bpmn"))) {
			definition = Bpmn2ProcessDefinition.translate(DEFINITION_ID, in);
		}
	}

	/** The id is the one the caller chose, because a diagram carries no id of ours. The name comes from the file. */
	@Test
	public void definitionIsIdentifiedByTheCaller() {
		assertThat(definition.getProcessDefinitionId()).isEqualTo(DEFINITION_ID);
		assertThat(definition.getName()).isEqualTo("Translator Lab");
	}

	/** One node per box of the diagram, named after the label of the box. */
	@Test
	public void nodesAreTheBoxesOfTheDiagram() {
		assertThat(nodes().map(Bpmn2ProcessDefinitionTest::state)) //
				.containsExactly(ROOT, "AUTH", "COMMUNICATE", "FOLLOW_UP", "REVIEW", "SINK_CANCELLED", "SINK_MAX_FOLLOW_UPS",
						"SINK_RECEIVED", "SINK_UNDECIDED", "WAIT");
	}

	/** The box without a label gets no state, and the node without a state is where a process starts. */
	@Test
	public void boxWithoutALabelBecomesTheRoot() {
		List<Node> withoutState = nodes().filter(node -> node.getState() == null).collect(Collectors.toList());

		assertThat(withoutState).hasSize(1);
		assertThat(withoutState.get(0).getEdges()).isNotEmpty();
	}

	/**
	 * Only a person box and a system box become a node that waits. An automatic box and a plain box do not, and neither
	 * does the start box.
	 */
	@Test
	public void onlyPersonAndSystemBoxesWait() {
		assertThat(nodes().filter(node -> node.getDecoupledInteraction() != null).map(Bpmn2ProcessDefinitionTest::state)) //
				.containsExactly("FOLLOW_UP", "REVIEW", "WAIT");
	}

	/**
	 * The arrows of a gateway keep their order: the labelled ones first, sorted by label, and the unlabelled one last. A
	 * chain of gateways behind one box is flattened, gateway after gateway.
	 */
	@Test
	public void graphIsTranslatedAsDrawn() {
		assertThat(renderGraph()).isEqualTo(EXPECTED_GRAPH);
	}

	/**
	 * The translator reads a diagram, and a diagram says nothing about the code that answers a question. The label of an
	 * arrow is a question for the reader, so every edge leaves its condition open, and the application fills it in.
	 */
	@Test
	public void noEdgeCarriesACondition() {
		List<Edge> edges = edges();

		assertThat(edges).isNotEmpty();
		assertThat(edges).allMatch(edge -> edge.getCondition() == null);
	}

	/**
	 * RX identifies an edge by its name, so every edge needs one. The label of the arrow is the name, and an arrow
	 * without a label is named after the two states it connects.
	 */
	@Test
	public void edgesAreNamedAfterTheArrow() {
		assertThat(node(ROOT).getEdges()).extracting(Edge::getName) //
				.containsExactly("Is authority?", "root -> REVIEW");

		assertThat(node("AUTH").getEdges()).extracting(Edge::getName) //
				.containsExactly("AUTH -> COMMUNICATE");

		assertThat(node("COMMUNICATE").getEdges()).extracting(Edge::getName) //
				.containsExactly("Info received?", "Max follow ups reached?", "Follow up date reached?", "COMMUNICATE -> WAIT");
	}

	private static String renderGraph() {
		StringBuilder text = new StringBuilder();

		nodes().forEach(node -> {
			text.append(state(node));
			if (node.getDecoupledInteraction() != null)
				text.append(" (waits)");
			text.append('\n');

			node.getEdges().forEach(edge -> text.append("  -> ").append(state(edge.getTo())).append('\n'));
		});

		return text.toString();
	}

	private static Node node(String state) {
		return nodes().filter(node -> state.equals(state(node))).findFirst() //
				.orElseThrow(() -> new IllegalArgumentException("No such node: " + state));
	}

	private static Stream<Node> nodes() {
		return definition.getNodes().stream().sorted(BY_STATE);
	}

	private static List<Edge> edges() {
		return definition.getNodes().stream().flatMap(node -> node.getEdges().stream()).collect(Collectors.toList());
	}

	private static String state(Node node) {
		return node.getState() == null ? ROOT : node.getState();
	}
}
