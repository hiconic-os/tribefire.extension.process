package tribefire.extension.process.bmpn2pd;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.BeforeClass;
import org.junit.Test;

import tribefire.extension.process.model.deployment.ConditionalEdge;
import tribefire.extension.process.model.deployment.Edge;
import tribefire.extension.process.model.deployment.Node;
import tribefire.extension.process.model.deployment.ProcessDefinition;
import tribefire.extension.process.model.deployment.ProcessElement;
import tribefire.extension.process.model.deployment.StandardNode;

/**
 * What {@link Bpmn2Pd} makes of a BPMN diagram. The translation needs nothing but the file, so these are plain unit tests.
 * <p>
 * They are written down as the specification of the cortex behaviour, because the RX translator has to answer for the same diagram in the same way.
 * {@code Bpmn2ProcessDefinitionTest} of {@code process-rx-bpmn2pd-test} asserts the same facts against the RX process configuration model.
 */
public class Bpmn2PdTest {

	/** How the node without a state is written in the rendered graph. That node is the one a process starts in. */
	private static final String ROOT = "root";

	/**
	 * The graph of {@code res/lab.bpmn}: every node, which of them wait, and the states a node may change to, in the order in which the engine
	 * considers them.
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
	private static final Comparator<StandardNode> BY_STATE = Comparator //
			.comparing((StandardNode node) -> node.getState() != null) //
			.thenComparing((StandardNode node) -> state(node));

	private static ProcessDefinition definition;

	@BeforeClass
	public static void translate() throws IOException {
		try (InputStream in = new FileInputStream(new File("res/lab.bpmn"))) {
			definition = Bpmn2Pd.translate(in);
		}
	}

	/** One node per box of the diagram, named after the label of the box. */
	@Test
	public void nodesAreTheBoxesOfTheDiagram() {
		assertThat(nodes().map(Bpmn2PdTest::state)) //
				.containsExactly(ROOT, "AUTH", "COMMUNICATE", "FOLLOW_UP", "REVIEW", "SINK_CANCELLED", "SINK_MAX_FOLLOW_UPS", "SINK_RECEIVED",
						"SINK_UNDECIDED", "WAIT");
	}

	/** The box without a label gets no state, and the node without a state is where a process starts. */
	@Test
	public void boxWithoutALabelBecomesTheRoot() {
		List<StandardNode> withoutState = nodes().filter(node -> node.getState() == null).collect(Collectors.toList());

		assertThat(withoutState).hasSize(1);
		assertThat(edgesOf(withoutState.get(0))).isNotEmpty();
	}

	/**
	 * Only a person box and a system box become a node that waits. An automatic box and a plain box do not, and neither does the start box.
	 */
	@Test
	public void onlyPersonAndSystemBoxesWait() {
		assertThat(nodes().filter(node -> node.getDecoupledInteraction() != null).map(Bpmn2PdTest::state)) //
				.containsExactly("FOLLOW_UP", "REVIEW", "WAIT");
	}

	/** A person box waits for a person, which the engine tells apart from a system box that waits for a system. */
	@Test
	public void personBoxWaitsForAUser() {
		assertThat(node("REVIEW").getDecoupledInteraction().getUserInteraction()).isTrue();
		assertThat(node("FOLLOW_UP").getDecoupledInteraction().getUserInteraction()).isTrue();
		assertThat(node("WAIT").getDecoupledInteraction().getUserInteraction()).isFalse();
	}

	/**
	 * The arrows of a gateway keep their order: the labelled ones first, sorted by label, and the unlabelled one last. A chain of gateways behind one
	 * box is flattened, gateway after gateway.
	 */
	@Test
	public void graphIsTranslatedAsDrawn() {
		assertThat(renderGraph()).isEqualTo(EXPECTED_GRAPH);
	}

	/**
	 * The translator reads a diagram, and a diagram says nothing about the code that answers a question. The label of an arrow is a question for the
	 * reader, so every edge leaves its condition open, and the application fills it in.
	 */
	@Test
	public void noEdgeCarriesACondition() {
		List<ConditionalEdge> conditionalEdges = elements(ConditionalEdge.class).collect(Collectors.toList());

		assertThat(conditionalEdges).isNotEmpty();
		assertThat(conditionalEdges).allMatch(edge -> edge.getCondition() == null);
	}

	private static String renderGraph() {
		StringBuilder text = new StringBuilder();

		nodes().forEach(node -> {
			text.append(state(node));
			if (node.getDecoupledInteraction() != null)
				text.append(" (waits)");
			text.append('\n');

			edgesOf(node).forEach(edge -> text.append("  -> ").append(state(edge.getTo())).append('\n'));
		});

		return text.toString();
	}

	/**
	 * The state changes of a node, in the order in which the engine considers them: the conditional edges as the node lists them, and then the
	 * unconditional edge, which the engine takes when no condition matched.
	 */
	private static List<Edge> edgesOf(StandardNode node) {
		List<Edge> edges = new ArrayList<>(node.getConditionalEdges());

		elements(Edge.class) //
				.filter(edge -> !(edge instanceof ConditionalEdge)) //
				.filter(edge -> edge.getFrom() == node) //
				.sorted(Comparator.comparing((Edge edge) -> state(edge.getTo()))) //
				.forEach(edges::add);

		return edges;
	}

	private static StandardNode node(String state) {
		return nodes().filter(node -> state.equals(state(node))).findFirst() //
				.orElseThrow(() -> new IllegalArgumentException("No such node: " + state));
	}

	private static Stream<StandardNode> nodes() {
		return elements(StandardNode.class).sorted(BY_STATE);
	}

	private static <E extends ProcessElement> Stream<E> elements(Class<E> type) {
		return definition.getElements().stream().filter(type::isInstance).map(type::cast);
	}

	private static String state(Node node) {
		return node.getState() == null ? ROOT : node.getState().toString();
	}
}
