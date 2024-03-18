package tribefire.extension.process.processing.mgt.processor;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.InvalidArgument;

import tribefire.extension.process.api.model.analysis.GetProcessLog;
import tribefire.extension.process.api.model.analysis.GetProcessLogByScalarFilters;
import tribefire.extension.process.api.model.data.ProcessLog;
import tribefire.extension.process.data.model.ProcessItem;
import tribefire.extension.process.data.model.log.ProcessLogEntry;
import tribefire.extension.process.data.model.log.ProcessLogEvent;
import tribefire.extension.process.data.model.log.ProcessLogLevel;

public class GetProcessLogByScalarFiltersProcessor extends ProcessRequestProcessor<GetProcessLogByScalarFilters, List<ProcessLogEntry>> {

	private final static Set<ProcessLogEvent> EVENTS_TRANSITION = EnumSet.of(//
			ProcessLogEvent.STATE_CHANGED, //
			ProcessLogEvent.OVERDUE_TRANSITION, //
			ProcessLogEvent.RESTART_TRANSITION, //
			ProcessLogEvent.ERROR_TRANSITION, //
			ProcessLogEvent.INVALID_TRANSITION //
	);

	private final static Set<ProcessLogEvent> EVENTS_CONDITION = EnumSet.of(//
			ProcessLogEvent.CONDITION_MATCHED //
	);

	private final static Set<ProcessLogEvent> EVENTS_TRANSITION_PROCESSOR = EnumSet.of(//
			ProcessLogEvent.PROCESSOR_EXECUTED, //
			ProcessLogEvent.ERROR_IN_PROCESSOR //
	);

	private final static Set<ProcessLogEvent> EVENTS_CONDITION_PROCESSOR = EnumSet.of(//
			ProcessLogEvent.CONDITION_EVALUATED, //
			ProcessLogEvent.ERROR_IN_CONDITION //
	);

	private final static Set<ProcessLogEvent> EVENTS_CONTROL = EnumSet.of(//
			ProcessLogEvent.PROCESS_STARTED, //
			ProcessLogEvent.PROCESS_ENDED, //
			ProcessLogEvent.PROCESS_SUSPENDED, //
			ProcessLogEvent.PROCESS_RESUMED, //
			ProcessLogEvent.PROCESS_RECOVERED, //
			ProcessLogEvent.PROCESS_HALTED, //
			ProcessLogEvent.PROCESS_IS_OVERDUE, //
			ProcessLogEvent.NEXT_STATE_SELECTED //
	);

	@Override
	protected Maybe<List<ProcessLogEntry>> processValidatedRequest() {

		GetProcessLog getProcessLog = GetProcessLog.T.create();
		getProcessLog.setItemId(request.getItemId());
		getProcessLog.setItemType(request.getItemType());
		getProcessLog.setPageLimit(request.getPageLimit());
		getProcessLog.setPageOffset(request.getPageOffset());
		ProcessLog processLog = getProcessLog.eval(context().getSession()).get();
		List<ProcessLogEntry> traces = processLog.getEntries();
		
		Predicate<ProcessLogEntry> filter = eventFilter(request).and(stateFilter(request));

		List<ProcessLogEntry> filteredTraces = traces.stream() //
				.filter(filter) //
				.sorted(Comparator.comparing(ProcessLogEntry::getDate)) //
				.collect(Collectors.toList());

		return Maybe.complete(filteredTraces);
	}

	private Predicate<ProcessLogEntry> stateFilter(GetProcessLogByScalarFilters request) {
		Predicate<ProcessLogEntry> filter = new EmptyJunctionPredicate<>(true);

		filter = extendStateFilter(filter, request, GetProcessLogByScalarFilters::getState, ProcessLogEntry::getState);

		return filter;
	}

	private Predicate<ProcessLogEntry> extendStateFilter(Predicate<ProcessLogEntry> filter, GetProcessLogByScalarFilters request,
			Function<GetProcessLogByScalarFilters, String> requestStateExtractor, Function<ProcessLogEntry, String> traceStateExtractor) {
		String requestedState = requestStateExtractor.apply(request);
		if (requestedState != null) {
			String fromState = (requestedState.isEmpty() && requestedState.equals("null")) ? null : requestedState;

			return filter.and(t -> equals(traceStateExtractor.apply(t), fromState));
		} else
			return filter;
	}

	private static boolean equals(Object o1, Object o2) {
		if (o1 == o2)
			return true;

		if (o1 == null)
			return false;

		if (o2 == null)
			return false;

		return o1.equals(o2);
	}

	private static class EmptyJunctionPredicate<T> implements Predicate<T> {
		private boolean value;

		public EmptyJunctionPredicate(boolean value) {
			super();
			this.value = value;
		}

		@Override
		public boolean test(T t) {
			return value;
		}

		@Override
		public Predicate<T> or(Predicate<? super T> other) {
			return (Predicate<T>) other;
		}

		@Override
		public Predicate<T> and(Predicate<? super T> other) {
			return (Predicate<T>) other;
		}
	}

	private Predicate<ProcessLogEntry> eventFilter(GetProcessLogByScalarFilters request) {
		Predicate<ProcessLogEntry> f = new EmptyJunctionPredicate<>(true);

		if (request.getIncludeTransitionEvents())
			f = f.or(e -> EVENTS_TRANSITION.contains(e.getEvent()));

		if (request.getIncludeConditionEvents())
			f = f.or(e -> EVENTS_CONDITION.contains(e.getEvent()));

		if (request.getIncludeTransitionProcessorEvents())
			f = f.or(e -> EVENTS_TRANSITION_PROCESSOR.contains(e.getEvent()));

		if (request.getIncludeConditionProcessorEvents())
			f = f.or(e -> EVENTS_CONDITION_PROCESSOR.contains(e.getEvent()));

		if (request.getIncludeControlEvents())
			f = f.or(e -> EVENTS_CONTROL.contains(e.getEvent()));

		if (request.getIncludeProblemEvents())
			f = f.or(e -> e.getLevel() == ProcessLogLevel.ERROR);

		if (request.getIncludeEvents() != null) {
			String pattern = request.getIncludeEvents();
			Pattern compiledPattern = Pattern.compile(pattern);

			f = f.or(e -> compiledPattern.matcher(e.getEvent().name()).matches());
		}
		
		return f;
	}

}