// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
// 
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package tribefire.extension.process.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.logging.Logger;
import com.braintribe.model.accessdeployment.aspect.AspectConfiguration;
import com.braintribe.model.accessdeployment.smood.CollaborativeSmoodAccess;
import com.braintribe.model.goofy.GoofyProcess;
import com.braintribe.model.goofydeployment.GoofyOutputer;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.source.FileUploadSource;
import com.braintribe.product.rat.imp.ImpApi;
import com.braintribe.product.rat.imp.impl.utils.GeneralGmUtils;
import com.braintribe.product.rat.imp.impl.utils.QueryHelper;
import com.braintribe.testing.category.KnownIssue;
import com.braintribe.testing.category.Slow;
import com.braintribe.testing.category.SpecialResources;
import com.braintribe.testing.category.VerySlow;
import com.braintribe.testing.internal.tribefire.tests.AbstractTribefireQaTest;

import tribefire.extension.process.model.data.Process;
import tribefire.extension.process.model.data.ProcessActivity;
import tribefire.extension.process.model.data.tracing.ProcessTrace;

@Category(KnownIssue.class)
//@Category(SpecialResources.class) // Test needs to access resource directory. TODO: Find a way to make this work in JUnitTestRunner
public class ProcessingEngineExampleCartridgeExecutingTest extends AbstractTribefireQaTest {

	@SuppressWarnings("hiding") // We need this logger for use in static methods
	private static Logger logger = Logger.getLogger(ProcessingEngineExampleCartridgeExecutingTest.class);

	////////// TEST SETTINGS ////////////

	private static final String GOOFY_ASPECT_CONFIGURATION_ID = "custom:aspectConfiguration/goofy";
	private static final String GOOFY_OUTPUTER_ID = "goofyOutputer";

	private static final String GOOFY_PROCESS_MODEL_NAME = "tribefire.cortex.testing:goofy-model";

	private static final String propertiesDirectoryPath = testDir(ProcessingEngineExampleCartridgeExecutingTest.class).getAbsolutePath();

	private static final int WAIT_FOR_PROCESS_CREATION_IN_SECONDS = 600;
	private static final int WAIT_FOR_PROCESS_FINISH_IN_SECONDS = 600;

	//////// PROCESS DEFINITION //////////

	private static final int NUM_RESTARTS = 3;

	private static final String ROOT = null;
	private static final String DECODE = "decode";
	private static final String VALIDATE = "validate";
	private static final String CLEARANCE = "clearance";
	private static final String VAL_ERROR = "valError";
	private static final String HASH = "hash";
	private static final String OUTPUT = "output";
	private static final String FINALIZE = "finalize";
	private static final String OVERDUE = "overdue";

	private static final String EDGE_TRANSITION = "edge-transition";
	private static final String PROCESS_RESUMED = "process-resumed";
	private static final String PROCESS_SUSPENDED = "process-suspended";
	private static final String PRECALL_TRANSITION = "precall-transition-processor";
	private static final String POSTCALL_TRANSITION = "postcall-transition-processor";
	private static final String CONTINUATION_DEMAND = "continuation-demand";
	private static final String ERROR_IN_TRANSITION = "error-in-transition-processor";
	private static final String MISSING_ERROR_NODE = "missing-error-node";
	private static final String PRECALL_CONDITION = "precall-condition-processor";
	private static final String POSTCALL_CONDITION = "postcall-condition-processor";
	private static final String DEFAULT_CONDITION = "default-condition-matched";
	private static final String CONDITION_MATCHED = "condition-matched";
	private static final String PROCESS_ENDED = "process-ended";
	private static final String RESTART = "restart";
	private static final String RESTART_TRANSITION = "restart-transition";
	private static final String MAX_RESTART_REACHED = "max-restart-reached";

	///////////////// LOCAL FIELDS ////////////////////////

	private String GOOFY_ACCESS_ID;
	private ImpApi imp;
	
	private PersistenceGmSession goofyAccessSession;

	@BeforeClass
	public static void synchCartridge() {
		ImpApi imp = apiFactory().build();
		
		logger.info("setup outputer");

		GoofyOutputer outputer = imp.deployable(GoofyOutputer.T, GOOFY_OUTPUTER_ID).get();
		outputer.setOutputFileDirectory(null);
		imp.commit();

		imp.deployable().with(GOOFY_OUTPUTER_ID).redeploy();
	}

	@Before
	public void prepareTest() {
		logger.info("Preparing test. Erasing previous test entities");

		eraseTestEntities();
		GOOFY_ACCESS_ID = nameWithTimestamp("goofy.smood");

		logger.info("Preparing test. Building imp");
		// imp with new cortex session
		imp = apiFactory().build();

		logger.info("Creating process access");
		GmMetaModel goofyModel = imp.model().with(GOOFY_PROCESS_MODEL_NAME).get();
		AspectConfiguration aspectConfig = new QueryHelper(imp.session()).findById(AspectConfiguration.T, GOOFY_ASPECT_CONFIGURATION_ID);
		CollaborativeSmoodAccess newGoofyAccess = imp.deployable().access().createCsa(GOOFY_ACCESS_ID, GOOFY_ACCESS_ID, goofyModel).get();
		newGoofyAccess.setAspectConfiguration(aspectConfig);
		
		imp.commit();
		imp.deployable(newGoofyAccess).deploy();

		goofyAccessSession = apiFactory().newSessionForAccess(newGoofyAccess.getExternalId());

	}

	public void executeTestForGoofy1(GoofyProcess process) {
		assertThat(process.getState()).isEqualTo(OVERDUE);
		assertThat(process.getHash()).isNotNull();

		assertThat(process.getRestartCounters()).hasSize(1);
		assertThat(process.getRestartCounters().stream().findFirst().get().getCount()).isEqualTo(3);

		TraceAsserter traceAsserter = TraceAsserter.forProcess(process);

		logger.info("Asserting traces for goofy1");

		for (int i = 0; i <= NUM_RESTARTS; i++) {
			logger.info("round " + i + "/" + NUM_RESTARTS);
			// root -> decode
			traceAsserter
					.assertNextProcessTraces(ROOT, DECODE, EDGE_TRANSITION, PROCESS_RESUMED, PRECALL_TRANSITION, POSTCALL_TRANSITION,
							CONTINUATION_DEMAND)
					// decode -> validate
					.assertNextProcessTraces(DECODE, VALIDATE, EDGE_TRANSITION, PRECALL_TRANSITION, POSTCALL_TRANSITION, PRECALL_CONDITION,
							POSTCALL_CONDITION, DEFAULT_CONDITION)
					// validate -> hash
					.assertNextProcessTraces(VALIDATE, HASH, EDGE_TRANSITION, PRECALL_TRANSITION, POSTCALL_TRANSITION, CONTINUATION_DEMAND)
					// hash -> output
					.assertNextProcessTraces(HASH, OUTPUT, EDGE_TRANSITION, PRECALL_TRANSITION, POSTCALL_TRANSITION, CONTINUATION_DEMAND)
					// output -> finalize
					.assertNextProcessTraces(OUTPUT, FINALIZE, EDGE_TRANSITION, PRECALL_TRANSITION, ERROR_IN_TRANSITION, PROCESS_ENDED);

			// the restarting part is of course omitted the last time
			if (i < NUM_RESTARTS) {
				traceAsserter.assertNextProcessTraces(FINALIZE, OVERDUE, EDGE_TRANSITION, RESTART).assertNextProcessTrace(OVERDUE, ROOT,
						RESTART_TRANSITION);
			}
		}

		traceAsserter.assertNextProcessTraces(FINALIZE, OVERDUE, EDGE_TRANSITION, MAX_RESTART_REACHED, MISSING_ERROR_NODE);

		logger.info("Assert that there are no further traces");

		traceAsserter.assertNoFurtherTraces();

		logger.info("Finished checking traces");
	}

	public void executeTestForGoofy2(GoofyProcess process) {
		TraceAsserter initialTraceAsserter = TraceAsserter.forProcess(process);

		logger.info("Asserting traces for goofy2");

		// root -> decode
		initialTraceAsserter
				.assertNextProcessTraces(ROOT, DECODE, EDGE_TRANSITION, PROCESS_RESUMED, PRECALL_TRANSITION, POSTCALL_TRANSITION, CONTINUATION_DEMAND)
				// decode -> validate
				.assertNextProcessTraces(DECODE, VALIDATE, EDGE_TRANSITION, PRECALL_TRANSITION, POSTCALL_TRANSITION, PRECALL_CONDITION,
						POSTCALL_CONDITION, CONDITION_MATCHED)
				// validate -> clearance
				.assertNextProcessTrace(VALIDATE, CLEARANCE, EDGE_TRANSITION).assertNextProcessTrace(null, null, PROCESS_SUSPENDED)
				.assertNoFurtherTraces();

		logger.info("So far so good. Give clearance");
		executeClearance();

		imp.utils();
		GeneralGmUtils.refreshEntity(process);

		logger.info("Asserting traces again");

		// the traces are checked again from the beginning -> so we repeat the previous assertion before we continue
		TraceAsserter finalTraceAsserter = TraceAsserter.forProcess(process)
				.assertNextProcessTraces(ROOT, DECODE, EDGE_TRANSITION, PROCESS_RESUMED, PRECALL_TRANSITION, POSTCALL_TRANSITION, CONTINUATION_DEMAND)
				// decode -> validate
				.assertNextProcessTraces(DECODE, VALIDATE, EDGE_TRANSITION, PRECALL_TRANSITION, POSTCALL_TRANSITION, PRECALL_CONDITION,
						POSTCALL_CONDITION, CONDITION_MATCHED)
				// validate -> clearance
				.assertNextProcessTrace(VALIDATE, CLEARANCE, EDGE_TRANSITION).assertNextProcessTrace(null, null, PROCESS_SUSPENDED);

		finalTraceAsserter.assertNextProcessTraces(null, null, PROCESS_RESUMED, PRECALL_CONDITION, POSTCALL_CONDITION, DEFAULT_CONDITION)
				.assertNextProcessTrace(CLEARANCE, VAL_ERROR, EDGE_TRANSITION).assertNextProcessTrace(CLEARANCE, VAL_ERROR, PROCESS_ENDED)
				.assertNoFurtherTraces();

		logger.info("traces asserted successfully");
	}

	@Test
	public void testGoofy1() {
		testWithGoofy1PropertiesFile("goofy-1.properties", 1);
	}

	@Test
	public void testGoofy2() {
		testWithGoofy2PropertiesFile("goofy-2.properties", 1);
	}

	@Test
	@Category(VerySlow.class)
	public void testGoofy1Bulk100() {
		testWithGoofy1PropertiesFile("goofy-1.bulk.100.properties", 100);
	}

	@Test
	public void testGoofy1Bulk1() {
		testWithGoofy1PropertiesFile("goofy-1.bulk.1.properties", 6);
	}

	@Test
	public void testGoofy1Bulk2() {
		testWithGoofy1PropertiesFile("goofy-1.bulk.2.properties", 10);
	}

	@Test
	@Category(Slow.class)
	public void testGoofy1Bulk3() {
		testWithGoofy1PropertiesFile("goofy-1.bulk.3.properties", 50);
	}

	@Test
	@Category(VerySlow.class)
	public void testGoofy1Bulk4() {
		testWithGoofy1PropertiesFile("goofy-1.bulk.4.properties", 1000);
	}

	@Test
	@Category(VerySlow.class)
	public void testGoofy1Bulk5() {
		testWithGoofy1PropertiesFile("goofy-1.bulk.5.properties", 1000);
	}

	public Process executeClearance() {

		PersistenceGmSession goofyAccessSession = imp.switchToAccess(GOOFY_ACCESS_ID).session();
		QueryHelper goofyAccessQueryHelper = new QueryHelper(goofyAccessSession).strictly();
		GoofyProcess process = goofyAccessQueryHelper.findUnique(GoofyProcess.T);

		assertThat(process.getActivity()).as("Unexpected proces activity").isEqualTo(ProcessActivity.waiting);

		process.setActivity(ProcessActivity.processing);

		goofyAccessSession.commit();

		waitMiliseconds(1000);

		imp.utils();
		GeneralGmUtils.refreshEntity(process);
		return process;
	}

	private void waitMiliseconds(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public List<GoofyProcess> importProcessesFromFile(String filename, int numberOfProcesses) {
		File file = new File(propertiesDirectoryPath, filename);
		createProcessses(file);

		PersistenceGmSession goofyAccessSession = imp.switchToAccess(GOOFY_ACCESS_ID).session();
		QueryHelper goofyAccessQueryHelper = new QueryHelper(goofyAccessSession);

		logger.info("Wait for the processes to be created");

		SelectQuery countingProcesses = new SelectQueryBuilder().from(GoofyProcess.T, "c").select().count("c").done();

		// wait for all the processes to be created

		int interval = 1 + numberOfProcesses / 100;
		for (int i = 0; i < WAIT_FOR_PROCESS_CREATION_IN_SECONDS; i += interval) {
			long actualNumberOfProcesses = goofyAccessSession.query().select(countingProcesses).first();

			if (actualNumberOfProcesses >= numberOfProcesses) {
				logger.info("All processes created after " + i + " seconds");
				// wait little bit more to see if more processes are created
				waitMiliseconds(500);
				break;
			}

			logger.info("Processes created so far: " + actualNumberOfProcesses);
			waitMiliseconds(1000 * interval);
		}

		List<GoofyProcess> processes = goofyAccessQueryHelper.allPersistedEntities(GoofyProcess.T);
		assertThat(processes).as("Unexpected number of created processes after timeout").hasSize(numberOfProcesses);
		assertThat(processes).as("Resource not uploaded correctly").allMatch(p -> p.getResource().getName().equals(filename));

		return processes;
	}

	public void testWithGoofy1PropertiesFile(String filename, int numberOfProcesses) {
		List<GoofyProcess> processes = importProcessesFromFile(filename, numberOfProcesses);

		testWithProcesses(processes, this::goofyProcess1Ready, this::executeTestForGoofy1);
	}
	public void testWithGoofy2PropertiesFile(String filename, int numberOfProcesses) {
		List<GoofyProcess> processes = importProcessesFromFile(filename, numberOfProcesses);

		testWithProcesses(processes, this::goofyProcess2Ready, this::executeTestForGoofy2);
	}

	private boolean goofyProcess2Ready(GoofyProcess process) {
		return process.getActivity().equals(ProcessActivity.waiting);
	}
	private boolean goofyProcess1Ready(GoofyProcess process) {
		return process.getActivity() != null && process.getActivity().equals(ProcessActivity.ended) && process.getRestartCounters().size() > 0
				&& process.getRestartCounters().stream().findFirst().get().getCount() >= 3 && process.getTrace() != null
				&& process.getTrace().getEvent().equals(MISSING_ERROR_NODE);
	}

	public void testWithProcesses(List<GoofyProcess> processes, Predicate<GoofyProcess> processReady, Consumer<GoofyProcess> testFunction) {
		logger.info("Testing " + processes.size() + " processes");

		int numberOfProcess = 0, round = 0;
		int interval = 1 + processes.size() / 200;

		long timeBefore = System.currentTimeMillis();

		while (processes.size() > 0) {
			waitMiliseconds(1000 * interval);
			round++;
			assertThat(round * interval).as("Processes took too long time to finish").isLessThan(WAIT_FOR_PROCESS_FINISH_IN_SECONDS);
			logger.info("waiting for process #" + numberOfProcess + ". Round: " + round);

			Iterator<GoofyProcess> processIterator = processes.iterator();
			while (processIterator.hasNext()) {
				GoofyProcess process = processIterator.next();
				imp.utils();
				GeneralGmUtils.refreshEntity(process);

				if (processReady.test(process)) {
					numberOfProcess++;

					assertThat(process.getDate()).isNotNull();
					assertThat(process.getLastTransit()).isNotNull();
					assertThat(process.getName()).isNotNull();
					assertThat(process.getNumber()).isNotNull();
					assertThat(process.getTrace()).isNotNull();

					testFunction.accept(process);
					processIterator.remove();
				}
			}
		}

		long passedSeconds = (System.currentTimeMillis() - timeBefore) / 1000;
		logger.info("Passed seconds " + passedSeconds);
		logger.info("Total idle seconds: " + round * interval + " seconds");

	}

	private static class TraceAsserter {
		private final Iterator<ProcessTrace> traceIterator;

		public TraceAsserter(List<ProcessTrace> traces) {
			this.traceIterator = traces.listIterator();
		}

		public TraceAsserter assertNextProcessTrace(String from, String to, String event) {
			String expected = from + " -> " + to + " | " + event;
			assertThat(traceIterator.hasNext()).as("You expected further traces but there are none").isTrue();

			ProcessTrace trace = traceIterator.next();

			String errorMessage = "\nexpected: " + expected + "\nactual: " + prettyTrace(trace) + "\n";

			assertThat(trace.getFromState()).as("Process trace has wrong fromState" + errorMessage).isEqualTo(from);
			assertThat(trace.getToState()).as("Process trace has wrong toState" + errorMessage).isEqualTo(to);
			assertThat(trace.getEvent()).as("Process trace has wrong event" + errorMessage).isEqualTo(event);

			return this;
		}

		public TraceAsserter assertNextProcessTraces(String from, String to, String... events) {
			for (String event : events) {
				assertNextProcessTrace(from, to, event);
			}

			return this;
		}

		public static TraceAsserter forProcess(Process process) {
			return new TraceAsserter(
					process.getTraces().stream().sorted(Comparator.comparingLong(trace -> trace.getId())).collect(Collectors.toList()));
		}

		public void assertNoFurtherTraces() {
			StringBuilder errorMessageBuilder = new StringBuilder();

			boolean foundFurtherTraces = traceIterator.hasNext();

			traceIterator.forEachRemaining(t -> errorMessageBuilder.append("\n" + prettyTrace(t)));

			assertThat(foundFurtherTraces).as("There are more traces that were not expected" + errorMessageBuilder.toString()).isFalse();
		}

		private String prettyTrace(ProcessTrace trace) {
			return trace.getFromState() + " -> " + trace.getToState() + " | " + trace.getEvent();
		}

	}

	public void createProcessses(File file) throws RuntimeException {
		logger.info("Creating process...");

		try {

			/* quick and dirty preread to get the parameters */

			Properties properties = new Properties();

			FileInputStream in = new FileInputStream(file);
			properties.load(in);
			in.close();

			String multiplicatorAsString = properties.getProperty("multiplicator");
			String bulkAsString = properties.getProperty("bulk");

			int multiplicator = multiplicatorAsString != null ? Integer.parseInt(multiplicatorAsString) : 1;
			int bulk = bulkAsString != null ? Integer.parseInt(bulkAsString) : 1;

			in = new FileInputStream(file);

			try {
				Resource resource = goofyAccessSession.resources().create().sourceType(FileUploadSource.T).name(file.getName()).store(in);
				resource.setMimeType("application/goofy");
				int b = 0;
				for (int m = 0; m < multiplicator; b++, m++) {

					GoofyProcess goofyProcess = goofyAccessSession.create(GoofyProcess.T);
					goofyProcess.setResource(resource);
					goofyProcess.setState("decode");

					if ((b + 1) % bulk == 0) {
						goofyAccessSession.commit();
					}
				}
				if (b % bulk != 0) {
					goofyAccessSession.commit();
				}
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						logger.error("error while closing input stream for " + file);
					}
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("error while creating resource from input file " + file, e);
		}
	}
}
