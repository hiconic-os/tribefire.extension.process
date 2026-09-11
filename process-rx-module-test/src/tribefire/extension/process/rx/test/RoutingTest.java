package tribefire.extension.process.rx.test;

import static org.assertj.core.api.Assertions.assertThat;
import static tribefire.extension.process.rx.test.wire.space.ProcessRxTestModuleSpace.STATE_APPROVED;
import static tribefire.extension.process.rx.test.wire.space.ProcessRxTestModuleSpace.STATE_MANUAL;
import static tribefire.extension.process.rx.test.wire.space.ProcessRxTestModuleSpace.STATE_REJECTED;
import static tribefire.extension.process.rx.test.wire.space.ProcessRxTestModuleSpace.STATE_REVIEW;

import org.junit.ClassRule;
import org.junit.Test;

import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

import hiconic.rx.test.common.RxPlatformTestClassRule;
import tribefire.extension.process.data.model.log.ProcessLogEvent;
import tribefire.extension.process.data.model.state.ProcessActivity;
import tribefire.extension.process.rx.test.model.RoutingTestProcess;

/** How the engine chooses the next state, and what runs on the way. */
public class RoutingTest extends AbstractProcessEngineTest {

	@ClassRule
	public static final RxPlatformTestClassRule platformFixture = new RxPlatformTestClassRule("res/app", "RoutingTest");

	@Override
	protected RxPlatformTestClassRule platformClassRule() {
		return platformFixture;
	}

	@Test
	public void matchingConditionWins() {
		RoutingTestProcess process = process(true, null);

		start(process);
		assertThat(await(process, ProcessActivity.ended)).isEqualTo(ProcessActivity.ended);

		RoutingTestProcess reloaded = reload(process);
		assertThat(reloaded.getState()).isEqualTo(STATE_APPROVED);
		assertThat(reloaded.getEndedAt()).isNotNull();
	}

	@Test
	public void edgeWithoutConditionIsTheDefault() {
		RoutingTestProcess process = process(false, null);

		start(process);
		await(process, ProcessActivity.ended);

		assertThat(reload(process).getState()).isEqualTo(STATE_REJECTED);
	}

	@Test
	public void processorsOfOneTransitionRunInTheDocumentedOrder() {
		RoutingTestProcess process = process(true, null);

		start(process);
		await(process, ProcessActivity.ended);

		// Two transitions happen: into review, then into approved. The processor list of the definition runs on both, which
		// is why "definition-transit" appears twice. The order within the second transition is definition, onLeft, edge,
		// onEntered.
		assertThat(reload(process).getTrace()).isEqualTo("definition-transit,demand,definition-transit,left,edge-transit,entered");
	}

	@Test
	public void transitionProcessorTakesAnEdgeThatRoutingNeverTakes() {
		RoutingTestProcess process = process(false, STATE_MANUAL);

		start(process);
		await(process, ProcessActivity.ended);

		assertThat(reload(process).getState()).isEqualTo(STATE_MANUAL);
	}

	@Test
	public void demandedStateWithoutAnEdgeHaltsTheProcess() {
		RoutingTestProcess process = process(false, "nowhere");

		start(process);
		assertThat(await(process, ProcessActivity.halted)).isEqualTo(ProcessActivity.halted);

		assertThat(reload(process).getState()).isEqualTo(STATE_REVIEW);
		assertThat(logEvents(process)).contains(ProcessLogEvent.INVALID_TRANSITION, ProcessLogEvent.PROCESS_HALTED);
	}

	@Test
	public void everyStepIsLogged() {
		RoutingTestProcess process = process(true, null);

		start(process);
		await(process, ProcessActivity.ended);

		assertThat(logEvents(process)) //
				.startsWith(ProcessLogEvent.PROCESS_STARTED) //
				.contains(ProcessLogEvent.STATE_CHANGED, ProcessLogEvent.CONDITION_MATCHED, ProcessLogEvent.PROCESSOR_EXECUTED) //
				.endsWith(ProcessLogEvent.PROCESS_ENDED);
	}

	private RoutingTestProcess process(boolean approved, String demandedState) {
		PersistenceGmSession session = session();

		RoutingTestProcess process = session.create(RoutingTestProcess.T);
		process.setApproved(approved);
		process.setDemandedState(demandedState);
		session.commit();

		return process;
	}
}
