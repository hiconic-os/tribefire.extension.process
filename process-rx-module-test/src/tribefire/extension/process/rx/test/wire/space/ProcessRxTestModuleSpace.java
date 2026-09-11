package tribefire.extension.process.rx.test.wire.space;

import com.braintribe.model.time.TimeSpan;
import com.braintribe.model.time.TimeUnit;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import hiconic.rx.access.module.api.AccessContract;
import hiconic.rx.access.module.api.AccessDataModelConfiguration;
import hiconic.rx.access.module.api.AccessServiceModelConfiguration;
import hiconic.rx.module.api.service.ModelConfigurations;
import hiconic.rx.module.api.wire.RxModuleContract;
import tribefire.extension.process.model.configuration.Condition;
import tribefire.extension.process.model.configuration.ProcessDefinition;
import tribefire.extension.process.model.configuration.ProcessDefinitionsConfiguration;
import tribefire.extension.process.rx.api.ProcessDefinitionEditor;
import tribefire.extension.process.rx.api.ProcessModelSymbols;
import tribefire.extension.process.rx.api.ProcessRxContract;
import tribefire.extension.process.rx.test.expert.ApprovedCondition;
import tribefire.extension.process.rx.test.expert.DemandStateAction;
import tribefire.extension.process.rx.test.expert.FailingAction;
import tribefire.extension.process.rx.test.expert.TraceAction;
import tribefire.extension.process.rx.test.model.FailingTestProcess;
import tribefire.extension.process.rx.test.model.RoutingTestProcess;
import tribefire.extension.process.rx.test.model.WaitingTestProcess;

/**
 * The application under test: one in-memory access, three process definitions, and the processors they reference.
 * <p>
 * The graphs are built here rather than in a configuration file, so that a test names a state, an edge or a processor by
 * the same constant the graph uses.
 */
@Managed
public class ProcessRxTestModuleSpace implements RxModuleContract {

	public static final String ACCESS_ID = "test.processes";

	// routing definition
	public static final String STATE_REVIEW = "review";
	public static final String STATE_APPROVED = "approved";
	public static final String STATE_REJECTED = "rejected";
	public static final String STATE_MANUAL = "manual";

	// waiting definition
	public static final String STATE_WAIT = "wait";
	public static final String STATE_WAIT_OVERDUE = "wait-overdue";
	public static final String STATE_ESCALATED = "escalated";
	public static final String STATE_WAIT_AUTO = "wait-auto";
	public static final String STATE_WAIT_DONE = "wait-done";

	// failing definition
	public static final String STATE_WORK = "work";
	public static final String STATE_WORK_DONE = "work-done";

	// processor ids
	public static final String CONDITION_APPROVED = "test.condition.approved";
	public static final String ACTION_DEMAND_STATE = "test.action.demand-state";
	public static final String ACTION_FAIL = "test.action.fail";
	public static final String ACTION_TRACE_DEFINITION = "test.action.trace.definition-transit";
	public static final String ACTION_TRACE_LEFT = "test.action.trace.left";
	public static final String ACTION_TRACE_EDGE = "test.action.trace.edge-transit";
	public static final String ACTION_TRACE_ENTERED = "test.action.trace.entered";
	public static final String ACTION_TRACE_ERROR = "test.action.trace.error";

	@Import
	private AccessContract access;

	@Import
	private ProcessRxContract process;

	@Override
	public void configureModels(ModelConfigurations configurations) {
		AccessDataModelConfiguration dataModel = access.accessModelConfigurations().dataModelConfiguration(ACCESS_ID);
		dataModel.addModel(ProcessModelSymbols.configuredProcessDataModel);
		dataModel.addModelByName("tribefire.extension.process:process-rx-test-model");

		AccessServiceModelConfiguration serviceModel = access.accessModelConfigurations().serviceModelConfiguration(ACCESS_ID);
		serviceModel.addModel(ProcessModelSymbols.configuredProcessApiModel);

		ProcessDefinitionsConfiguration definitions = process.definitions();
		definitions.getMonitoredAccessIds().add(ACCESS_ID);
		definitions.getDefinitions().add(routingDefinition());
		definitions.getDefinitions().add(waitingDefinition());
		definitions.getDefinitions().add(failingDefinition());
	}

	@Override
	public void onDeploy() {
		process.registerConditionProcessor(CONDITION_APPROVED, ApprovedCondition::new);
		process.registerTransitionProcessor(ACTION_DEMAND_STATE, DemandStateAction::new);
		process.registerTransitionProcessor(ACTION_FAIL, FailingAction::new);
		process.registerTransitionProcessor(ACTION_TRACE_DEFINITION, () -> new TraceAction("definition-transit"));
		process.registerTransitionProcessor(ACTION_TRACE_LEFT, () -> new TraceAction("left"));
		process.registerTransitionProcessor(ACTION_TRACE_EDGE, () -> new TraceAction("edge-transit"));
		process.registerTransitionProcessor(ACTION_TRACE_ENTERED, () -> new TraceAction("entered"));
		process.registerTransitionProcessor(ACTION_TRACE_ERROR, () -> new TraceAction("error"));
	}

	/**
	 * root to review, and from review either to approved, when the condition processor matches, or to rejected by default.
	 * The edge to manual is disabled by expression, so only a transition processor can take it.
	 * <p>
	 * The four processor lists of the transition into approved each record their own entry in the trace, which is how the
	 * test sees their order.
	 */
	private ProcessDefinition routingDefinition() {
		ProcessDefinitionEditor editor = ProcessDefinitionEditor.create(RoutingTestProcess.DEFINITION_ID, "Routing Test Process");

		editor.rootEdge(STATE_REVIEW, "root-review");
		editor.conditionedEdge(STATE_REVIEW, STATE_APPROVED, "review-approved", CONDITION_APPROVED);
		editor.conditionedEdge(STATE_REVIEW, STATE_MANUAL, "review-manual", Condition.expression(false));
		editor.edge(STATE_REVIEW, STATE_REJECTED, "review-rejected");

		editor.definition().getOnTransit().add(ProcessDefinitionEditor.transitionProcessor(ACTION_TRACE_DEFINITION));
		editor.onEnter(STATE_REVIEW, ACTION_DEMAND_STATE);
		editor.onLeave(STATE_REVIEW, ACTION_TRACE_LEFT);
		editor.onTransit("review-approved", ACTION_TRACE_EDGE);
		editor.onEnter(STATE_APPROVED, ACTION_TRACE_ENTERED);

		return editor.definition();
	}

	/**
	 * root to wait, which waits for a decoupled interaction. Resuming leads to wait-overdue, which waits with a deadline of
	 * one second and escalates once that passed. The state wait-auto waits with the same deadline but without an overdue
	 * node, so it continues by normal routing instead.
	 */
	private ProcessDefinition waitingDefinition() {
		ProcessDefinitionEditor editor = ProcessDefinitionEditor.create(WaitingTestProcess.DEFINITION_ID, "Waiting Test Process");

		editor.rootEdge(STATE_WAIT, "root-wait");
		editor.edge(STATE_WAIT, STATE_WAIT_OVERDUE, "wait-overdue-edge");
		editor.conditionedEdge(STATE_WAIT, STATE_WAIT_AUTO, "wait-auto-edge", Condition.expression(false));
		editor.conditionedEdge(STATE_WAIT, STATE_WAIT_DONE, "wait-done-edge", Condition.expression(false));
		editor.edge(STATE_WAIT_OVERDUE, STATE_WAIT_DONE, "overdue-done-edge");
		editor.conditionedEdge(STATE_WAIT_OVERDUE, STATE_ESCALATED, "overdue-escalated-edge", Condition.expression(false));
		editor.edge(STATE_WAIT_AUTO, STATE_WAIT_DONE, "auto-done-edge");

		editor.decoupledInteraction(STATE_WAIT, "Waits to be resumed");

		// waits with a deadline, and escalates over its overdue node once the deadline passed
		editor.decoupledInteraction(STATE_WAIT_OVERDUE, "Waits until it is overdue");
		editor.gracePeriod(STATE_WAIT_OVERDUE, TimeSpan.create(1, TimeUnit.second));
		editor.overdueNode(STATE_WAIT_OVERDUE, STATE_ESCALATED);

		// waits with a deadline as well, but has no overdue node, so it continues by normal routing
		editor.decoupledInteraction(STATE_WAIT_AUTO, "Waits until it is overdue, then continues");
		editor.gracePeriod(STATE_WAIT_AUTO, TimeSpan.create(1, TimeUnit.second));

		return editor.definition();
	}

	/** root to work, where a processor fails as the process asks for, and on to work-done once it succeeds. */
	private ProcessDefinition failingDefinition() {
		ProcessDefinitionEditor editor = ProcessDefinitionEditor.create(FailingTestProcess.DEFINITION_ID, "Failing Test Process");

		editor.rootEdge(STATE_WORK, "root-work");
		editor.edge(STATE_WORK, STATE_WORK_DONE, "work-done-edge");

		editor.onEnter(STATE_WORK, ACTION_FAIL);
		editor.definition().getOnError().add(ProcessDefinitionEditor.transitionProcessor(ACTION_TRACE_ERROR));

		return editor.definition();
	}
}
