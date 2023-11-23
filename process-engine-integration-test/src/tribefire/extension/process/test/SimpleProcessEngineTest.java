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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.model.accessdeployment.smood.CollaborativeSmoodAccess;
import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.extensiondeployment.AccessAspect;
import com.braintribe.model.generic.reflection.SimpleType;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.OrderingDirection;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.product.rat.imp.ImpApi;
import com.braintribe.qa.tribefire.process.InvoiceProcess;
import com.braintribe.qa.tribefire.process.InvoiceProcessSub;
import com.braintribe.qa.tribefire.process.InvoiceProcessSubSub;
import com.braintribe.qa.tribefire.process.InvoiceProcessWithPriority;
import com.braintribe.qa.tribefire.test.Invoice;
import com.braintribe.testing.category.KnownIssue;
import com.braintribe.utils.DateTools;
import com.braintribe.utils.lcd.CommonTools;

import tribefire.extension.process.imp.ProcessDefinitionImp;
import tribefire.extension.process.imp.ProcessDefinitionImpCave;
import tribefire.extension.process.imp.ProcessingEngineImpCave;
import tribefire.extension.process.model.data.HasExecutionPriority;
import tribefire.extension.process.model.data.Process;
import tribefire.extension.process.model.deployment.ProcessingEngine;
import tribefire.extension.process.model.scripting.deployment.ScriptedConditionProcessor;
import tribefire.extension.process.model.scripting.deployment.ScriptedTransitionProcessor;
import tribefire.extension.scripting.deployment.model.GroovyScript;

public class SimpleProcessEngineTest extends AbstractPersistenceTest {

	@Test
	@Category(KnownIssue.class) // This test is always stable locally but unstable in our Jenkins CI
	public void testProcessingEngine_invoiceProcess() throws GmSessionException, InterruptedException {
		final String ROOT = null;
		final String IMPORT_STATE = "import";
		final String VALIDATE = "validate";
		final String NON_CONFIDENTIAL = "nonConfidential";
		final String CONFIDENTIAL = "confidential";

		ProcessingEngine processingEngine;
		final List<Deployable> createdDeployables = new ArrayList<>();

		logger.info("Starting DevQA-test: executing state engine processing...");

		ImpApi imp = apiFactory().build();
		GmMetaModel familyMetaModel = createFamilyModel(imp);

		// create Process model based on the ProcessModel and merged with FamilyModel
		GmMetaModel processMetaModel = imp.model("com.braintribe.gm:process-model").get();
		GmMetaModel familyProcessMetaModel = imp.model().create(modelName("FamilyProcess"), processMetaModel, familyMetaModel).get();
		// @formatter:off
		imp.model().entityType()
			.create(InvoiceProcess.T, familyProcessMetaModel)
			.addProperty("invoiceState", SimpleType.TYPE_STRING)
			.addProperty("invoice", Invoice.T)
			.addInheritance(Process.T)
			.get();
		imp.commit();


		// set up the processing engine with all its parts
		// ------------------------------------------------------------------------------------------------------

		ProcessingEngineImpCave peCave = new ProcessingEngineImpCave(imp.session());
		processingEngine = peCave.createProcessingEngine(name("pe")).get();
		
		imp.commit();

		ScriptedTransitionProcessor scriptedTransitionProcessor = peCave
		.createScriptedTransitionProcessor(name("TransitionProcessor"), name("TransitionProcessor"))
		.addScript(GroovyScript.T, "$context.process.invoiceState='validate'")
		.get();

		ScriptedConditionProcessor scriptedConditionProcessor = peCave
		.createScriptedCondition(name("Condition"), name("Condition"))
		.addScript(GroovyScript.T, "$context.process.invoice.total > 250000;")
		.get();

		AccessAspect stateChangeProcessingAspect = imp.deployable()
				.access()
					.aspect()
						.createStateProcessingAspect(name("aspect.statechange"), name("aspect.statechange"), processingEngine)
						.get();

		imp.commit();

		ProcessDefinitionImpCave pdCave = new ProcessDefinitionImpCave(imp.session());
		pdCave.create(name("Definition"))
			.setTrigger(InvoiceProcess.T, "invoiceState")
			.addStandardNodes(ROOT, IMPORT_STATE, CONFIDENTIAL, NON_CONFIDENTIAL, VALIDATE)
			.addOnEntered(IMPORT_STATE, scriptedTransitionProcessor)
			.addConditionalEdge(VALIDATE, CONFIDENTIAL, scriptedConditionProcessor)
			.addConditionalEdge(VALIDATE, NON_CONFIDENTIAL, null)
			.addEdge(ROOT, IMPORT_STATE)
			.addEdge(IMPORT_STATE, VALIDATE)
			.addToProcessingEngine(processingEngine);

		imp.commit();

		CollaborativeSmoodAccess familyAccess = imp.deployable()
				.access()
					.createCsa(name("Access"), name("Access"), familyProcessMetaModel)
					.addAspect(stateChangeProcessingAspect)
					.get();

		createdDeployables.addAll(CommonTools.getList(
				scriptedTransitionProcessor,
				scriptedConditionProcessor,
				processingEngine,
				stateChangeProcessingAspect,
				familyAccess));
		// @formatter:on

		imp.commit();

		imp.service().deployRequest(createdDeployables).call();

		// create some invoice- and process entities to feed the engine
		// ------------------------------------------------------------------------------------------------------
		PersistenceGmSessionFactory factory = apiFactory().buildSessionFactory();

		PersistenceGmSession session2 = factory.newSession(familyAccess.getExternalId());
		logger.info("Created session for access '" + familyAccess.getExternalId() + "'.");

		Invoice invoice1 = session2.create(Invoice.T);
		invoice1.setTotal(Double.valueOf(300000));

		InvoiceProcess invoiceProcess1 = session2.create(InvoiceProcess.T);
		invoiceProcess1.setInvoice(invoice1);
		invoiceProcess1.setInvoiceState(IMPORT_STATE);

		session2.commit();

		Invoice invoice2 = session2.create(Invoice.T);
		invoice2.setTotal(Double.valueOf(100000));

		InvoiceProcess invoiceProcess2 = session2.create(InvoiceProcess.T);
		invoiceProcess2.setInvoice(invoice2);
		invoiceProcess2.setInvoiceState(IMPORT_STATE);

		session2.commit();

		// give the processing engine some time to do its work
		Thread.sleep(5000);

		// check if the outcome is as expected
		PersistenceGmSession session3 = factory.newSession(familyAccess.getExternalId());
		logger.info("Checking the final state of the [" + InvoiceProcess.class.getName() + "]");
		InvoiceProcess invoiceProcess1Retrieved = session3.query().entity(invoiceProcess1).refresh();
		System.out.println(invoiceProcess1Retrieved.getInvoiceState());
		assertThat(invoiceProcess1Retrieved.getInvoiceState()).isEqualTo(CONFIDENTIAL);
		assertThat(invoiceProcess1Retrieved.getTrace().getEvent()).isEqualTo("process-ended");
		InvoiceProcess invoiceProcess2Retrieved = session3.query().entity(invoiceProcess2).refresh();
		assertThat(invoiceProcess2Retrieved.getInvoiceState()).isEqualTo(NON_CONFIDENTIAL);
		assertThat(invoiceProcess2Retrieved.getTrace().getEvent()).isEqualTo("process-ended");

		logger.info("All assertions have completed succefully!");
		logger.info("Completed DevQA-test: executing state engine processing.");
	}

	@Test
	@Category(KnownIssue.class) // This test is always stable locally but unstable in our Jenkins CI
	public void testProcessingEngine_triggerType() throws GmSessionException, InterruptedException {
		final String ROOT = null;
		final String IMPORT_STATE = "import";
		final String VALIDATE = "validate";
		final String NON_CONFIDENTIAL = "nonConfidential";
		final String CONFIDENTIAL = "confidential";
		
		ProcessingEngine processingEngine;
		final List<Deployable> createdDeployables = new ArrayList<>();
		
		logger.info("Starting DevQA-test: executing state engine processing...");
		
		ImpApi imp = apiFactory().build();
		GmMetaModel familyMetaModel = createFamilyModel(imp);
		
		// create Process model based on the ProcessModel and merged with FamilyModel
		GmMetaModel processMetaModel = imp.model("com.braintribe.gm:process-model").get();
		GmMetaModel familyProcessMetaModel = imp.model().create(modelName("FamilyProcess"), processMetaModel, familyMetaModel).get();
		// @formatter:off
		GmEntityType gmEntityTypeInvoiceProcess = imp.model().entityType()
			.create(InvoiceProcess.T, familyProcessMetaModel)
			.addProperty("invoiceState", SimpleType.TYPE_STRING)
			.addProperty("invoice", Invoice.T)
			.addInheritance(Process.T)
			.get();
		
		GmEntityType gmEntityTypeSub = imp.model().entityType()
			.create(InvoiceProcessSub.T, familyProcessMetaModel)
			.addInheritance(gmEntityTypeInvoiceProcess)
			.get();
		
		imp.model().entityType()
			.create(InvoiceProcessSubSub.T, familyProcessMetaModel)
			.addInheritance(gmEntityTypeSub);
		
		imp.commit();
		
		
		// set up the processing engine with all its parts
		// ------------------------------------------------------------------------------------------------------
		
		ProcessingEngineImpCave peCave = new ProcessingEngineImpCave(imp.session());
		processingEngine = peCave.createProcessingEngine(name("pe")).get();
		
		imp.commit();
		
		ScriptedTransitionProcessor scriptedTransitionProcessor = peCave
				.createScriptedTransitionProcessor(name("TransitionProcessor"), name("TransitionProcessor"))
				.addScript(GroovyScript.T, "$context.process.invoiceState='validate'")
				.get();
		
		ScriptedConditionProcessor scriptedConditionProcessor = peCave
				.createScriptedCondition(name("Condition"), name("Condition"))
				.addScript(GroovyScript.T, "$context.process.invoice.total > 250000;")
				.get();
		
		AccessAspect stateChangeProcessingAspect = imp.deployable()
				.access()
				.aspect()
				.createStateProcessingAspect(name("aspect.statechange"), name("aspect.statechange"), processingEngine)
				.get();
		
		imp.commit();
		
		ProcessDefinitionImpCave pdCave = new ProcessDefinitionImpCave(imp.session());
		pdCave.create(name("Definition"))
		.setTrigger(InvoiceProcess.T, "invoiceState")
		.setTriggerType(InvoiceProcessSub.T)
		.addStandardNodes(ROOT, IMPORT_STATE, CONFIDENTIAL, NON_CONFIDENTIAL, VALIDATE)
		.addOnEntered(IMPORT_STATE, scriptedTransitionProcessor)
		.addConditionalEdge(VALIDATE, CONFIDENTIAL, scriptedConditionProcessor)
		.addConditionalEdge(VALIDATE, NON_CONFIDENTIAL, null)
		.addEdge(ROOT, IMPORT_STATE)
		.addEdge(IMPORT_STATE, VALIDATE)
		.addToProcessingEngine(processingEngine);
		
		imp.commit();
		
		CollaborativeSmoodAccess familyAccess = imp.deployable()
				.access()
				.createCsa(name("Access"), name("Access"), familyProcessMetaModel)
				.addAspect(stateChangeProcessingAspect)
				.get();
		
		createdDeployables.addAll(CommonTools.getList(
				scriptedTransitionProcessor,
				scriptedConditionProcessor,
				processingEngine,
				stateChangeProcessingAspect,
				familyAccess));
		// @formatter:on
		
		imp.commit();
		
		imp.service().deployRequest(createdDeployables).call();
		
		// create some invoice- and process entities to feed the engine
		// ------------------------------------------------------------------------------------------------------
		PersistenceGmSessionFactory factory = apiFactory().buildSessionFactory();
		
		PersistenceGmSession session2 = factory.newSession(familyAccess.getExternalId());
		logger.info("Created session for access '" + familyAccess.getExternalId() + "'.");
		
		Invoice invoice1 = session2.create(Invoice.T);
		invoice1.setTotal(Double.valueOf(300000));
		
		InvoiceProcess invoiceProcess1 = session2.create(InvoiceProcess.T);
		invoiceProcess1.setInvoice(invoice1);
		invoiceProcess1.setInvoiceState(IMPORT_STATE);
		
		session2.commit();
		
		Invoice invoice2 = session2.create(Invoice.T);
		invoice2.setTotal(Double.valueOf(100000));
		
		InvoiceProcess invoiceProcess2 = session2.create(InvoiceProcessSub.T);
		invoiceProcess2.setInvoice(invoice1);
		invoiceProcess2.setInvoiceState(IMPORT_STATE);
		
		InvoiceProcess invoiceProcess3 = session2.create(InvoiceProcessSubSub.T);
		invoiceProcess3.setInvoice(invoice2);
		invoiceProcess3.setInvoiceState(IMPORT_STATE);
		
		session2.commit();
		
		// give the processing engine some time to do its work
		Thread.sleep(5000);
		
		// check if the outcome is as expected
		PersistenceGmSession session3 = factory.newSession(familyAccess.getExternalId());
		logger.info("Checking the final state of the [" + InvoiceProcess.class.getName() + "]");
		
		// Process with trigger type should have been triggered
		InvoiceProcess invoiceProcess2Retrieved = session3.query().entity(invoiceProcess2).refresh();
		assertThat(invoiceProcess2Retrieved.getInvoiceState()).isEqualTo(CONFIDENTIAL);
		assertThat(invoiceProcess2Retrieved.getTrace().getEvent()).isEqualTo("process-ended");
		
		// Process with subtype of  trigger type should have been triggered
		InvoiceProcess invoiceProcess3Retrieved = session3.query().entity(invoiceProcess3).refresh();
		assertThat(invoiceProcess3Retrieved.getInvoiceState()).isEqualTo(NON_CONFIDENTIAL);
		assertThat(invoiceProcess3Retrieved.getTrace().getEvent()).isEqualTo("process-ended");
		
		// Process which is not of trigger type should not have been triggered
		InvoiceProcess invoiceProcess1Retrieved = session3.query().entity(invoiceProcess1).refresh();
		System.out.println(invoiceProcess1Retrieved.getInvoiceState());
		assertThat(invoiceProcess1Retrieved.getInvoiceState()).isEqualTo(IMPORT_STATE);
		assertThat(invoiceProcess1Retrieved.getTrace()).isNull();
		
		logger.info("All assertions have completed succefully!");
		logger.info("Completed DevQA-test: executing state engine processing.");
	}
	
	@Test
	@Category(KnownIssue.class) // This test is always stable locally but unstable in our Jenkins CI
	public void testProcessingEngine_invoiceProcessWithPriority() throws Exception {
		ProcessingEngine processingEngine;
		final List<Deployable> createdDeployables = new ArrayList<>();

		final String ROOT = null;
		final String DONE = "done";

		logger.info("Starting DevQA-test: executing state engine processing with priority...");

		ImpApi imp = apiFactory().build();
		GmMetaModel familyMetaModel = createFamilyModel(imp);

		// create Process model based on the ProcessModel and merged with FamilyModel
		GmMetaModel processMetaModel = imp.model("com.braintribe.gm:process-model").get();
		GmMetaModel familyProcessMetaModel = imp.model().create(modelName("FamilyProcess"), processMetaModel, familyMetaModel).get();
		// @formatter:off
		imp.model().entityType()
		.create(InvoiceProcessWithPriority.T, familyProcessMetaModel)
		.addProperty("finished", SimpleType.TYPE_DATE)
		.addProperty("testIterationId", SimpleType.TYPE_STRING)
		.addProperty("ordering", SimpleType.TYPE_LONG)
		.addProperty("invoiceState", SimpleType.TYPE_STRING)
		.addProperty("invoice", Invoice.T)
		.addInheritance(Process.T)
		.addInheritance(HasExecutionPriority.T)
		.get();
		imp.commit();


		// set up the processing engine with all its parts
		// ------------------------------------------------------------------------------------------------------

		ProcessingEngineImpCave peCave = new ProcessingEngineImpCave(imp.session());
		processingEngine = peCave.createProcessingEngine(name("pe")).get();

		imp.commit();

		final String testIterationId = UUID.randomUUID().toString();

		int stepCount = 5;
		int processCount = 1000;
		int pusher = 1;

		String[] states = new String[stepCount+2];
		states[0] = ROOT;
		states[stepCount+1] = DONE;
		for (int i=0; i<stepCount; ++i) {
			states[i+1] = "step"+i;
		}

		// (ROOT) -> (0) -> (1) -> (2) -> (3) -> (4) -> (DONE)

		ScriptedTransitionProcessor[] scriptedTransitionProcessors = new ScriptedTransitionProcessor[stepCount];
		for (int i=0; i<stepCount-1; ++i) {
			scriptedTransitionProcessors[i] =  peCave
					.createScriptedTransitionProcessor(name("TransitionProcessor-"+i), name("TransitionProcessor-"+i))
					.addScript(GroovyScript.T, "$context.process.invoiceState = \"step"+(i+1)+"\"; Thread.sleep(100L);")
					.get();
		}
		scriptedTransitionProcessors[stepCount-1] =  peCave
				.createScriptedTransitionProcessor(name("TransitionProcessor-"+DONE), name("TransitionProcessor-"+DONE))
				.addScript(GroovyScript.T, "$context.process.invoiceState = \""+DONE+"\"; $context.process.finished = new Date(); $context.process.ordering = com.braintribe.utils.LongIdGenerator.provideLongId();")
				.get();

		AccessAspect stateChangeProcessingAspect = imp.deployable()
				.access()
				.aspect()
				.createStateProcessingAspect(name("aspect.statechange"), name("aspect.statechange"), processingEngine)
				.get();

		imp.commit();

		ProcessDefinitionImpCave pdCave = new ProcessDefinitionImpCave(imp.session());
		ProcessDefinitionImp definitionImp = pdCave.create(name("Definition With Priority"))
				.setTrigger(InvoiceProcessWithPriority.T, "invoiceState")
				.addStandardNodes(states);

		for (int i=0; i<states.length-1; ++i) {
			definitionImp.addEdge(states[i], states[i+1]);
		}
		for (int i=0; i<stepCount; ++i) {
			definitionImp.addOnEntered(states[i+1], scriptedTransitionProcessors[i]);
		}


		definitionImp.addToProcessingEngine(processingEngine);

		imp.commit();

		CollaborativeSmoodAccess familyAccess = imp.deployable()
				.access()
				.createCsa(name("AccessWithPriority"), name("AccessWithPriority"), familyProcessMetaModel)
				.addAspect(stateChangeProcessingAspect)
				.get();

		createdDeployables.addAll(CommonTools.getList(
				processingEngine,
				stateChangeProcessingAspect,
				familyAccess));
		for (ScriptedTransitionProcessor p : scriptedTransitionProcessors) {
			createdDeployables.add(p);
		}

		// @formatter:on

		imp.commit();

		imp.service().deployRequest(createdDeployables).call();

		Thread.sleep(1000l);

		PersistenceGmSessionFactory factory2 = apiFactory().buildSessionFactory();

		PersistenceGmSession session2 = factory2.newSession(familyAccess.getExternalId());
		logger.info("Created session for access '" + familyAccess.getExternalId() + "'.");

		// create some invoice- and process entities to feed the engine
		// ------------------------------------------------------------------------------------------------------

		long pushStart = System.currentTimeMillis();

		ExecutorService service = Executors.newFixedThreadPool(pusher);
		try {
			List<Future<?>> futures = new ArrayList<>();
			for (int i = 0; i < pusher; ++i) {
				futures.add(service.submit(() -> {

					PersistenceGmSessionFactory factory = apiFactory().buildSessionFactory();
					PersistenceGmSession pusherSession = factory.newSession(familyAccess.getExternalId());

					for (int j = 0; j < processCount; ++j) {
						Invoice invoice = pusherSession.create(Invoice.T);
						invoice.setTotal(10d);

						InvoiceProcessWithPriority invoiceProcess = pusherSession.create(InvoiceProcessWithPriority.T);
						invoiceProcess.setInvoice(invoice);
						invoiceProcess.setInvoiceState(states[1]);
						invoiceProcess.setExecutionPriority(0d);
						invoiceProcess.setTestIterationId(testIterationId);

						pusherSession.commit();
					}

				}));
			}
			for (Future<?> f : futures) {
				f.get();
			}
		} finally {
			service.shutdown();
		}

		long pushEnd = System.currentTimeMillis();

		int expectedCount = (pusher * processCount) + 1;

		logger.info("Pushing " + (pusher * processCount) + " normal processes took: " + (pushEnd - pushStart) + " ms.");

		// After creating many process contexts, we will now create another one with a higher priority
		// It should not finish as the last one

		Invoice invoice1 = session2.create(Invoice.T);
		invoice1.setTotal(100d);

		InvoiceProcessWithPriority invoiceProcess1 = session2.create(InvoiceProcessWithPriority.T);
		invoiceProcess1.setInvoice(invoice1);
		invoiceProcess1.setInvoiceState(states[1]);
		invoiceProcess1.setExecutionPriority(100d);
		invoiceProcess1.setTestIterationId(testIterationId);

		session2.commit();

		long highPrioEnd = System.currentTimeMillis();

		logger.info("Pushing one high prio process took: " + (highPrioEnd - pushEnd) + " ms.");

		// check if the outcome is as expected
		PersistenceGmSession session3 = factory2.newSession(familyAccess.getExternalId());

		// give the processing engine some time to do its work
		long maxWait = 120_000l;
		long start = System.currentTimeMillis();
		long waitedSoFar = 0l;
		int foundDone = 0;
		// @formatter:off
		SelectQuery countQuery = (new SelectQueryBuilder()).from(InvoiceProcessWithPriority.T, "p")
			.where()
				.conjunction()
					.property("p", InvoiceProcessWithPriority.testIterationId).eq(testIterationId)
					.property("p", InvoiceProcessWithPriority.invoiceState).eq(DONE)
				.close()
			.select().count("p")
			.done();
		// @formatter:on
		do {
			Thread.sleep(5000);
			waitedSoFar = System.currentTimeMillis() - start;
			List<Long> result = session3.query().select(countQuery).value();
			foundDone = result.get(0).intValue();
		} while ((foundDone < expectedCount) && (waitedSoFar < maxWait));

		logger.info("Quit count loop after: " + (System.currentTimeMillis() - start) + " ms.");

		assertThat(foundDone).isEqualTo(expectedCount);

		Thread.sleep(5000);

		logger.info("Checking the final state of the [" + InvoiceProcessWithPriority.class.getName() + "]");

		// @formatter:off
		EntityQuery query = EntityQueryBuilder.from(InvoiceProcessWithPriority.T)
				.where()
					.property(InvoiceProcessWithPriority.testIterationId)
						.eq(testIterationId)
				.orderBy(InvoiceProcessWithPriority.ordering, OrderingDirection.ascending)
				.done();
		// @formatter:on

		List<InvoiceProcessWithPriority> list = session3.query().entities(query).list();
		int size = list.size();
		assertThat(size).isEqualTo(expectedCount);

		int indexOfHighPrioProcess = -1;
		
		for (int i = 0; i < size; ++i) {
			InvoiceProcessWithPriority invoiceProcess1Retrieved = list.get(i);
			assertThat(invoiceProcess1Retrieved.getInvoiceState()).isEqualTo(DONE);
			// assertThat(invoiceProcess1Retrieved.getTrace().getEvent()).isEqualTo("process-ended"); Jeez... DevQa
			// can't even finish the processes after 90s

			String message = "Process at position: " + i + " has order number: " + invoiceProcess1Retrieved.getOrdering();
			Date d = invoiceProcess1Retrieved.getFinished();
			if (d != null) {
				message += ", finished at: " + DateTools.encode(d, DateTools.LEGACY_DATETIME_WITH_MS_FORMAT);
			}
			if (invoiceProcess1Retrieved.getInvoice().getTotal() == 100d) {
				indexOfHighPrioProcess = i;
				logger.info(message + " ***");
			} else {
				logger.info(message);
			}

		}

		logger.info("High prio process is at position: " + indexOfHighPrioProcess);

		assertThat(indexOfHighPrioProcess).isGreaterThan(-1);
		assertThat(indexOfHighPrioProcess).isLessThan(size - 1);

		logger.info("All assertions have completed succefully!");
		logger.info("Completed DevQA-test: executing state engine processing with priority.");
	}

	@Before
	@After
	public void tearDown() {
		eraseTestEntities();
	}
}
