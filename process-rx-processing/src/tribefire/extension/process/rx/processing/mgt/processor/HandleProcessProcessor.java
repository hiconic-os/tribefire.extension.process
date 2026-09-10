// ============================================================================
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ============================================================================
package tribefire.extension.process.rx.processing.mgt.processor;

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
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.service.api.MulticastRequest;
import com.braintribe.model.service.api.result.Neutral;
import com.braintribe.model.time.TimeSpan;

import tribefire.extension.process.api.ConditionProcessor;
import tribefire.extension.process.api.TransitionProcessor;
import tribefire.extension.process.api.model.ctrl.HandleProcess;
import tribefire.extension.process.api.model.ctrl.NotifyProcessActivity;
import tribefire.extension.process.data.model.ProcessItem;
import tribefire.extension.process.data.model.log.ProcessLogEvent;
import tribefire.extension.process.data.model.state.ProcessActivity;
import tribefire.extension.process.data.model.state.TransitionPhase;
import tribefire.extension.process.model.configuration.ConditionProcessorReference;
import tribefire.extension.process.model.configuration.Edge;
import tribefire.extension.process.model.configuration.Node;
import tribefire.extension.process.model.configuration.TransitionProcessorReference;
import tribefire.extension.process.reason.model.EdgeNotFound;
import tribefire.extension.process.reason.model.UnexpectedProcessActivity;
import tribefire.extension.process.reason.model.UnexpectedProcessState;
import tribefire.extension.process.rx.processing.mgt.BasicConditionProcessorContext;
import tribefire.extension.process.rx.processing.mgt.BasicTransitionProcessorContext;
import tribefire.extension.process.rx.processing.oracle.TransitionOracle;

public class HandleProcessProcessor extends OracledProcessRequestProcessor<HandleProcess, Neutral> {

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
		} else {
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

			Node overdueNode = transitionOracle.getTo().getOverdueNode();

			// do we have an explicit overdue edge?
			if (overdueNode != null) {
				String fromState = processItem.getState();
				String toState = overdueNode.getState();

				// state change
				processOracle.transitionOracle(fromState, toState).initTransition(processItem);

				log(ProcessLogEvent.OVERDUE_TRANSITION, "transitioned from [" + fromState + "] to [" + toState + "] after overdue");

				commitItem();

				enqueueProcessContinuation();

				return Maybe.complete(Neutral.NEUTRAL);
			} else {
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
					.cause(Reasons.build(EdgeNotFound.T).text("Edge from state " + processItem.getPreviousState() + " to state "
							+ processItem.getState() + " not found in process " + processItem).toReason())
					.toMaybe();
		}

		TransitionPhase transitionPhase = processItem.getTransitionPhase();

		if (transitionPhase == null) {
			UnexpectedProcessState reason = Reasons.build(UnexpectedProcessState.T).text("Transition phase must not be null").toReason();
			return handleError(reason, ProcessLogEvent.ILLEGAL_TRANSITION_PHASE);
		}

		switch (transitionPhase) {
			case CHANGED_STATE:
				return handleTransitionProcessing();
			case COMPLETED_PROCESSOR:
				return handleTransitionProcessing();
			case COMPLETED_TRANSITION:
				return continueProcess();
			case DECOUPLED_INTERACTION:
				return doTransitionOrEnd();

			default:
				UnexpectedProcessState reason = Reasons.build(UnexpectedProcessState.T) //
						.text("Illegal transition phase: " + transitionPhase) //
						.toReason();
				return handleError(reason, ProcessLogEvent.ILLEGAL_TRANSITION_PHASE);
		}
	}

	private Maybe<Neutral> handleTransitionProcessing() {
		Iterator<TransitionProcessorReference> processors = transitionOracle
				.getSuccessiveTransitionProcessors(processItem.getTransitionProcessorId());

		if (processors.hasNext()) {
			TransitionProcessorReference processorReference = processors.next();

			processItem.setTransitionPhase(TransitionPhase.EXECUTING_PROCESSOR);
			processItem.setTransitionProcessorId(processorReference.getProcessorId());

			String processorInfo = buildProcessorId(processorReference);

			commitItem();

			// do actual processor call
			BasicTransitionProcessorContext<ProcessItem> tpContext = new BasicTransitionProcessorContext<>(context().getSystemSession(), processItem);

			TransitionProcessor<ProcessItem> processor = (TransitionProcessor<ProcessItem>) processManagerContext.processExpertResolver
					.resolveTransitionProcessor(processorReference.getProcessorId());

			try {
				TimeSpan duration = runAndKeepProcessAlive(() -> processor.process(tpContext));

				PersistenceGmSession session = tpContext.getSession();
				if (session.getTransaction().hasManipulations())
					session.commit();

				if (tpContext.getError() != null) {
					return handleError(tpContext.getError(), ProcessLogEvent.ERROR_IN_PROCESSOR,
							"error while executing transition processor " + processorInfo);
				}

				log(ProcessLogEvent.PROCESSOR_EXECUTED, processorInfo + " executed in " + duration.formatWithFloorUnitAndSubUnit(), transitionOracle);
			} catch (Exception e) {
				return handleError(e, ProcessLogEvent.ERROR_IN_PROCESSOR, "error while executing transition processor " + processorInfo);
			}

			String continueWithState = tpContext.getContinueWithState();

			if (continueWithState != null) {
				log(ProcessLogEvent.NEXT_STATE_SELECTED,
						String.format("transition processor %s demanded continuation with state [%s]", processorInfo, continueWithState));
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
		TimerTask task = new UpdateLastTransitionTask();

		Timer timer = new Timer("HandleProcess-Keep-Process-Alive", true);
		timer.scheduleAtFixedRate(task, 15000, 15000);

		V value = null;

		try {
			value = supplier.get();
		} finally {
			task.cancel();
			timer.cancel();
		}

		TimeSpan duration = TimeSpan.fromMillies(System.currentTimeMillis() - start);

		return new ValueAndDuration<>(value, duration);
	}

	private class UpdateLastTransitionTask extends TimerTask {
		@Override
		public void run() {
			updateLastTransitionDate();
		}
	}

	private void updateLastTransitionDate() {
		PersistenceGmSession offspringSession = systemSession().newEquivalentSession();
		ProcessItem item = offspringSession.query().entity(processItem).findLocalOrBuildShallow();
		item.setLastTransit(new Date());
		offspringSession.commit();
	}

	private Maybe<Neutral> continueProcess() {
		return continueProcessOrWait();
	}

	private Maybe<Neutral> continueProcessOrWait() {
		Node node = transitionOracle.getTo();

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
				UnexpectedProcessState reason = Reasons.build(UnexpectedProcessState.T) //
						.text("Cannot continue with next state [" + processItem.getNextState() + "] from terminal node " + processItem.getState()) //
						.toReason();
				return handleError(reason, ProcessLogEvent.INVALID_TRANSITION);
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
				Reason reason = Reasons.build(EdgeNotFound.T) //
						.text("Edge from [" + processItem.getState() + "] to [" + nextState + "] not found") //
						.toReason();
				return handleError(reason, ProcessLogEvent.INVALID_TRANSITION);
			}

			return Maybe.complete(nextState);
		}

		Node node = transitionOracle.getTo();
		List<Edge> outgoingEdges = processOracle.outgoingEdges(node.getState());

		for (Edge edge : outgoingEdges) {
			if (edge.getCondition() != null) {
				Maybe<Boolean> matchMaybe = evaluateCondition(edge);

				if (matchMaybe.isUnsatisfied()) {
					return matchMaybe.whyUnsatisfied().asMaybe();
				}

				if (matchMaybe.get()) {
					nextState = edge.getTo().getState();
					log(ProcessLogEvent.CONDITION_MATCHED, String.format("condition for state [%s] matched", nextState));
					return Maybe.complete(nextState);
				}
			}
		}

		List<Edge> list = outgoingEdges.stream().filter(edge -> edge.getCondition() == null).toList();

		switch (list.size()) {
			case 0: {
				UnexpectedProcessState reason = Reasons.build(UnexpectedProcessState.T) //
						.text("Default routing failed due to missing unconditional edge").toReason();
				return handleError(reason, ProcessLogEvent.UNDETERMINED_NEXT_NODE);
			}
			case 1: {
				Edge edge = list.get(0);
				nextState = edge.getTo().getState();
				return Maybe.complete(nextState);
			}
			default: {
				UnexpectedProcessState reason = Reasons.build(UnexpectedProcessState.T) //
						.text("Default routing failed due to ambiguity of multiple unconditional edges").toReason();
				return handleError(reason, ProcessLogEvent.UNDETERMINED_NEXT_NODE);
			}
		}
	}

	private Maybe<Boolean> evaluateCondition(Edge edge) {
		ConditionProcessorReference condition = edge.getCondition();

		if (condition == null) {
			log(ProcessLogEvent.CONDITION_EVALUATED, "default condition evaluated for state [" + edge.getTo().getState() + "]", transitionOracle);
			return Maybe.complete(true);
		}

		String processorInfo = buildProcessorId(condition);

		ConditionProcessor<ProcessItem> processor = (ConditionProcessor<ProcessItem>) processManagerContext.processExpertResolver
				.resolveConditionProcessor(condition.getProcessorId());

		BasicConditionProcessorContext<ProcessItem> context = new BasicConditionProcessorContext<>(systemSession(), processItem);

		try {
			ValueAndDuration<Boolean> v = callAndKeepProcessAlive(() -> processor.matches(context));
			TimeSpan duration = v.duration;

			if (context.getError() != null) {
				return handleError(context.getError(), ProcessLogEvent.ERROR_IN_CONDITION, "error while checking condition " + processorInfo);
			}

			log(ProcessLogEvent.CONDITION_EVALUATED, "called condition processor " + processorInfo + " for state [" + edge.getTo().getState()
					+ "] in " + duration.formatWithFloorUnitAndSubUnit(), transitionOracle);

			return Maybe.complete(v.value);
		} catch (Exception e) {
			return handleError(e, ProcessLogEvent.ERROR_IN_CONDITION, "error while checking condition " + processorInfo);
		}
	}

	private <T> Maybe<T> handleError(Exception e, ProcessLogEvent event, String msg) {
		String tracebackId = UUID.randomUUID().toString();
		logger.error("Error while processing " + processItem + " (tracebackId=" + tracebackId + ")", e);

		InternalError reason = Reasons.build(InternalError.T) //
				.text("Error while processing. See log with tracebackId=" + tracebackId) //
				.assign(InternalError::setJavaException, e) //
				.toReason();

		return handleError(reason, event, msg);
	}

	private <T> Maybe<T> handleError(Reason error, ProcessLogEvent event) {
		// TODO: separate short msg from details (trim in any case to the MaxLength)
		actuallyHandleError(event, error.stringify());
		return error.asMaybe();
	}

	private <T> Maybe<T> handleError(Reason error, ProcessLogEvent event, String msg) {
		// TODO: separate short msg from details (trim in any case to the MaxLength)
		actuallyHandleError(event, msg + ": " + error.stringify());
		return error.asMaybe();
	}

	private void actuallyHandleError(ProcessLogEvent event, String msg) {
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

		List<TransitionProcessorReference> errorHandlers = transitionOracle.getErrorHandlers();

		for (TransitionProcessorReference processorReference : errorHandlers) {
			// do actual processor call
			BasicTransitionProcessorContext<ProcessItem> tpContext = new BasicTransitionProcessorContext<>(systemSession(), processItem);

			TransitionProcessor<ProcessItem> processor = (TransitionProcessor<ProcessItem>) processManagerContext.processExpertResolver
					.resolveTransitionProcessor(processorReference.getProcessorId());
			String processorInfo = buildProcessorId(processorReference);

			try {
				processor.process(tpContext);

				log(ProcessLogEvent.PROCESSOR_EXECUTED, processorInfo + " executed as error handler", transitionOracle);

				systemSession().commit();
			} catch (Exception e) {
				logger.error("error while executing transition processor " + processorInfo, e);
			}
		}
	}

	private static String buildProcessorId(TransitionProcessorReference processor) {
		return processor.getProcessorId();
	}

	private static String buildProcessorId(ConditionProcessorReference processor) {
		return processor.getProcessorId();
	}

}
