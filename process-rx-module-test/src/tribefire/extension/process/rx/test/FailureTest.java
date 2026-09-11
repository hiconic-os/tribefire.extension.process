package tribefire.extension.process.rx.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static tribefire.extension.process.rx.test.wire.space.ProcessRxTestModuleSpace.STATE_WORK;
import static tribefire.extension.process.rx.test.wire.space.ProcessRxTestModuleSpace.STATE_WORK_DONE;

import java.util.function.Consumer;

import org.junit.ClassRule;
import org.junit.Test;

import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

import hiconic.rx.test.common.RxPlatformTestClassRule;
import tribefire.extension.process.data.model.log.ProcessLogEvent;
import tribefire.extension.process.data.model.state.ProcessActivity;
import tribefire.extension.process.rx.test.model.FailingTestProcess;

/** A failing transition processor halts the process, and an operator continues it once the cause is gone. */
public class FailureTest extends AbstractProcessEngineTest {

	@ClassRule
	public static final RxPlatformTestClassRule platformFixture = new RxPlatformTestClassRule("res/app", "FailureTest");

	@Override
	protected RxPlatformTestClassRule platformClassRule() {
		return platformFixture;
	}

	@Test
	public void processorThatThrowsHaltsTheProcess() {
		FailingTestProcess process = process(p -> p.setFailByException(true));

		start(process);
		assertThat(await(process, ProcessActivity.halted)).isEqualTo(ProcessActivity.halted);

		assertThat(reload(process).getState()).isEqualTo(STATE_WORK);
		assertThat(logEvents(process)).contains(ProcessLogEvent.ERROR_IN_PROCESSOR, ProcessLogEvent.PROCESS_HALTED);
	}

	@Test
	public void processorThatSetsAnErrorHaltsTheProcess() {
		FailingTestProcess process = process(p -> p.setFailByReason(true));

		start(process);
		await(process, ProcessActivity.halted);

		assertThat(logEvents(process)).contains(ProcessLogEvent.ERROR_IN_PROCESSOR, ProcessLogEvent.PROCESS_HALTED);
	}

	@Test
	public void errorHandlersOfTheDefinitionRun() {
		FailingTestProcess process = process(p -> p.setFailByReason(true));

		start(process);
		await(process, ProcessActivity.halted);

		assertThat(reload(process).getTrace()).isEqualTo("error");
	}

	@Test
	public void recoverContinuesTheProcessOnceTheCauseIsGone() {
		FailingTestProcess process = process(p -> p.setFailByException(true));

		start(process);
		await(process, ProcessActivity.halted);

		PersistenceGmSession session = session();
		FailingTestProcess halted = session.query().entity(process).require();
		halted.setFailByException(false);
		session.commit();

		recover(halted);
		await(process, ProcessActivity.ended);

		FailingTestProcess reloaded = reload(process);
		assertThat(reloaded.getState()).isEqualTo(STATE_WORK_DONE);
		assertThat(reloaded.getTrace()).contains("worked");
		assertThat(logEvents(process)).contains(ProcessLogEvent.PROCESS_RECOVERED, ProcessLogEvent.PROCESS_ENDED);
	}

	@Test
	public void endedProcessCannotBeRecovered() {
		FailingTestProcess process = process(p -> {
			// asked to fail in no way, so it runs through
		});

		start(process);
		await(process, ProcessActivity.ended);

		assertThatThrownBy(() -> recover(process)).isInstanceOf(RuntimeException.class);
	}

	private FailingTestProcess process(Consumer<FailingTestProcess> configurer) {
		PersistenceGmSession session = session();

		FailingTestProcess process = session.create(FailingTestProcess.T);
		configurer.accept(process);
		session.commit();

		return process;
	}
}
