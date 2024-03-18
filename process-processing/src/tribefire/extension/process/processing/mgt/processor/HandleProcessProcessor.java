// ============================================================================
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package tribefire.extension.process.processing.mgt.processor;

import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.function.Supplier;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.InternalError;
import com.braintribe.logging.Logger;
import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.service.api.MulticastRequest;
import com.braintribe.model.service.api.result.Neutral;
import com.braintribe.model.time.TimeSpan;

import tribefire.extension.process.api.model.ctrl.HandleProcess;
import tribefire.extension.process.api.model.ctrl.NotifyProcessActivity;
import tribefire.extension.process.data.model.ProcessItem;
import tribefire.extension.process.data.model.log.ProcessLogEvent;
import tribefire.extension.process.data.model.state.ProcessActivity;
import tribefire.extension.process.data.model.state.TransitionPhase;
import tribefire.extension.process.model.deployment.ConditionProcessor;
import tribefire.extension.process.model.deployment.ConditionalEdge;
import tribefire.extension.process.model.deployment.Edge;
import tribefire.extension.process.model.deployment.Node;
import tribefire.extension.process.model.deployment.RestartNode;
import tribefire.extension.process.model.deployment.StandardNode;
import tribefire.extension.process.model.deployment.TransitionProcessor;
import tribefire.extension.process.processing.mgt.BasicConditionProcessorContext;
import tribefire.extension.process.processing.mgt.BasicTransitionProcessorContext;
import tribefire.extension.process.processing.oracle.TransitionOracle;
import tribefire.extension.process.reason.model.EdgeNotFound;
import tribefire.extension.process.reason.model.UnexpectedProcessActivity;
import tribefire.extension.process.reason.model.UnexpectedProcessState;

public class HandleProcessProcessor extends OracledProcessRequestProcessor<HandleProcess, Neutral> {
	private static final Logger logger = Logger.getLogger(HandleProcessProcessor.class);
	private TransitionOracle transitionOracle;
	
	@Override
	protected Reason validateItem(ProcessItem processItem) {
		Reason superReason = super.validateItem(processItem);
		
		if (superReason != null)
			return superReason;
		
		if (processItem.getActivity() != ProcessActivity.processing && processItem.getActivity() != ProcessActivity.waiting) {
			return Reasons.build(UnexpectedProcessActivity.T) //
					.text("ProcessItem " + processItem + " is in unexpected activity " + processItem.getActivity()) //
					.toReason();
		}
		
		return null;
	}
	
	@Override
	protected Maybe<Neutral> processWithLockedItem() {
		if (processItem.getActivity() == ProcessActivity.processing) {
			return handleProcess();
		}
		else {
			return handleWaitingProcess();
		}
	}

	private Maybe<Neutral> handleWaitingProcess() {
		Date overdueAt = processItem.getOverdueAt();
		
		if (overdueAt == null)
			return Maybe.complete(Neutral.NEUTRAL);
	
		Date now = new Date();
	
		// overdue check
		if (now.after(overdueAt)) {
			processItem.setActivity(ProcessActivity.processing);
			log(ProcessLogEvent.PROCESS_IS_OVERDUE, "process is overdue");
			log(ProcessLogEvent.PROCESS_RESUMED, "process resumed after it was overdue");
			
			StandardNode standardNode = (StandardNode) transitionOracle.getTo();
			
			Node overdueNode = standardNode.getOverdueNode();
			
			// do we have an explicit overdue edge?
			if (overdueNode != null) {
				String fromState = processItem.getState();
				String toState = (String) overdueNode.getState();
				
				// state change
				processOracle.transitionOracle(fromState, toState).initTransition(processItem);

				log(ProcessLogEvent.OVERDUE_TRANSITION, "transitioned from [" + fromState + "] to [" + toState + "] after overdue");
				
				commitItem();
				
				enqueueProcessContinuation();
				
				return Maybe.complete(Neutral.NEUTRAL);
			}
			else {
				// no overdue edge, thus continue normally (auto resume after overdue)
				doTransitionOrEnd();
			}

		}
		
		return Maybe.complete(Neutral.NEUTRAL);
	}

	private Maybe<Neutral> handleProcess() {
		transitionOracle = processOracle.transitionOracle(processItem.getPreviousState(), processItem.getState(), true);
		
		if (transitionOracle == null) {
			return Reasons.build(UnexpectedProcessState.T).text("Unable to proceed with transitioning as it contradicts the process defintion") //
					.cause(Reasons.build(EdgeNotFound.T).text("Edge from state " + processItem.getPreviousState() + " to state " + processItem.getState() + " not found in process " + processItem).toReason()).toMaybe();
		}
		
		TransitionPhase transitionPhase = processItem.getTransitionPhase();
		
		if (transitionPhase == null) {
			UnexpectedProcessState reason = Reasons.build(UnexpectedProcessState.T).text("Transition phase must not be null").toReason();
			return handleError(reason, ProcessLogEvent.ILLEGAL_TRANSITION_PHASE).asMaybe();
		}

		switch (transitionPhase) {
		case CHANGED_STATE: return handleTransitionProcessing();
		case COMPLETED_PROCESSOR:  return handleTransitionProcessing();
		case COMPLETED_TRANSITION: return continueProcess();
		case DECOUPLED_INTERACTION: return doTransitionOrEnd();
			
		default:
			UnexpectedProcessState reason = Reasons.build(UnexpectedProcessState.T).text("Illegal transition phase: " + transitionPhase).toReason();
			return handleError(reason, ProcessLogEvent.ILLEGAL_TRANSITION_PHASE).asMaybe();
		}
	}

	private Maybe<Neutral> handleTransitionProcessing() {
		Iterator<TransitionProcessor> processors = transitionOracle.getSuccessiveTransitionProcessors(processItem.getTransitionProcessorId());
		
		if (processors.hasNext()) {
			TransitionProcessor processorDeployable = processors.next();
			
			processItem.setTransitionPhase(TransitionPhase.EXECUTING_PROCESSOR);
			processItem.setTransitionProcessorId(processorDeployable.getExternalId());
			
			String processorInfo = buildProcessorId(processorDeployable);
			
			commitItem();
			
			// do actual processor call
			BasicTransitionProcessorContext<ProcessItem> tpContext = new BasicTransitionProcessorContext<>(context().getSystemSession(), processItem);
			
			tribefire.extension.process.api.TransitionProcessor<ProcessItem> processor = processManagerContext.deployedComponentResolver.resolve(processorDeployable.getExternalId(), TransitionProcessor.T);
			
			try {
				TimeSpan duration = runAndKeepProcessAlive(() -> processor.process(tpContext));
				
				PersistenceGmSession session = tpContext.getSession();
				if (session.getTransaction().hasManipulations())
					session.commit();
				
				if (tpContext.getError() != null) {
					return handleError(tpContext.getError(), ProcessLogEvent.ERROR_IN_PROCESSOR, "error while executing transition processor " + processorInfo).asMaybe();
				}
				
				log(ProcessLogEvent.PROCESSOR_EXECUTED, processorInfo + " executed in " + duration.formatWithFloorUnitAndSubUnit(), transitionOracle);
			}
			catch (Exception e) {
				return handleError(e, ProcessLogEvent.ERROR_IN_PROCESSOR, "error while executing transition processor " + processorInfo).asMaybe();
			}
			
			String continueWithState = tpContext.getContinueWithState();

			if (continueWithState != null) {
				log(ProcessLogEvent.NEXT_STATE_SELECTED, String.format("transition processor %s demanded continuation with state [%s]", processorInfo, continueWithState));
				processItem.setNextState(continueWithState);
			}
			
			if (processors.hasNext()) {
				processItem.setTransitionPhase(TransitionPhase.COMPLETED_PROCESSOR);
				commitItem();
				enqueueProcessContinuation();
				return Maybe.complete(Neutral.NEUTRAL);
			}
		}

		processItem.setTransitionPhase(TransitionPhase.COMPLETED_TRANSITION);
		processItem.setTransitionProcessorId(null);
		commitItem();
		return continueProcess();
	}
	
	
	public static class ValueAndDuration<V> {
		public final V value;
		public final TimeSpan duration;
		
		public ValueAndDuration(V value, TimeSpan duration) {
			this.value = value;
			this.duration = duration;
		}
	}
	
	private TimeSpan runAndKeepProcessAlive(Runnable runnable) {
		return callAndKeepProcessAlive(() -> {
			runnable.run();
			return null;
		}).duration;
	}
	
	private <V> ValueAndDuration<V> callAndKeepProcessAlive(Supplier<V> supplier) {
		long start = System.currentTimeMillis();
		
		// start keep alive thread
		TimerTask task = updateLastTransitionTask();
		
		Timer timer = new Timer("HandleProcess-Keep-Process-Alive", true);
		timer.scheduleAtFixedRate(task, 15000, 15000);

		V value = null;
		
		try {
			value = supplier.get();
		}
		finally {
			task.cancel();
			timer.cancel();
		}
		
		TimeSpan duration = TimeSpan.fromMillies(System.currentTimeMillis() - start);
		
		return new ValueAndDuration<>(value, duration);
	}
	
	private TimerTask updateLastTransitionTask() {
		return new TimerTask() {
			@Override
			public void run() {
				updateLastTransitionDate();
			}
		};
	}
	
	private void updateLastTransitionDate() {
		PersistenceGmSession offspringSession = systemSession().newEquivalentSession();
		ProcessItem item = offspringSession.query().entity(processItem).findLocalOrBuildShallow();
		item.setLastTransit(new Date());
		offspringSession.commit();
	}

	private Maybe<Neutral> continueProcess() {
		if (transitionOracle.isRestart())
			return doRestartTransition();
		else
			return continueProcessOrWait();
	}
	
	private Maybe<Neutral> continueProcessOrWait() {
		StandardNode node = (StandardNode) transitionOracle.getTo();
		
		// handle decoupled interaction
		if (node.getDecoupledInteraction() != null) {
			processItem.setActivity(ProcessActivity.waiting);
			processItem.setTransitionPhase(TransitionPhase.DECOUPLED_INTERACTION);
			log(ProcessLogEvent.PROCESS_SUSPENDED, "process suspended");
			commitItem();
			
			notifyProcess(ProcessActivity.waiting);
			
			return Maybe.complete(Neutral.NEUTRAL);
		}
		
		return doTransitionOrEnd();
	}
	
	private Maybe<Neutral> doTransitionOrEnd() {
		if (processOracle.drainNodes.contains(transitionOracle.getTo())) {
			if (processItem.getNextState() != null) {
				UnexpectedProcessState reason = Reasons.build(UnexpectedProcessState.T).text("Cannot continue with next state [" + processItem.getNextState() + "] from terminal node " + processItem.getState()).toReason();
				return handleError(reason, ProcessLogEvent.INVALID_TRANSITION).asMaybe();
			}
			
			processItem.setActivity(ProcessActivity.ended);
			processItem.setEndedAt(new Date());
			
			log(ProcessLogEvent.PROCESS_ENDED, "process ended");
			commitItem();
			
			notifyProcess(ProcessActivity.ended);
			
			return Maybe.complete(Neutral.NEUTRAL);
		}
		
		return doTransition();
	}

	private void notifyProcess(ProcessActivity activity) {
		NotifyProcessActivity request = NotifyProcessActivity.T.create();
		request.setItemId(itemId);
		request.setItemType(itemEntityType.getTypeSignature());
		request.setActivity(activity);
		request.setDomainId(context().getDomainId());
		
		MulticastRequest multicast = MulticastRequest.T.create();
		multicast.setServiceRequest(request);
		
		multicast.eval(processManagerContext.evaluator).get(null);
	}

	private Maybe<Neutral> doTransition() {
		Maybe<String> nextStateMaybe = determineNextState();
		
		if (nextStateMaybe.isUnsatisfied())
			return nextStateMaybe.whyUnsatisfied().asMaybe();
		
		String fromState = processItem.getState();
		String toState = nextStateMaybe.get();
		
		// state change
		processOracle.transitionOracle(fromState, toState).initTransition(processItem);

		log(ProcessLogEvent.STATE_CHANGED, "changed state from [" + fromState + "] to [" + toState + "]");
		
		commitItem();
		
		enqueueProcessContinuation();
		
		return Maybe.complete(Neutral.NEUTRAL);
	}

	private Maybe<String> determineNextState() {
		String nextState = processItem.getNextState();
		
		if (nextState != null) {
			if (!processOracle.hasEdge(processItem.getState(), nextState)) {
				Reason reason = Reasons.build(EdgeNotFound.T).text("Edge from [" + processItem.getState() + "] to [" + nextState + "] not found").toReason();
				return handleError(reason, ProcessLogEvent.INVALID_TRANSITION).asMaybe();
			}
			
			return Maybe.complete(nextState);
		}
		
		StandardNode node = (StandardNode) transitionOracle.getTo();
		
		List<ConditionalEdge> conditionalEdges = node.getConditionalEdges();
		
		if (!conditionalEdges.isEmpty()) {
		
			for (ConditionalEdge conditionalEdge: conditionalEdges) {
				Maybe<Boolean> matchMaybe = evaluateCondition(conditionalEdge);
				
				if (matchMaybe.isUnsatisfied()) {
					return matchMaybe.whyUnsatisfied().asMaybe();
				}
				
				if (matchMaybe.get()) {
					nextState = (String) conditionalEdge.getTo().getState();
					log(ProcessLogEvent.CONDITION_MATCHED, String.format("condition for state [%s] matched", nextState));
					return Maybe.complete(nextState);
				}
			}
			
			UnexpectedProcessState reason = Reasons.build(UnexpectedProcessState.T).text("Could not determine next state from transition processor or conditional edges").toReason();
			return handleError(reason, ProcessLogEvent.UNDETERMINED_NEXT_NODE).asMaybe();
		}
		else {
			List<Edge> list = processOracle.standardEdgesFromState.getOrDefault(node.getState(), Collections.emptyList());
			
			switch (list.size()) {
				case 0: {
					UnexpectedProcessState reason = Reasons.build(UnexpectedProcessState.T) //
							.text("Default routing failed due to missing unconditional edge").toReason();
					return handleError(reason, ProcessLogEvent.UNDETERMINED_NEXT_NODE).asMaybe();
				}
				case 1: {
					Edge edge = list.get(0);
					nextState = (String) edge.getTo().getState();
					return Maybe.complete(nextState);
				}
				default: {
					UnexpectedProcessState reason = Reasons.build(UnexpectedProcessState.T) //
							.text("Default routing ambiguity due to multiple unconditional edges").toReason();
					return handleError(reason, ProcessLogEvent.UNDETERMINED_NEXT_NODE).asMaybe();
				}
			}
		}
		
	}

	private Maybe<Boolean> evaluateCondition(ConditionalEdge conditionalEdge) {
		ConditionProcessor condition = conditionalEdge.getCondition();
		
		if (condition == null) {
			log(ProcessLogEvent.CONDITION_EVALUATED, "default condition evaluated for state [" + conditionalEdge.getTo().getState() + "]", transitionOracle);
			return Maybe.complete(true);
		}
		
		String processorInfo = buildProcessorId(condition);
		
		tribefire.extension.process.api.ConditionProcessor<ProcessItem> processor = processManagerContext.deployedComponentResolver.resolve(condition.getExternalId(), ConditionProcessor.T);
		
		BasicConditionProcessorContext<ProcessItem> context = new BasicConditionProcessorContext<ProcessItem>(systemSession(), processItem);
		
		try {
			ValueAndDuration<Boolean> v = callAndKeepProcessAlive(() -> processor.matches(context));
			TimeSpan duration = v.duration;
			
			if (context.getError() != null) {
				return handleError(context.getError(), ProcessLogEvent.ERROR_IN_CONDITION, "error while checking condition " + processorInfo).asMaybe();
			}
			
			log(ProcessLogEvent.CONDITION_EVALUATED, "called condition processor " + processorInfo + " for state [" + conditionalEdge.getTo().getState() + "] in " + duration.formatWithFloorUnitAndSubUnit(), transitionOracle);
			
			return Maybe.complete(v.value);
		}
		catch (Exception e) {
			return handleError(e, ProcessLogEvent.ERROR_IN_CONDITION, "error while checking condition " + processorInfo).asMaybe();
		}
	}

	private Maybe<Neutral> doRestartTransition() {
		RestartNode restartNode = (RestartNode) transitionOracle.getTo();
		
		Edge restartEdge = restartNode.getRestartEdge();

		final String fromState;
		final String toState;
		
		if (restartEdge != null) {
			// explicit restart from restart edge
			StandardNode restartFrom = restartEdge.getFrom();
			Node restartTo = restartEdge.getTo();
			
			fromState = (String)restartFrom.getState();
			toState = (String)restartTo.getState();
		}
		else {
			// auto restart from last edge
			fromState = processItem.getPreviousState();
			toState = processItem.getState();
		}
		
		// TODO: talk about restart counter which was dropped and also about RestartNode in general
		
		
		// transition phase reinit
		processOracle.transitionOracle(fromState, toState).initTransition(processItem);
		
		log(ProcessLogEvent.RESTART_TRANSITION, "restarted from [" + fromState + "] to [" + toState + "]");
		
		commitItem();
		
		enqueueProcessContinuation();
		
		return Maybe.complete(Neutral.NEUTRAL);
	}
	
	private Reason handleError(Exception e, ProcessLogEvent event, String msg) {
		String tracebackId = UUID.randomUUID().toString();
		logger.error("Error while processing " + processItem + " (tracebackId=" + tracebackId + ")", e);
		// TODO: include stack trace somehow in a nice way
		return handleError(Reasons.build(InternalError.T).text("Error while processing. See log with tracebackId=" + tracebackId).toReason(), event, msg);
	}
	
	private Reason handleError(Reason error, ProcessLogEvent event) {
		// TODO: separate short msg from details (trim in any case to the MaxLength)
		handleError(event, error.stringify(), "");
		return error;
	}
	
	private Reason handleError(Reason error, ProcessLogEvent event, String msg) {
		// TODO: separate short msg from details (trim in any case to the MaxLength)
		handleError(event, msg + ": " + error.stringify(), "");
		return error;
	}
	
	private void handleError(ProcessLogEvent event, String msg, String details) {
		log(event, msg);
		
		// halt process
		processItem.setActivity(ProcessActivity.halted);
		log(ProcessLogEvent.PROCESS_HALTED, "Process halted after an error. Take special care and continue with RecoverProcess.");
		commitItem();
		
		// notify error handlers
		notifyError();
		
		notifyProcess(ProcessActivity.halted);
	}

	private void notifyError() {
		
		List<TransitionProcessor> errorHandlers = transitionOracle.getErrorHandlers();
		
		for (TransitionProcessor processorDeployable: errorHandlers) {
			// do actual processor call
			BasicTransitionProcessorContext<ProcessItem> tpContext = new BasicTransitionProcessorContext<>(context().getSystemSession(), processItem);
			
			tribefire.extension.process.api.TransitionProcessor<ProcessItem> processor = processManagerContext.deployedComponentResolver.resolve(processorDeployable.getExternalId(), TransitionProcessor.T);
			String processorInfo = buildProcessorId(processorDeployable);
			
			try {
				processor.process(tpContext);
				
				PersistenceGmSession session = tpContext.getSession();
				if (session.getTransaction().hasManipulations())
					session.commit();
				
				log(ProcessLogEvent.PROCESSOR_EXECUTED, processorInfo + " executed as error handler", transitionOracle);
			}
			catch (Exception e) {
				logger.error("error while executing transition processor " + processorInfo, e);
			}
		}
	}

	private static String buildProcessorId(Deployable processor) {
		return processor.entityType().getShortName() + "(" + processor.getExternalId() + ")";
	}

}
