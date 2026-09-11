package tribefire.extension.process.rx.test;

import static org.assertj.core.api.Assertions.assertThat;
import static tribefire.extension.process.rx.test.wire.space.ProcessRxTestModuleSpace.STATE_ESCALATED;
import static tribefire.extension.process.rx.test.wire.space.ProcessRxTestModuleSpace.STATE_WAIT;
import static tribefire.extension.process.rx.test.wire.space.ProcessRxTestModuleSpace.STATE_WAIT_AUTO;
import static tribefire.extension.process.rx.test.wire.space.ProcessRxTestModuleSpace.STATE_WAIT_DONE;
import static tribefire.extension.process.rx.test.wire.space.ProcessRxTestModuleSpace.STATE_WAIT_OVERDUE;

import org.junit.ClassRule;
import org.junit.Test;

import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

import hiconic.rx.test.common.RxPlatformTestClassRule;
import tribefire.extension.process.data.model.log.ProcessLogEvent;
import tribefire.extension.process.data.model.state.ProcessActivity;
import tribefire.extension.process.data.model.state.TransitionPhase;
import tribefire.extension.process.rx.test.model.WaitingTestProcess;

/**
 * A node with a decoupled interaction: the process stops, and something outside the engine continues it.
 * <p>
 * The overdue tests run the search themselves with {@code revive()}, instead of waiting for the worker to run it on its
 * interval. {@link #revivalWorkerFindsOverdueProcessesOnItsOwn()} is the one test that covers that worker, so that a
 * broken worker fails there and nowhere else.
 */
public class WaitingTest extends AbstractProcessEngineTest {

	@ClassRule
	public static final RxPlatformTestClassRule platformFixture = new RxPlatformTestClassRule("res/app", "WaitingTest");

	@Override
	protected RxPlatformTestClassRule platformClassRule() {
		return platformFixture;
	}

	@Test
	public void processWaitsInANodeWithADecoupledInteraction() {
		WaitingTestProcess process = startedProcess();

		WaitingTestProcess reloaded = reload(process);
		assertThat(reloaded.getState()).isEqualTo(STATE_WAIT);
		assertThat(reloaded.getTransitionPhase()).isEqualTo(TransitionPhase.DECOUPLED_INTERACTION);
		assertThat(logEvents(process)).contains(ProcessLogEvent.PROCESS_SUSPENDED);
	}

	@Test
	public void nodeWithoutAGracePeriodHasNoDeadline() {
		WaitingTestProcess process = startedProcess();

		assertThat(reload(process).getOverdueAt()).isNull();

		// the search finds nothing to do, and the process keeps waiting
		revive();
		assertThat(reload(process).getActivity()).isEqualTo(ProcessActivity.waiting);
	}

	@Test
	public void resumeContinuesByNormalRouting() {
		WaitingTestProcess process = startedProcess();

		resume(process);
		await(process, ProcessActivity.waiting);

		// wait-overdue waits as well, so the process is waiting again, one state further
		assertThat(reload(process).getState()).isEqualTo(STATE_WAIT_OVERDUE);
	}

	@Test
	public void resumeToStateTakesAnEdgeThatRoutingNeverTakes() {
		WaitingTestProcess process = startedProcess();

		resumeToState(process, STATE_WAIT_DONE);
		await(process, ProcessActivity.ended);

		assertThat(reload(process).getState()).isEqualTo(STATE_WAIT_DONE);
	}

	@Test
	public void processIsNotOverdueBeforeItsDeadline() {
		WaitingTestProcess process = waitingWithDeadline(STATE_WAIT_OVERDUE);

		revive();

		WaitingTestProcess reloaded = reload(process);
		assertThat(reloaded.getActivity()).isEqualTo(ProcessActivity.waiting);
		assertThat(reloaded.getState()).isEqualTo(STATE_WAIT_OVERDUE);
		assertThat(logEvents(process)).doesNotContain(ProcessLogEvent.PROCESS_IS_OVERDUE);
	}

	@Test
	public void waitingLongerThanTheDeadlineEscalatesOverTheOverdueNode() {
		WaitingTestProcess process = waitingWithDeadline(STATE_WAIT_OVERDUE);

		passDeadline(process);
		revive();
		await(process, ProcessActivity.ended);

		assertThat(reload(process).getState()).isEqualTo(STATE_ESCALATED);
		assertThat(logEvents(process)).contains(ProcessLogEvent.PROCESS_IS_OVERDUE, ProcessLogEvent.OVERDUE_TRANSITION);
	}

	@Test
	public void waitingWithoutAnOverdueNodeContinuesByNormalRouting() {
		WaitingTestProcess process = waitingWithDeadline(STATE_WAIT_AUTO);

		passDeadline(process);
		revive();
		await(process, ProcessActivity.ended);

		assertThat(reload(process).getState()).isEqualTo(STATE_WAIT_DONE);
		assertThat(logEvents(process)) //
				.contains(ProcessLogEvent.PROCESS_IS_OVERDUE) //
				.doesNotContain(ProcessLogEvent.OVERDUE_TRANSITION);
	}

	@Test
	public void revivalWorkerFindsOverdueProcessesOnItsOwn() {
		WaitingTestProcess process = waitingWithDeadline(STATE_WAIT_OVERDUE);

		// nobody calls revive() here: the worker of the process manager has to find the process by itself
		await(process, ProcessActivity.ended);

		assertThat(reload(process).getState()).isEqualTo(STATE_ESCALATED);
	}

	private WaitingTestProcess waitingWithDeadline(String state) {
		WaitingTestProcess process = startedProcess();

		resumeToState(process, state);
		await(process, ProcessActivity.waiting);
		assertThat(reload(process).getOverdueAt()).isNotNull();

		return process;
	}

	private WaitingTestProcess startedProcess() {
		PersistenceGmSession session = session();
		WaitingTestProcess process = session.create(WaitingTestProcess.T);
		session.commit();

		start(process);
		await(process, ProcessActivity.waiting);

		return process;
	}
}
