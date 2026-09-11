package tribefire.extension.process.rx.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.util.Date;
import java.util.List;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.time.TimeSpan;
import com.braintribe.model.time.TimeUnit;

import hiconic.rx.access.module.api.AccessContract;
import hiconic.rx.test.common.AbstractClassScopedRxTest;
import tribefire.extension.process.api.model.analysis.GetProcessLog;
import tribefire.extension.process.api.model.ctrl.RecoverProcess;
import tribefire.extension.process.api.model.ctrl.ResumeProcess;
import tribefire.extension.process.api.model.ctrl.ResumeProcessToState;
import tribefire.extension.process.api.model.ctrl.ReviveProcesses;
import tribefire.extension.process.api.model.ctrl.StartProcess;
import tribefire.extension.process.api.model.ctrl.WaitForProcess;
import tribefire.extension.process.api.model.data.ProcessLog;
import tribefire.extension.process.data.model.ProcessItem;
import tribefire.extension.process.data.model.log.ProcessLogEntry;
import tribefire.extension.process.data.model.log.ProcessLogEvent;
import tribefire.extension.process.data.model.state.ProcessActivity;
import tribefire.extension.process.rx.test.wire.space.ProcessRxTestModuleSpace;

/**
 * What every engine test needs: a session on the test access, and the process API requests wrapped so that a test reads as
 * a sequence of steps.
 * <p>
 * {@link #await(ProcessItem, ProcessActivity...)} is how a test waits. It uses {@code WaitForProcess}, which returns as
 * soon as the process reached one of the given activities, because the engine advances a process asynchronously. When it
 * times out, the failure message carries the state the process is actually in, and its log, so that a red test says what
 * went wrong rather than only that nothing happened.
 */
public abstract class AbstractProcessEngineTest extends AbstractClassScopedRxTest {

	private static final TimeSpan MAX_WAIT = TimeSpan.create(20, TimeUnit.second);
	private static final long MAX_DEADLINE_WAIT_MS = 5000;

	protected PersistenceGmSession session() {
		return resolveExportContract(AccessContract.class).systemSessionFactory().newSession(ProcessRxTestModuleSpace.ACCESS_ID);
	}

	/** Starts the process and asserts that the engine accepted it. */
	protected void start(ProcessItem process) {
		StartProcess request = StartProcess.T.create();
		request.item(process);

		Maybe<?> maybe = request.eval(session()).getReasoned();
		assertThat(maybe.isSatisfied()) //
				.withFailMessage(() -> "StartProcess was rejected: " + maybe.whyUnsatisfied().stringify()) //
				.isTrue();
	}

	protected void resume(ProcessItem process) {
		ResumeProcess request = ResumeProcess.T.create();
		request.item(process);
		request.eval(session()).get();
	}

	protected void resumeToState(ProcessItem process, String state) {
		ResumeProcessToState request = ResumeProcessToState.T.create();
		request.item(process);
		request.setToState(state);
		request.eval(session()).get();
	}

	protected void recover(ProcessItem process) {
		RecoverProcess request = RecoverProcess.T.create();
		request.item(process);
		request.eval(session()).get();
	}

	/**
	 * Runs the search for unattended and overdue processes once, which is what the revival worker does on its interval. A
	 * test calls it directly, so that it does not depend on the timing of that worker.
	 */
	protected void revive() {
		ReviveProcesses.T.create().eval(session()).get();
	}

	/** Waits until the process reached one of the given activities, and returns the one it reached. */
	protected ProcessActivity await(ProcessItem process, ProcessActivity... activities) {
		WaitForProcess request = WaitForProcess.create(process, activities);
		request.setMaxWait(MAX_WAIT);

		Maybe<ProcessActivity> maybe = request.eval(session()).getReasoned();

		if (maybe.isUnsatisfied())
			fail("process did not reach any of " + List.of(activities) + ": " + maybe.whyUnsatisfied().stringify() //
					+ System.lineSeparator() + describe(process));

		return maybe.get();
	}

	/**
	 * Waits until the deadline of a waiting process has passed. A deadline is a point in time, so a test that asserts what
	 * happens after it has to let it pass; nothing else in these tests waits on the clock.
	 */
	protected void passDeadline(ProcessItem process) {
		Date overdueAt = reload(process).getOverdueAt();
		assertThat(overdueAt).withFailMessage(() -> "process has no deadline: " + describe(process)).isNotNull();

		long remaining = overdueAt.getTime() - System.currentTimeMillis() + 50;
		if (remaining <= 0)
			return;

		assertThat(remaining).withFailMessage("deadline of the test graph is too far away: %d ms", remaining).isLessThan(MAX_DEADLINE_WAIT_MS);

		try {
			Thread.sleep(remaining);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("interrupted while waiting for the deadline of " + process.getId(), e);
		}
	}

	/** The process as it is stored now, read in a fresh session, because the engine works in its own. */
	protected <P extends ProcessItem> P reload(P process) {
		return session().query().entity(process).require();
	}

	protected List<ProcessLogEvent> logEvents(ProcessItem process) {
		GetProcessLog request = GetProcessLog.T.create();
		request.item(process);

		ProcessLog log = request.eval(session()).get();
		return log.getEntries().stream().map(ProcessLogEntry::getEvent).toList();
	}

	/** State, activity, phase, deadline and log of a process, for a failure message. */
	protected String describe(ProcessItem process) {
		ProcessItem stored = reload(process);

		return "process " + stored.entityType().getShortName() + "[" + stored.getId() + "]" //
				+ " state=" + stored.getState() //
				+ " previousState=" + stored.getPreviousState() //
				+ " nextState=" + stored.getNextState() //
				+ " activity=" + stored.getActivity() //
				+ " phase=" + stored.getTransitionPhase() //
				+ " lastTransit=" + stored.getLastTransit() //
				+ " overdueAt=" + stored.getOverdueAt() //
				+ System.lineSeparator() + "log: " + logEvents(process);
	}
}
