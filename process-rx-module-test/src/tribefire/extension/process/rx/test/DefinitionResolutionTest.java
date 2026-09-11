package tribefire.extension.process.rx.test;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.ClassRule;
import org.junit.Test;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

import hiconic.rx.test.common.RxPlatformTestClassRule;
import tribefire.extension.process.api.model.ctrl.ReviveProcesses;
import tribefire.extension.process.api.model.ctrl.StartProcess;
import tribefire.extension.process.reason.model.ProcessDefinitionNotFound;
import tribefire.extension.process.rx.test.model.UnmappedTestProcess;

/** What the engine answers when a process type names no definition, and that the configured graphs are all there. */
public class DefinitionResolutionTest extends AbstractProcessEngineTest {

	@ClassRule
	public static final RxPlatformTestClassRule platformFixture = new RxPlatformTestClassRule("res/app", "DefinitionResolutionTest");

	@Override
	protected RxPlatformTestClassRule platformClassRule() {
		return platformFixture;
	}

	@Test
	public void processTypeWithoutMetadataIsRejected() {
		PersistenceGmSession session = session();
		UnmappedTestProcess process = session.create(UnmappedTestProcess.T);
		session.commit();

		StartProcess request = StartProcess.T.create();
		request.item(process);

		Maybe<?> maybe = request.eval(session).getReasoned();

		assertThat(maybe.isUnsatisfiedBy(ProcessDefinitionNotFound.T)).isTrue();
		assertThat(maybe.whyUnsatisfied().stringify()).contains(UnmappedTestProcess.T.getTypeSignature());
	}

	@Test
	public void processManagerRequestsReachTheTestAccess() {
		Maybe<?> maybe = ReviveProcesses.T.create().eval(session()).getReasoned();

		assertThat(maybe.isSatisfied()) //
				.withFailMessage(() -> "ReviveProcesses was rejected: " + maybe.whyUnsatisfied().stringify()) //
				.isTrue();
	}
}
