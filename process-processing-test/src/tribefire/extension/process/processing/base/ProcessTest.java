// ============================================================================
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package tribefire.extension.process.processing.base;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.LogManager;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.essential.InvalidArgument;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.service.api.result.Neutral;
import com.braintribe.model.time.TimeSpan;
import com.braintribe.model.time.TimeUnit;

import tribefire.extension.process.api.model.analysis.GetProcessList;
import tribefire.extension.process.api.model.analysis.GetProcessLog;
import tribefire.extension.process.api.model.ctrl.RecoverProcess;
import tribefire.extension.process.api.model.ctrl.ResumeProcess;
import tribefire.extension.process.api.model.ctrl.ResumeProcessToState;
import tribefire.extension.process.api.model.ctrl.ReviveProcesses;
import tribefire.extension.process.api.model.ctrl.StartProcess;
import tribefire.extension.process.api.model.ctrl.StartProcessToState;
import tribefire.extension.process.api.model.ctrl.WaitForProcess;
import tribefire.extension.process.api.model.ctrl.WaitForProcesses;
import tribefire.extension.process.api.model.data.ProcessAndActivities;
import tribefire.extension.process.api.model.data.ProcessIdentification;
import tribefire.extension.process.api.model.data.ProcessList;
import tribefire.extension.process.api.model.data.ProcessLog;
import tribefire.extension.process.data.model.ProcessItem;
import tribefire.extension.process.data.model.log.ProcessLogEntry;
import tribefire.extension.process.data.model.log.ProcessLogEvent;
import tribefire.extension.process.data.model.state.ProcessActivity;
import tribefire.extension.process.data.model.state.TransitionPhase;
import tribefire.extension.process.processing.definition.AmountProcessState;
import tribefire.extension.process.processing.definition.RoutingMaps;
import tribefire.extension.process.processing.definition.SimpleProcessState;
import tribefire.extension.process.reason.model.ProcessNotFound;
import tribefire.extension.process.reason.model.UnexpectedProcessActivity;
import tribefire.extension.process.reason.model.UnexpectedProcessState;
import tribefire.extension.process.test.model.AmountTestProcess;
import tribefire.extension.process.test.model.FailingByExceptionTestProcess;
import tribefire.extension.process.test.model.FailingByReasonTestProcess;
import tribefire.extension.process.test.model.ResumableTestProcess;
import tribefire.extension.process.test.model.SelfRoutingTestProcess;
import tribefire.extension.process.test.model.TestProcess;

public class ProcessTest extends ProcessProcessingTestBase {
	
	@Before
	public void initLog() {
		try (FileInputStream fis =  new FileInputStream("res/logging.properties")){
	        LogManager.getLogManager().readConfiguration(fis);
	    } catch(IOException e) {
	        e.printStackTrace();
	    }
	}
	
	@Test
	public void invalidStart() {
		testStartReasoning(UnexpectedProcessActivity.T, p -> p.setActivity(ProcessActivity.ended));
		testStartReasoning(UnexpectedProcessState.T, p -> p.setState("foobar"));
		testStartReasoning(InvalidArgument.T, () -> {
			TestProcess p = TestProcess.T.create();
			return p;
		});
		testStartReasoning(ProcessNotFound.T, () -> {
			TestProcess p = TestProcess.T.create();
			p.setId("unknown");
			return p;
		});
	}
	
	private void testStartReasoning(EntityType<? extends Reason> expectedReason, Consumer<ProcessItem> configurer) {
		PersistenceGmSession session = testContract.sessionFactory().newSession(ProcessProcessingTestCommons.ACCESS_ID_PROCESSES);
		
		TestProcess process = session.create(TestProcess.T);
		configurer.accept(process);
		
		session.commit();
		
		testStartReasoning(session, expectedReason, () -> process);
	}
	
	private void testStartReasoning(EntityType<? extends Reason> expectedReason, Supplier<TestProcess> toBeStarted) {
		PersistenceGmSession session = testContract.sessionFactory().newSession(ProcessProcessingTestCommons.ACCESS_ID_PROCESSES);
		testStartReasoning(session, expectedReason, toBeStarted);
	}
	
	private void testStartReasoning(PersistenceGmSession session, EntityType<? extends Reason> expectedReason, Supplier<TestProcess> toBeStarted) {
		StartProcess startProcess = StartProcess.T.create();
		startProcess.item(toBeStarted.get());
		
		Maybe<Neutral> maybe = startProcess.eval(session).getReasoned();
		
		Assertions.assertThat(maybe.isUnsatisfied()).isTrue();
		
		Assertions.assertThat((Reason)maybe.whyUnsatisfied()).isInstanceOfAny(expectedReason.getJavaType());
	}
	
	@Test
	public void taggedProcess() {
		PersistenceGmSession session = testContract.sessionFactory().newSession(ProcessProcessingTestCommons.ACCESS_ID_PROCESSES);
		
		TestProcess process = session.create(TestProcess.T);
		session.commit();
		
		StartProcess startProcess = StartProcess.T.create();
		startProcess.item(process);
		
		startProcess.eval(session).get();
		
		WaitForProcess waitForProcess = WaitForProcess.create(process, ProcessActivity.halted, ProcessActivity.ended);
		ProcessActivity result = waitForProcess.eval(session).get();
		
		Assertions.assertThat(result).withFailMessage("waiting for process failed").isSameAs(ProcessActivity.ended);
		
		session.query().entity(process).refresh();
		
		Set<String> expectedTags = Stream.of(SimpleProcessState.values()).map(SimpleProcessState::name).collect(Collectors.toSet());
		
		Assertions.assertThat(process.getTags()).isEqualTo(expectedTags);
		
		LogMatcher logMatcher = new LogMatcher();

		logMatcher.build(ProcessLogEvent.PROCESS_STARTED).done();
		logMatcher.build(ProcessLogEvent.CONDITION_MATCHED).done();
		logMatcher.build(ProcessLogEvent.STATE_CHANGED).state(SimpleProcessState.ONE.name()).done();

		logMatcher.build(ProcessLogEvent.PROCESSOR_EXECUTED).state(SimpleProcessState.ONE.name()).done();
		logMatcher.build(ProcessLogEvent.CONDITION_MATCHED).state(SimpleProcessState.ONE.name()).done();
		logMatcher.build(ProcessLogEvent.STATE_CHANGED).state(SimpleProcessState.TWO.name()).done();
		logMatcher.build(ProcessLogEvent.PROCESSOR_EXECUTED).state(SimpleProcessState.TWO.name()).done();
		logMatcher.build(ProcessLogEvent.CONDITION_MATCHED).state(SimpleProcessState.TWO.name()).done();
		logMatcher.build(ProcessLogEvent.STATE_CHANGED).state(SimpleProcessState.THREE.name()).done();
		logMatcher.build(ProcessLogEvent.PROCESSOR_EXECUTED).state(SimpleProcessState.THREE.name()).done();
		logMatcher.build(ProcessLogEvent.PROCESS_ENDED).state(SimpleProcessState.THREE.name()).done();
		
		printProcessLog(session, process, logMatcher);
	}
	
	//@Test
	public void manyTaggedProcesses() {
		PersistenceGmSession session = testContract.sessionFactory().newSession(ProcessProcessingTestCommons.ACCESS_ID_PROCESSES);

		int processCount = 100_000;
		
		List<TestProcess> processes = new ArrayList<>();
		
		for (int i = 0; i < processCount; i++) {
			TestProcess process = session.create(TestProcess.T);
			process.setId(String.valueOf(i));
			processes.add(process);
		}
		
		session.commit();
		
		WaitForProcesses waitForProcesses = WaitForProcesses.T.create();
		waitForProcesses.setMaxWait(TimeSpan.create(5, TimeUnit.minute));
		
		for (TestProcess process : processes) {
			StartProcess startProcess = StartProcess.T.create();
			startProcess.item(process);
			
			startProcess.eval(session).get();
			
			waitForProcesses.getProcesses().add(ProcessAndActivities.create(process, ProcessActivity.halted, ProcessActivity.ended));
		}
		
		Map<ProcessIdentification, ProcessActivity> waitResults = waitForProcesses.eval(session).get();
		
		Assertions.assertThat(waitResults.values()).withFailMessage("waiting for processes failed").containsOnly(ProcessActivity.ended);		
	}
		
	
	@Test
	public void selfRoutedProcess() {
		PersistenceGmSession session = testContract.sessionFactory().newSession(ProcessProcessingTestCommons.ACCESS_ID_PROCESSES);
		
		SelfRoutingTestProcess process = session.create(SelfRoutingTestProcess.T);
		
		// setup routing map
		Map<String,String> routingMap = RoutingMaps.routingMap(2,1,0);
		
		process.getRoutingMap().putAll(routingMap);
		
		session.commit();
		
		StartProcessToState startProcess = StartProcessToState.T.create();
		startProcess.item(process);
		startProcess.setState(routingMap.get(null));
		
		startProcess.eval(session).get();
		
		WaitForProcess waitForProcess = WaitForProcess.create(process, ProcessActivity.halted, ProcessActivity.ended);
		ProcessActivity result = waitForProcess.eval(session).get();

		printProcessLog(session, process);
		
		Assertions.assertThat(result).withFailMessage("waiting for process failed").isSameAs(ProcessActivity.ended);
		
	}
	
	@Test 
	public void amountProcessEndingInNormal() {
		amountProcess(AmountProcessState.NORMAL);
	}
	
	@Test 
	public void amountProcessEndingInApproval() {
		amountProcess(AmountProcessState.APPROVAL);
	}
	
	@Test 
	public void amountProcessEndingInSpecialAproval() {
		amountProcess(AmountProcessState.SPECIAL_APPROVAL);
	}
	
	public void amountProcess(AmountProcessState endState) {
		PersistenceGmSession session = testContract.sessionFactory().newSession(ProcessProcessingTestCommons.ACCESS_ID_PROCESSES);
		
		AmountTestProcess p = session.create(AmountTestProcess.T);
		
		final double amount;
		
		switch (endState) {
		case NORMAL: amount = 500; break;
		case APPROVAL: amount = 5_000; break;
		case SPECIAL_APPROVAL: amount = 50_000; break;
		default:
			throw new IllegalStateException();
		}
		
		p.setAmount(amount);
		
		session.commit();
		
		StartProcess startProcess = StartProcess.T.create();
		startProcess.item(p);
		startProcess.eval(session).get();
		
		WaitForProcess waitForProcess = WaitForProcess.create(p, ProcessActivity.ended, ProcessActivity.halted);
		
		ProcessActivity processActivity = waitForProcess.eval(session).get();
		
		Assertions.assertThat(processActivity).withFailMessage("waiting for amount process failed").isSameAs(ProcessActivity.ended);

		LogMatcher logMatcher = new LogMatcher();
		
		logMatcher.build(ProcessLogEvent.PROCESS_STARTED).done();
		logMatcher.build(ProcessLogEvent.CONDITION_EVALUATED).done();
		logMatcher.build(ProcessLogEvent.CONDITION_MATCHED).done();
		logMatcher.build(ProcessLogEvent.STATE_CHANGED).state(AmountProcessState.DECISION.name()).done();
		
		switch (endState) {
		case NORMAL:
			logMatcher.build(ProcessLogEvent.CONDITION_EVALUATED).state(AmountProcessState.DECISION.name()).done();
			break;
		case DECISION:
		case APPROVAL:
			logMatcher.build(ProcessLogEvent.CONDITION_EVALUATED).state(AmountProcessState.DECISION.name()).done();
			logMatcher.build(ProcessLogEvent.CONDITION_EVALUATED).state(AmountProcessState.DECISION.name()).done();
			break;
		case SPECIAL_APPROVAL:
			logMatcher.build(ProcessLogEvent.CONDITION_EVALUATED).state(AmountProcessState.DECISION.name()).done();
			logMatcher.build(ProcessLogEvent.CONDITION_EVALUATED).state(AmountProcessState.DECISION.name()).done();
			logMatcher.build(ProcessLogEvent.CONDITION_EVALUATED).state(AmountProcessState.DECISION.name()).done();
			break;
			
		}
		
		logMatcher.build(ProcessLogEvent.CONDITION_MATCHED).state(AmountProcessState.DECISION.name()).done();
		logMatcher.build(ProcessLogEvent.STATE_CHANGED).state(endState.name()).done();
		logMatcher.build(ProcessLogEvent.PROCESS_ENDED).state(endState.name()).done();
		
		printProcessLog(session, p, logMatcher);
		
		session.query().entity(p).refresh();
	}
	
	private void printProcessLog(PersistenceGmSession session, ProcessItem process) {
		printProcessLog(session, process, null, null);
	}
	
	private void printProcessLog(PersistenceGmSession session, ProcessItem process, Integer fromOrder) {
		printProcessLog(session, process, null, fromOrder);
	}
	
	private void printProcessLog(PersistenceGmSession session, ProcessItem process, LogMatcher logMatcher) {
		printProcessLog(session, process, logMatcher, null);
	}
	
	private void printProcessLog(PersistenceGmSession session, ProcessItem process, LogMatcher logMatcher, Integer fromOrder) {
		GetProcessLog getProcessLog = GetProcessLog.T.create();
		getProcessLog.item(process);
		
		if (fromOrder != null)
			getProcessLog.setFromOrder(fromOrder);
		
		ProcessLog processLog = getProcessLog.eval(session).get();
		
		System.out.println(process.entityType().getTypeSignature() + " logs:");
		for (ProcessLogEntry entry: processLog.getEntries()) {
			System.out.println(entry.getDate() + " " + entry.getEvent() + " at [" + entry.getState() + "]: " + entry.getMsg());
		}
		
		if (logMatcher != null)  {
			String message = logMatcher.validation(processLog.getEntries());
			
			if (message != null)
				Assertions.fail("Unexpected Log Profile:\n" +message);
		}
	}
	
	@Test
	public void resumingProcess() {
		internalResumingProcess(RoutingMaps.state(2, 0));
	}
	
	@Test
	public void resumingProcessToState() {
		internalResumingProcess(RoutingMaps.state(2, 1));
	}

	private void internalResumingProcess(String expectedEndState) {
		
		String initialState = RoutingMaps.state(0,0);
		String waitState = RoutingMaps.state(1,1);
		String standardEndState = RoutingMaps.state(1,1);

		
		PersistenceGmSession session = testContract.sessionFactory().newSession(ProcessProcessingTestCommons.ACCESS_ID_PROCESSES);
		
		ResumableTestProcess process = session.create(ResumableTestProcess.T);
		
		// setup routing map
		Map<String,String> routingMap = RoutingMaps.routingMap(0,1,0);
		
		process.getRoutingMap().putAll(routingMap);
		
		session.commit();
		
		StartProcessToState startProcess = StartProcessToState.T.create();
		startProcess.item(process);
		startProcess.setState(routingMap.get(null));
		
		startProcess.eval(session).get();

		// wait for process to reach the state S_1_1 in which it should reach the ProcessActivity.halted
		WaitForProcess waitForWaitingProcess = WaitForProcess.create(process, ProcessActivity.halted, ProcessActivity.waiting);
		ProcessActivity activity = waitForWaitingProcess.eval(session).get();
		
		Assertions.assertThat(activity).withFailMessage("waiting for process in ProcessActivity.waiting failed").isSameAs(ProcessActivity.waiting);
		
		GetProcessList getProcessList = GetProcessList.T.create();
		getProcessList.getActivities().add(ProcessActivity.halted);
		getProcessList.item(process);
		ProcessList processList = getProcessList.eval(session).get();
		
		if (processList.getProcesses().stream().filter(p -> p.getId().equals(process.getId())).count() != 1)
			Assertions.fail("Could not find halted process with GetProcessList");
		
		session.query().entity(process).refresh();
		Assertions.assertThat(process.getState()).withFailMessage("unexpected state of process that reached ProcessActivity.waiting").isEqualTo(RoutingMaps.state(1, 1));
		
		// now resume the waiting process again so that it automatically can reach its end
		final ResumeProcess resumeProcess;
		
		boolean resumeToState = expectedEndState != standardEndState;
		if (resumeToState) {
			ResumeProcessToState resumeProcessToState = ResumeProcessToState.T.create();
			resumeProcessToState.setToState(expectedEndState);
			resumeProcess = resumeProcessToState;
		}
		else {
			resumeProcess = ResumeProcess.T.create();
		}
		
		resumeProcess.item(process);
		resumeProcess.eval(session).get();

		// wait for process to reach the ProcessActivity.ended
		WaitForProcess waitForEndedProcess = WaitForProcess.create(process, ProcessActivity.halted, ProcessActivity.ended);
		activity = waitForEndedProcess.eval(session).get();
		
		Assertions.assertThat(activity).withFailMessage("waiting for process in ProcessActivity.ended failed").isSameAs(ProcessActivity.ended);

		session.query().entity(process).refresh();
		Assertions.assertThat(process.getState()).withFailMessage("unexpected state of process that reached ProcessActivity.ended").isEqualTo(expectedEndState);
		
		
		LogMatcher logMatcher = new LogMatcher();
		
		logMatcher.build(ProcessLogEvent.PROCESS_STARTED).done();
		logMatcher.build(ProcessLogEvent.NEXT_STATE_SELECTED).done();
		logMatcher.build(ProcessLogEvent.STATE_CHANGED).state(initialState).done();
		
		
		logMatcher.build(ProcessLogEvent.PROCESSOR_EXECUTED).state(initialState).done();
		logMatcher.build(ProcessLogEvent.NEXT_STATE_SELECTED).state(initialState).done();
		logMatcher.build(ProcessLogEvent.STATE_CHANGED).state(waitState).done();
		logMatcher.build(ProcessLogEvent.PROCESSOR_EXECUTED).state(waitState).done();
		logMatcher.build(ProcessLogEvent.NEXT_STATE_SELECTED).state(waitState).done();
		logMatcher.build(ProcessLogEvent.PROCESS_SUSPENDED).state(waitState).done();
		
		if (resumeToState)
			logMatcher.build(ProcessLogEvent.NEXT_STATE_SELECTED).state(waitState).done();
		
		logMatcher.build(ProcessLogEvent.PROCESS_RESUMED).state(waitState).done();
		logMatcher.build(ProcessLogEvent.STATE_CHANGED).state(expectedEndState).done();
		logMatcher.build(ProcessLogEvent.PROCESSOR_EXECUTED).state(expectedEndState).done();
		logMatcher.build(ProcessLogEvent.PROCESS_ENDED).state(expectedEndState).done();
		
		printProcessLog(session, process, logMatcher);
	}
	
	@Test
	public void recoverProcessRerunProcessor() {
		EntityType<FailingByReasonTestProcess> processType = FailingByReasonTestProcess.T;
		PersistenceGmSession session = testContract.sessionFactory().newSession(ProcessProcessingTestCommons.ACCESS_ID_PROCESSES);
		
		FailingByReasonTestProcess failedProcess = failingProcess(processType, session);
		
		session.query().entity(failedProcess).refresh();
		
		int logSeq = failedProcess.logSequence();
		
		failedProcess.setDoNotFail(true);
		session.commit();
		
		RecoverProcess recoverProcess = RecoverProcess.T.create();
		recoverProcess.item(failedProcess);
	
		recoverProcess.eval(session).get();
		
		WaitForProcess waitForProcess = WaitForProcess.create(failedProcess, ProcessActivity.halted, ProcessActivity.ended);
		ProcessActivity result = waitForProcess.eval(session).get();
		
		Assertions.assertThat(result).withFailMessage("waiting for process failed").isSameAs(ProcessActivity.ended);
		
		printProcessLog(session, failedProcess, logSeq);
	}
	
	@Test
	public void recoverProcessSkipProcessor() {
		EntityType<FailingByReasonTestProcess> processType = FailingByReasonTestProcess.T;
		PersistenceGmSession session = testContract.sessionFactory().newSession(ProcessProcessingTestCommons.ACCESS_ID_PROCESSES);
		
		FailingByReasonTestProcess failedProcess = failingProcess(processType, session);
		
		session.query().entity(failedProcess).refresh();
		
		int logSeq = failedProcess.logSequence();
		
		RecoverProcess recoverProcess = RecoverProcess.T.create();
		recoverProcess.setTransitionPhase(TransitionPhase.COMPLETED_TRANSITION);
		recoverProcess.setTransitionProcessorId(null);
		recoverProcess.item(failedProcess);
		
		recoverProcess.eval(session).get();
		
		WaitForProcess waitForProcess = WaitForProcess.create(failedProcess, ProcessActivity.halted, ProcessActivity.ended);
		ProcessActivity result = waitForProcess.eval(session).get();
		
		Assertions.assertThat(result).withFailMessage("waiting for process failed").isSameAs(ProcessActivity.ended);
		printProcessLog(session, failedProcess, logSeq);
	}
	
	@Test
	public void failingByReasonProcess() {
		EntityType<FailingByReasonTestProcess> processType = FailingByReasonTestProcess.T;
		
		failingProcess(processType);
	}
	
	@Test
	public void failingByExceptionProcess() {
		failingProcess(FailingByExceptionTestProcess.T);
	}


	private <T extends TestProcess> T failingProcess(EntityType<T> processType) {
		PersistenceGmSession session = testContract.sessionFactory().newSession(ProcessProcessingTestCommons.ACCESS_ID_PROCESSES);
		T process = failingProcess(processType, session);
		
		GetProcessLog getProcessLog = GetProcessLog.T.create();
		getProcessLog.item(process);
		
		ProcessLog processLog = getProcessLog.eval(session).get();
		
		LogMatcher logMatcher = new LogMatcher();
		
		logMatcher.build(ProcessLogEvent.PROCESS_STARTED).done();
		logMatcher.build(ProcessLogEvent.CONDITION_MATCHED).done();
		logMatcher.build(ProcessLogEvent.STATE_CHANGED).state(SimpleProcessState.ONE.name()).done();
		
		logMatcher.build(ProcessLogEvent.CONDITION_MATCHED).state(SimpleProcessState.ONE.name()).done();
		logMatcher.build(ProcessLogEvent.STATE_CHANGED).state(SimpleProcessState.TWO.name()).done();
		logMatcher.build(ProcessLogEvent.ERROR_IN_PROCESSOR).state(SimpleProcessState.TWO.name()).done();
		logMatcher.build(ProcessLogEvent.PROCESS_HALTED).state(SimpleProcessState.TWO.name()).done();
		
		System.out.println(process.entityType().getTypeSignature() + " logs:");
		for (ProcessLogEntry entry: processLog.getEntries()) {
			System.out.println(entry);
		}

		String message = logMatcher.validation(processLog.getEntries());
		
		if (message != null)
			Assertions.fail("Unexpected Log Profile:\n" +message);

		return process;
	}
	
	
	private <T extends TestProcess> T failingProcess(EntityType<T> processType, PersistenceGmSession session) {
		T process = session.create(processType);
		
		session.commit();
		
		StartProcess startProcess = StartProcess.T.create();
		startProcess.item(process);
		
		startProcess.eval(session).get();
		
		WaitForProcess waitForProcess = WaitForProcess.create(process, ProcessActivity.halted, ProcessActivity.ended);
		ProcessActivity result = waitForProcess.eval(session).get();
		
		Assertions.assertThat(result).withFailMessage("waiting for process failed").isSameAs(ProcessActivity.halted);
		
		return process;
	}
	
	@Test
	public void reviveUnattended() {
		PersistenceGmSession session = testContract.sessionFactory().newSession(ProcessProcessingTestCommons.ACCESS_ID_PROCESSES);

		TestProcess p1 = createUnattendedTestProcess(session);
		TestProcess p2 = createUnattendedTestProcess(session);
		
		session.commit();
		
		ReviveProcesses reviveProcesses = ReviveProcesses.T.create();
		reviveProcesses.eval(session).get();
		
		WaitForProcesses waitForProcesses = WaitForProcesses.T.create();
		waitForProcesses.getProcesses().add(ProcessAndActivities.create(p1, ProcessActivity.ended, ProcessActivity.halted));
		waitForProcesses.getProcesses().add(ProcessAndActivities.create(p2, ProcessActivity.ended, ProcessActivity.halted));
		
		Map<ProcessIdentification, ProcessActivity> waitResults = waitForProcesses.eval(session).get();
		
		Assertions.assertThat(waitResults.values()).withFailMessage("waiting for revived processes failed").containsOnly(ProcessActivity.ended);
		
	}
	
	private TestProcess createUnattendedTestProcess(PersistenceGmSession session) {
		Date now = new Date();
		Date startedDate = new Date(now.getTime() - Duration.ofMinutes(30).toMillis());
		Date lastTransit = new Date(startedDate.getTime() + Duration.ofSeconds(5).toMillis());
		
		TestProcess process = session.create(TestProcess.T);
		process.setActivity(ProcessActivity.processing);
		process.setPreviousState(SimpleProcessState.ONE.name());
		process.setState(SimpleProcessState.TWO.name());
		process.setStartedAt(startedDate);
		process.setLastTransit(lastTransit);
		process.setTransitionPhase(TransitionPhase.COMPLETED_TRANSITION);
		
		return process;
	}
}
