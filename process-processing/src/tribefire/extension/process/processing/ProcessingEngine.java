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
package tribefire.extension.process.processing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.common.lcd.Pair;
import com.braintribe.logging.Logger;
import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.cortex.aspect.StateProcessingAspect;
import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.deployment.DeploymentStatus;
import com.braintribe.model.extensiondeployment.AccessAspect;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.EntityProperty;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.pr.criteria.matching.StandardMatcher;
import com.braintribe.model.generic.processing.pr.fluent.TC;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.SimpleType;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.generic.reflection.StrategyOnCriterionMatch;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.processing.deployment.api.DeployedComponentResolver;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.processing.sp.api.AfterStateChangeContext;
import com.braintribe.model.processing.sp.api.BeforeStateChangeContext;
import com.braintribe.model.processing.sp.api.PrioritizedStateChangeProcessorMatch;
import com.braintribe.model.processing.sp.api.ProcessStateChangeContext;
import com.braintribe.model.processing.sp.api.StateChangeContext;
import com.braintribe.model.processing.sp.api.StateChangeProcessor;
import com.braintribe.model.processing.sp.api.StateChangeProcessorAdapter;
import com.braintribe.model.processing.sp.api.StateChangeProcessorException;
import com.braintribe.model.processing.sp.api.StateChangeProcessorMatch;
import com.braintribe.model.processing.sp.api.StateChangeProcessorRule;
import com.braintribe.model.processing.sp.api.StateChangeProcessorSelectorContext;
import com.braintribe.model.processing.sp.api.StateChangeProcessors;
import com.braintribe.model.processing.time.TimeSpanConversion;
import com.braintribe.model.processing.worker.api.Worker;
import com.braintribe.model.processing.worker.api.WorkerContext;
import com.braintribe.model.processing.worker.api.WorkerException;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.OrderingDirection;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.record.ListRecord;
import com.braintribe.model.stateprocessing.api.StateChangeProcessorCapabilities;
import com.braintribe.model.time.TimeSpan;
import com.braintribe.model.time.TimeUnit;
import com.braintribe.utils.lcd.NullSafe;

import tribefire.extension.process.api.ConditionProcessor;
import tribefire.extension.process.api.ProcessTracer;
import tribefire.extension.process.api.TraceBuilder;
import tribefire.extension.process.api.TraceContext;
import tribefire.extension.process.model.ActivatorCallContext;
import tribefire.extension.process.model.ContinueWithState;
import tribefire.extension.process.model.MoverCallContext;
import tribefire.extension.process.model.MoverValidationContext;
import tribefire.extension.process.model.RestarterCallContext;
import tribefire.extension.process.model.TransitionProcessorCallContext;
import tribefire.extension.process.model.data.HasExecutionPriority;
import tribefire.extension.process.model.data.Process;
import tribefire.extension.process.model.data.ProcessActivity;
import tribefire.extension.process.model.data.RestartCounter;
import tribefire.extension.process.model.data.details.AmbiguousContinuationDemand;
import tribefire.extension.process.model.data.details.ContinuationDemand;
import tribefire.extension.process.model.data.details.DistinctContinuationDemand;
import tribefire.extension.process.model.data.details.HasDeployableInfo;
import tribefire.extension.process.model.data.details.ProcessorDetails;
import tribefire.extension.process.model.data.details.ProcessorKind;
import tribefire.extension.process.model.data.details.ProcessorPostCall;
import tribefire.extension.process.model.data.details.ProcessorPreCall;
import tribefire.extension.process.model.data.tracing.ProcessTrace;
import tribefire.extension.process.model.data.tracing.TraceKind;
import tribefire.extension.process.model.deployment.ConditionalEdge;
import tribefire.extension.process.model.deployment.Edge;
import tribefire.extension.process.model.deployment.HasErrorNode;
import tribefire.extension.process.model.deployment.Node;
import tribefire.extension.process.model.deployment.ProcessDefinition;
import tribefire.extension.process.model.deployment.ProcessElement;
import tribefire.extension.process.model.deployment.RestartNode;
import tribefire.extension.process.model.deployment.StandardNode;
import tribefire.extension.process.model.deployment.TransitionProcessor;
import tribefire.extension.process.processing.condition.BasicConditionProcessorContext;


/**
 * a specific processor that manages transition handlers from the process model
 * <br/<
 * implements :<br/>
 * {@link StateChangeProcessor} as it is a processor itself <br/>
 * {@link StateChangeProcessorRule} as it is a rule to be include in the rule set<br/> 
 * <br/>
 * notes:<br/>
 * the applicable handlers are extracted at the onBefore call, event hough that only a projected new value exists and 
 * not the real value (which obviously only exists after the application, i.e. at the onAfter call. Yet, if we are to do 
 * a sanity check - vetoing the application if no applicable handlers exist - we need to do it here, as only the onBefore
 * call can veto the changes.<br/>
 * 
 * @author pit, dirk
 *
 */
public class ProcessingEngine implements StateChangeProcessorRule, Worker, ProcessingEngineConstants {
	private static final Logger logger = Logger.getLogger(ProcessingEngine.class);
	private tribefire.extension.process.model.deployment.ProcessingEngine deployedProcessingEngine;
	
	private static class ProcessDefinitionInfo {
		public ProcessDefinition processDefinition;
		public Map<Object, Node> nodeByState = new HashMap<Object, Node>();
		public Set<StandardNode> fromNodes = new HashSet<StandardNode>();
		public Map<Pair<Object,Object>, Edge> edgesByStateChange = new HashMap<Pair<Object,Object>, Edge>();
		//public MultiMap<Object, ConditionalEdge> conditionalEdgesByLeftState = new ComparatorBasedNavigableMultiMap<Object, ConditionalEdge>(StateComparator.instance, EdgeComparator.instance);
		public Set<StandardNode> drainNodes = new HashSet<StandardNode>();
	}
	
	private Map<ProcessDefinition, ProcessDefinitionInfo> definitionInfos = new HashMap<ProcessDefinition, ProcessDefinitionInfo>();
	private Map<String, ProcessDefinitionInfo> definitionInfoByName = new HashMap<String, ProcessDefinitionInfo>();
	private ProcessTracer tracer;
	private Set<String> priviledgedRoles = new HashSet<String>(Collections.singleton("tf-internal"));
	private PersistenceGmSessionFactory sessionFactory;
	private Supplier<PersistenceGmSession> cortexSessionProvider;
	private Future<?> monitorFuture;
	private DeployedComponentResolver deployedComponentResolver;
	
	@Required @Configurable
	public void setDeployedComponentResolver(DeployedComponentResolver deployedComponentResolver) {
		this.deployedComponentResolver = deployedComponentResolver;
	}
	
	@Required @Configurable
	public void setCortexSessionSupplier(Supplier<PersistenceGmSession> cortexSessionProvider) {
		this.cortexSessionProvider = cortexSessionProvider;
	}
	
	@Required @Configurable
	public void setSessionFactory(PersistenceGmSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	@Required @Configurable
	public void setTracer(ProcessTracer tracer) {
		this.tracer = tracer;
	}
	
	// TODO: This property is not modelled on denotation type - is it needed?
	@Configurable
	public void setPriviledgedRoles(Set<String> priviledgedRoles) {
		this.priviledgedRoles = priviledgedRoles;
	}
	
	@Required
	public void setDeployedProcessingEngine(tribefire.extension.process.model.deployment.ProcessingEngine deployedProcessingEngine) {
		
		/* create and store an extract from the passed process engine which contains all relevant data and not more
		 * in order to decouple from a session that may not be open after this setter call
		 */
		StandardMatcher matcher = new StandardMatcher();
		
		TraversingCriterion traversingCriterion = TC.create()
			.disjunction()
				.pattern().entity(GmEntityType.class).property("metaData").close()
				.pattern().entity(GmEntityType.class).property("propertyMetaData").close()
				.pattern().entity(GmProperty.class).property("metaData").close()
				.pattern().entity(GmEnumType.class).property("metaData").close()
				.pattern().entity(Deployable.class).property("cartridge").close()
				.pattern().entity(Deployable.class).property("trace").close()
				.pattern().entity(Deployable.class).property("dependencies").close()
			.close()
			.done();

		matcher.setCriterion(traversingCriterion);
		
		StandardCloningContext cloningContext = new StandardCloningContext();
		cloningContext.setAbsenceResolvable(true);
		cloningContext.setMatcher(matcher);
		
		// clone a safe and complete copy for later use
		deployedProcessingEngine = (tribefire.extension.process.model.deployment.ProcessingEngine)GMF.getTypeReflection()
				.getBaseType().clone(cloningContext, deployedProcessingEngine, StrategyOnCriterionMatch.partialize);
		
		this.deployedProcessingEngine = deployedProcessingEngine;
		
		// build the infos needed for fast lookups during processing
		Set<ProcessDefinition> definitions = deployedProcessingEngine.getProcessDefinitions();
		
		if (definitions != null) {
			for (final ProcessDefinition definition: definitions) {
				ProcessDefinitionInfo info = new ProcessDefinitionInfo();
				info.processDefinition = definition;
				
				for (ProcessElement processElement: NullSafe.set(definition.getElements())) {
					if (processElement instanceof Node) {
						Node node = (Node)processElement;
						Object state = node.getState();
						
						info.nodeByState.put(state, node);
					}
					else if (processElement instanceof Edge) {
						Edge edge = (Edge)processElement;
						StandardNode fromNode = edge.getFrom();
						Object fromState = fromNode.getState();
						Object toState = edge.getTo().getState();
						Pair<Object, Object> key = new Pair<Object, Object>(fromState, toState);
						
						info.fromNodes.add(fromNode);
						info.edgesByStateChange.put(key, edge);
					}
					
				}
				
				for (Node node: info.nodeByState.values()) {
					if (node instanceof StandardNode) {
						StandardNode standardNode = (StandardNode)node;
						if (!info.fromNodes.contains(standardNode))
							info.drainNodes.add(standardNode);
					}
				}
				
				definitionInfos.put(definition, info);
				definitionInfoByName.put(definition.getName(), info);
			}
		}
	}
	
	protected TraceBuilder traceBuilder(final StateChangeContext<Process> context, final Object state) throws StateChangeProcessorException  {
		return traceBuilder(context, state, false);
	}
	
	protected TraceBuilder traceBuilder(final StateChangeContext<Process> context, final Object state, final boolean traceAsSystemUser) throws StateChangeProcessorException  {
		final Process process = context.getSystemProcessEntity();
		return tracer.build(new TraceContext() {
			
			@Override
			public Class<?> getLoggerContextClass() {
				return ProcessingEngine.class;
			}
			
			@Override
			public PersistenceGmSession getSession() {
				return context.getSystemSession();
			}
			
			@Override
			public Process getProcess() {
				return process;
			}
			
			@Override
			public String getInitiator() {
				if (traceAsSystemUser)
					return context.getSystemSession().getSessionAuthorization().getUserId();
				else
					return context.getSession().getSessionAuthorization().getUserId();
			}
			
			@Override
			public Object getState() {
				return state;
			}
		});
	}
	
	@Override
	public List<StateChangeProcessorMatch> matches(StateChangeProcessorSelectorContext context) {
		if (!context.isForProperty())
			return Collections.emptyList();

		ensureMoverValidationContext(context);
		
		// check ProcessTrace.restart
		EntityType<ProcessTrace> traceType = ProcessTrace.T;
		
		if (context.getProperty().getName().equals("restart") && traceType.isAssignableFrom(context.getEntityType())) {
			Manipulation manipulation = context.getManipulation();
			if (manipulation instanceof ChangeValueManipulation) {
				ChangeValueManipulation cvm = (ChangeValueManipulation) manipulation;
				Object newValue = cvm.getNewValue();
				if (Boolean.TRUE.equals(newValue)) {
					return getMoverMatchForTraceRestart(context);
				}
			}
		}
		
		// check process definitions for matches on state properties
		Set<ProcessDefinition> definitions = deployedProcessingEngine.getProcessDefinitions();
		
		if (definitions != null) {
			for (final ProcessDefinition definition: definitions) {
				GmProperty gmProperty = definition.getTrigger();
				
				if (gmProperty != null) {
					GmEntityType type = definition.triggerType();
					EntityType<?> candidateEntityType = (EntityType<?>) type.reflectionType();
					
					EntityType<?> entityType = context.getEntityType();
					if (candidateEntityType.isAssignableFrom(entityType)) {
						
						if (context.getProperty().getName().equals(Process.activity)) {
							return matchesActivation(context, definition);
						}
						else if (context.getProperty().getName().equals(gmProperty.getName())) {
							
							ProcessDefinitionInfo info = definitionInfos.get(definition);
							
							// determine transition
							ChangeValueManipulation manipulation = (ChangeValueManipulation) context.getManipulation();
							
							Object leavingState = null;
							Object enteringState = manipulation.getNewValue();
							
							EntityReference reference = context.getEntityProperty().getReference();
							boolean isRestartEdge = false;
							
							double executionPriority = 0d;
							
							if (reference instanceof PersistentEntityReference) {
								try {
									Process processAsBefore = (Process) context.getSystemSession().query().entity(reference).require();
									
									if (processAsBefore instanceof HasExecutionPriority) {
										executionPriority = ((HasExecutionPriority) processAsBefore).getExecutionPriority();
									}
									
									Property property = entityType.getProperty(gmProperty.getName());
									leavingState = property.get(processAsBefore);
									
									ProcessTrace trace = processAsBefore.getTrace();
									if (trace != null) {
										String event = trace.getEvent();
										if (event != null) {
											isRestartEdge = event.equals(EVENT_ADMINISTRATIVE_RESTART);
											
											
											// reset counter
											resetRestartCounter(context, leavingState, enteringState, processAsBefore);
										}
									}
										
								} catch (Exception e) {
									logger.error(String.format("cannot resolve process which is expected to exist [%s]", reference), e);
								}
							}
	
							
							Edge edge = info.edgesByStateChange.get(new Pair<Object, Object>(leavingState, enteringState));
							Node leavingNode = info.nodeByState.get(leavingState);
							Node enteringNode = info.nodeByState.get(enteringState);
							
							
							StateChangeProcessorMatch invalidationMatch = getInvalidationMatch(context, leavingState, enteringState, leavingNode, enteringNode, edge);
							
							boolean restart = enteringNode instanceof RestartNode;
							boolean restartPreparation = leavingNode instanceof RestartNode;
							
							if (invalidationMatch == null) {
								if(isRestartEdge) {
									StateChangeProcessorMatch match = new TransitionTracer(leavingState, enteringState, info); 
									return Collections.singletonList(match);
								}
								MatchesBuilder matchesBuilder = new MatchesBuilder(info, leavingState, enteringState, restart, executionPriority);
								
								List<TransitionProcessor> genericOnTransitProcessors = info.processDefinition.getOnTransit();
								if (genericOnTransitProcessors != null) {
									matchesBuilder.append( genericOnTransitProcessors);
								}
								if (edge != null) {
									matchesBuilder.append(((StandardNode)leavingNode).getOnLeft());
									matchesBuilder.append(edge.getOnTransit());
								}
								
								if (!restartPreparation)
									matchesBuilder.append(enteringNode.getOnEntered());
								
								return matchesBuilder.build();
							}
							else
								return Collections.singletonList(invalidationMatch);
						}
						
					}
				}
			}
		}

		return Collections.emptyList();
	}
	
	private static void ensureMoverValidationContext(StateChangeProcessorSelectorContext context) {
		MoverValidationContext processorContext  = context.getCustomContext();
		if (processorContext == null) {
			processorContext = MoverValidationContext.T.create();	
			processorContext.setRestartedProcessReferences(new HashSet<String>());
			context.setCustomContext(processorContext);
		}
	}

	private List<StateChangeProcessorMatch> getMoverMatchForTraceRestart(StateChangeProcessorSelectorContext context) {
		EntityReference reference = context.getEntityProperty().getReference();

		SelectQuery query = new SelectQueryBuilder()
			.from(Process.class,"p")
			.join("p", "traces", "t")
			.where()
				.property("t", "id").eq(reference.getRefId())
			.select("p")
			.select("t")
			.done();
		
		try {
			ListRecord result = (ListRecord)context.getSystemSession().query().select(query).first();
			
			if (result != null) {
				Process process = (Process)result.getValues().get(0);
				ProcessTrace trace = (ProcessTrace)result.getValues().get(1);
				EntityType<Process> processType = process.entityType();
				
				ProcessDefinition processDefinition = getProcessDefinitionForType(processType);
				
				if (processDefinition != null) {
					
					Object fromState = trace.getFromState();
					Object toState = trace.getToState();
					
					ProcessDefinitionInfo processDefinitionInfo = definitionInfos.get(processDefinition);
					
					Edge edge = processDefinitionInfo.edgesByStateChange.get(new Pair<Object,Object>(fromState, toState));
					
					if (edge != null) {
						MoverCallContext moverCallContext = MoverCallContext.T.create();
						moverCallContext.setMoveFrom(fromState);
						moverCallContext.setMoveTo(toState);
						moverCallContext.setProcessReference((PersistentEntityReference) process.reference());
						moverCallContext.setProcessDefinitionName(processDefinition.getName());
						moverCallContext.setInitiator(context.getSession().getSessionAuthorization().getUserId());
						MoverValidationContext validiationContext = context.getCustomContext();
						return Collections.<StateChangeProcessorMatch>singletonList(new Mover(moverCallContext, validiationContext));
					}
					else {
						logger.warn(String.format("no valid transition found for a restart in trace %s", reference));
					}
				}
				else {
					logger.warn(String.format("no ProcessDefinition found for a restart in trace %s", reference));
				}
			}
			else {
				logger.warn(String.format("no owner process found for the trace %s when trying to restart it", reference));
			}
			
		} catch (GmSessionException e) {
			logger.error(String.format("error while determine owner process for the trace %s", reference),e);
		}
		
		return Collections.emptyList();
	}
	
	private ProcessDefinition getProcessDefinitionForType(EntityType<Process> processType) {
		// check process definitions for matches on state properties
		Set<ProcessDefinition> definitions = deployedProcessingEngine.getProcessDefinitions();
		
		if (definitions != null) {
			for (final ProcessDefinition definition: definitions) {
				GmProperty gmProperty = definition.getTrigger();
				
				if (gmProperty != null) {
					GmEntityType type = definition.triggerType();
					EntityType<?> candidateEntityType = type.entityType();
					
					if (candidateEntityType.isAssignableFrom(processType)) {
						return definition;
					}
				}
			}
		}
		
		return null;
	}
				
	private List<StateChangeProcessorMatch> matchesActivation(StateChangeProcessorSelectorContext context, ProcessDefinition definition) {
		if (!isPriviledgedUserRequest(context)) {
			// determine transition
			ChangeValueManipulation manipulation = (ChangeValueManipulation) context.getManipulation();
			ProcessActivity activity = (ProcessActivity)manipulation.getNewValue();
	
			if (activity == ProcessActivity.processing) {
				ActivatorCallContext activatorCallContext = ActivatorCallContext.T.create();
				activatorCallContext.setProcessDefinitionName(definition.getName());
				return Collections.<StateChangeProcessorMatch>singletonList(new Activator(activatorCallContext));
			}
		}
		
		return Collections.emptyList();
	}
	
	private class Activator extends StateChangeProcessorAdapter<Process, ActivatorCallContext> implements StateChangeProcessorMatch {

		private ActivatorCallContext activatorCallContext;
		
		public Activator(ActivatorCallContext activatorCallContext) {
			this.activatorCallContext = activatorCallContext;
		}
		
		public Activator() {
		}
		
		@Override
		public ActivatorCallContext onBeforeStateChange(BeforeStateChangeContext<Process> context) throws StateChangeProcessorException {
			return activatorCallContext;
		}
		
		@Override
		public void onAfterStateChange(AfterStateChangeContext<Process> context, ActivatorCallContext customContext) throws StateChangeProcessorException {
			Process processEntity = context.getSystemProcessEntity();
			ProcessDefinitionInfo definitionInfo = definitionInfoByName.get(customContext.getProcessDefinitionName());
			GmProperty gmStateProperty = definitionInfo.processDefinition.getTrigger();
			String statePropertyName = gmStateProperty.getName();
			Property stateProperty = processEntity.entityType().getProperty(statePropertyName);
			Object state = stateProperty.get(processEntity);
			
			traceBuilder(context, state)
				.trace(EVENT_PROCESS_RESUMED);
		}
		
		@Override
		public void processStateChange(ProcessStateChangeContext<Process> context, ActivatorCallContext customContext) throws StateChangeProcessorException {
			Object state = null; 
					
			try {
				Process processEntity = context.getSystemProcessEntity();
				ProcessDefinitionInfo definitionInfo = definitionInfoByName.get(customContext.getProcessDefinitionName());
				GmProperty gmStateProperty = definitionInfo.processDefinition.getTrigger();
				String statePropertyName = gmStateProperty.getName();
				Property stateProperty = processEntity.entityType().getProperty(statePropertyName);
				state = stateProperty.get(processEntity);
				Node node = definitionInfo.nodeByState.get(state);
				
				if (node instanceof StandardNode) {
					processConditions(context, processEntity, stateProperty, (StandardNode)node, null, null);
				}
			} catch (Exception e) {
				traceBuilder(context, state)
					.exception(e)
					.message("error while activate process")
					.error(EVENT_INTERNAL_ERROR);
			}
		}
		
		@Override
		public String getProcessorId() {
			return getClass().getName();
		}
		

		@Override
		public StateChangeProcessor<?, ?> getStateChangeProcessor() {
			return this;
		}
	}

	private void resetRestartCounter(StateChangeProcessorSelectorContext context, Object leavingState, Object enteringState, Process process) {
		try {
			EntityType<? extends Process> entityType = process.entityType();
			SelectQuery counterQuery = new SelectQueryBuilder()
				.from(entityType, "p")
				.join("p", Process.restartCounters, "c")
				.where()
				.conjunction()
					.property("p", null).eq().entity(process)
					.property("c", "leftState").eq(leavingState)
					.property("c", "restartState").ne(enteringState)
				.close()
				.select("c")
				.done();
			
			RestartCounter counter = context.getSystemSession().query().select(counterQuery).first();
			
			if (counter != null) {
				counter.setCount(0);
			}
			
			context.getSystemSession().commit();
		} catch (Exception e) {
			logger.error(String.format("error while reseting RestartCounter for edge from state [%s] to state [%s] for process %s", leavingState, enteringState, process), e);
		}
	}
	
	private enum InvalidationReason {
		missingLeavingNode, missingEnteringNode, missingEdge;
	}

	private StateChangeProcessorMatch getInvalidationMatch(StateChangeProcessorSelectorContext context, Object leavingState, Object enteringState, Node leavingNode, Node enteringNode, Edge edge) {
		Set<InvalidationReason> invalidationReasons = new HashSet<InvalidationReason>();

		if (edge == null && !isPriviledgedUserRequest(context))
			invalidationReasons.add(InvalidationReason.missingEdge);
		
		if (leavingNode == null)
			invalidationReasons.add(InvalidationReason.missingLeavingNode);
		
		if (enteringNode == null && enteringState != null)
			invalidationReasons.add(InvalidationReason.missingEnteringNode);
		
		if (invalidationReasons.isEmpty()) {
			return null;
		}
		else {
			return new InvalidationMatch(leavingState, enteringState, invalidationReasons);
		}
	}

	private boolean isPriviledgedUserRequest(StateChangeProcessorSelectorContext context) {
		Set<String> userRoles = context.getSession().getSessionAuthorization().getUserRoles();
		for (String priviledgedRole: priviledgedRoles) {
			if (userRoles.contains(priviledgedRole))
				return true;
		}
		return false;
	}

	private class MatchesBuilder {
		private List<TransitionProcessor> transitionProcessors = new ArrayList<TransitionProcessor>();
		private ProcessDefinitionInfo processDefinitionInfo;
		private Object enteringState;
		private Object leavingState;
		private boolean restart;
		private double executionPriority;
		
		public MatchesBuilder(ProcessDefinitionInfo processDefinitionInfo, Object leavingState, Object enteringState, boolean restart, double executionPriority) {
			super();
			this.processDefinitionInfo = processDefinitionInfo;
			this.leavingState = leavingState;
			this.enteringState = enteringState;
			this.restart = restart;
			this.executionPriority = executionPriority;
		}
		
		public void append(List<TransitionProcessor> processors) {
			if (processors == null)
				return;
			
			for (TransitionProcessor transitionProcessor: processors) {
				if (transitionProcessor.getAutoDeploy()) { //TODO: originally checked for getDeployed() -> if this check actually needs to be aware of the deployment state an according DDSA request needs to be performed
					transitionProcessors.add(transitionProcessor);
				}
			}
		}
		
		public List<StateChangeProcessorMatch> build() {
			List<StateChangeProcessorMatch> matches = new ArrayList<StateChangeProcessorMatch>();
			
			// add processor that will create transition traces
			matches.add(new TransitionTracer(leavingState, enteringState, processDefinitionInfo));
			
			String processDefinitionName = processDefinitionInfo.processDefinition.getName();
			StateChangeProcessorMatch match = createMatch(processDefinitionName, transitionProcessors, leavingState, enteringState, executionPriority);
			
			matches.add(match);
			
			if (restart) {
				RestarterCallContext restarterCallContext = RestarterCallContext.T.create();
				restarterCallContext.setLeftState(leavingState);
				restarterCallContext.setEnteredState(enteringState);
				restarterCallContext.setProcessDefinitionName(processDefinitionName);
				matches.add(new Restarter(restarterCallContext));
			}
			
			return matches;
		}
	}
	
	private StateChangeProcessorMatch createMatch(String processDefinitionName, final List<TransitionProcessor> transitionProcessors, Object leavingState, Object enteredState, final double executionPriority) {
		
		List<TransitionProcessor> exportedTransitionProcessors = new ArrayList<TransitionProcessor>();
		for (TransitionProcessor transitionProcessor: transitionProcessors) {
			TransitionProcessor exportedTransitionProcessor = export(transitionProcessor);
			exportedTransitionProcessors.add(exportedTransitionProcessor);
		}

		TransitionProcessorCallContext context = TransitionProcessorCallContext.T.create();
		context.setProcessDefinitionName(processDefinitionName);
		context.setEnteredState(enteredState);
		context.setLeftState(leavingState);
		context.setTransitionProcessors(exportedTransitionProcessors);
		
		final TransitionStateChangeProcessor stateChangeProcessor = new TransitionStateChangeProcessor(context);
		return new PrioritizedStateChangeProcessorMatch() {
			
			@Override
			public String getProcessorId() {
				return getRuleId();
			}
			
			@Override
			public StateChangeProcessor<?, ?> getStateChangeProcessor() {
				return stateChangeProcessor;
			}
			
			@Override
			public double getAsyncExecutionPriority() {
				return executionPriority;
			}
		};
	}

	private static <D extends Deployable> D export(D deployable) {
		EntityType<D> entityType = deployable.entityType();
		D exportedDeployable = entityType.create();
		exportedDeployable.setId(deployable.getId());
		exportedDeployable.setExternalId(deployable.getExternalId());
		return exportedDeployable;
	}

	@Override
	public String getRuleId() {
		return getClass().getSimpleName() + ":" + deployedProcessingEngine.getExternalId();
	}
	
	private static String buildProcessorId(Deployable transitionProcessor) {
		return transitionProcessor.entityType().getShortName() + "(" + transitionProcessor.getExternalId() + ")";
	}

	@Override
	public StateChangeProcessor<? extends GenericEntity, ? extends GenericEntity> getStateChangeProcessor(String processorId) {
		if (processorId.equals(Activator.class.getName())) {
			return new Activator();
		}
		else if (processorId.equals(Restarter.class.getName()))
			return new Restarter();
		else if (processorId.equals(Mover.class.getName()))
			return new Mover();
		else
			return new TransitionStateChangeProcessor();
	}
	
	private class Restarter implements StateChangeProcessor<Process, RestarterCallContext>, StateChangeProcessorMatch  {
		
		private RestarterCallContext context;
		private StateChangeProcessorCapabilities capabilities = StateChangeProcessors.capabilities(true, false, true);

		public Restarter(RestarterCallContext context) {
			this.context = context;
		}
		
		public Restarter() {
		}

		@Override
		public RestarterCallContext onBeforeStateChange(BeforeStateChangeContext<Process> context) throws StateChangeProcessorException {
			return this.context;
		}

		@Override
		public void onAfterStateChange(AfterStateChangeContext<Process> context, RestarterCallContext customContext) throws StateChangeProcessorException {
			// do nothing as the restarter acts only in processStateChange();
		}

		@Override
		public void processStateChange(ProcessStateChangeContext<Process> context, RestarterCallContext customContext) throws StateChangeProcessorException {
			Process processEntity = context.getSystemProcessEntity();
			String propertyName = context.getEntityProperty().getPropertyName();
			EntityType<Process> entityType = processEntity.entityType();
			Property stateProperty = entityType.getProperty(propertyName); 
			Object actualState = stateProperty.get(processEntity);
			
			Object enteredState = customContext.getEnteredState();
			
			if (nullSafeEquals(actualState, enteredState)) {
				// detected that no TransitionProcessor took over the control of the flow and therefor we finally execute the restart
				ProcessDefinitionInfo info = definitionInfoByName.get(customContext.getProcessDefinitionName());
				RestartNode restartNode = (RestartNode)info.nodeByState.get(enteredState);
				
				Edge restartEdge = restartNode.getRestartEdge();
				
				Object leavingState = null;
				Object enteringState = null;
				
				if (restartEdge != null) {
					leavingState = restartEdge.getFrom().getState();
					enteringState = restartEdge.getTo().getState();
				}
				else {
					SelectQuery query = new SelectQueryBuilder()
						.from(Process.class, "p")
						.join("p", "traces", "t")
						.where()
							.conjunction()
								.property("t", "kind").eq(TraceKind.trace)
								.property("t", "event").eq(EVENT_EDGE_TRANSITION)
							.close()
						.select("t")
						.orderBy().property("t", "date")
						.orderingDirection(OrderingDirection.descending)
						.limit(1)
						.done();
							
					try {
						ProcessTrace trace = (ProcessTrace)context.getSystemSession().query().select(query).first();
						
						if (trace != null) {
							String encodedLeavingState = trace.getFromState();
							String encodedEnteringState = trace.getToState();
							
							SimpleType simpleType = stateProperty.getType().cast();
							
							leavingState = encodedLeavingState != null? simpleType.instanceFromString(encodedLeavingState): null;
							enteringState = encodedEnteringState != null? simpleType.instanceFromString(encodedEnteringState): null;
						}
						else {
							String msg = "automatic restart failed due to missing previous transition trace for process " + processEntity;
							traceBuilder(context, enteredState)
								.edge(customContext.getLeftState(), customContext.getEnteredState())
								.message(msg) 
								.error(EVENT_CORRUPTED_PROCESS_STATE);
						}
					} catch (GmSessionException e) {
						String msg = "error while determining previous transition trace from process entity";

						traceBuilder(context, enteredState)
							.edge(customContext.getLeftState(), customContext.getEnteredState())
							.message(msg) 
							.exception(e)
							.error(EVENT_INTERNAL_ERROR);
					}
				}
				
				// modify process entity to jump in repeated leaving state and then in entering state and respect restart limits 
				try {
					Integer maxRestarts = determineMaxRestarts(info, restartNode);
					
					int currentCount = 0;
					
					boolean restart = true;
					
					if (maxRestarts != -1) {
						SelectQuery counterQuery = new SelectQueryBuilder()
							.from(Process.class, "p")
							.join("p", Process.restartCounters, "c")
							.where()
							.conjunction()
								.property("p", null).eq().entity(processEntity)
								.property("c", "leftState").eq(String.valueOf(leavingState))
								.property("c", "restartState").eq(String.valueOf(enteringState))
							.close()
							.select("c")
							.done();
				
						RestartCounter counter = context.getSystemSession().query().select(counterQuery).first();
						
						if (counter == null) {
							counter = context.getSystemSession().create(RestartCounter.T);
							counter.setLeftState(String.valueOf(leavingState));
							counter.setRestartState(String.valueOf(enteringState));
							Set<RestartCounter> restartCounters = processEntity.getRestartCounters();
							if (restartCounters == null) {
								restartCounters = new HashSet<RestartCounter>();
								processEntity.setRestartCounters(restartCounters);
							}
							
							restartCounters.add(counter);
						}
		
						currentCount = counter.getCount();
						
						if (currentCount < maxRestarts) {
							counter.setCount(currentCount + 1);
						}
						else {
							traceBuilder(context, enteredState)
								.edge(customContext.getLeftState(), customContext.getEnteredState())
								.message(String.format("max restarts [%s] reached", maxRestarts))
								.error(EVENT_MAX_RESTART);
							
							// send to potential error node or let the process pause here indefinitly
							Node errorNode = restartNode.getErrorNode();
							
							if (errorNode == null)
								errorNode = info.processDefinition.getErrorNode();
							
							if (errorNode != null) {
								stateProperty.set(processEntity, errorNode.getState());
							}
							else {
								traceBuilder(context, enteredState)
									.edge(customContext.getLeftState(), customContext.getEnteredState())
									.message("no error node found in fallback chain of a restart node after max restarts was reached")
									.error(EVENT_MISSING_ERROR_NODE);
								processEntity.setOverdueAt(null);
							}
							context.getSystemSession().commit();
							
							restart = false;
						}
					}

					
					if (restart) {
						String msg = maxRestarts != null?
								String.format("restarting edge from state [%s] to state [%s] in iteration %s", leavingState, enteringState, currentCount):
								String.format("restarting edge from state [%s] to state [%s]. ", leavingState, enteringState);
								
						traceBuilder(context, enteredState)
							.edge(customContext.getLeftState(), customContext.getEnteredState())
							.message(msg)
							.trace(EVENT_RESTART);

						stateProperty.set(processEntity, leavingState);
						context.getSystemSession().commit();
						stateProperty.set(processEntity, enteringState);
						context.getSystemSession().commit();
					} 
					
				} catch (Exception e) {
					logger.error(String.format("error while executing restart from restart state [%s] over state [%s] to state [%s] for process %s", 
							enteredState, leavingState, enteringState, processEntity), e);
				}
			}
		}

		private Integer determineMaxRestarts(ProcessDefinitionInfo info, RestartNode restartNode) {
			Integer maxRestarts = restartNode.getMaximumNumberOfRestarts();
			if (maxRestarts == null)
				maxRestarts = info.processDefinition.getMaximumNumbersOfRestarts();
				
			if (maxRestarts == null)
				maxRestarts = 3;
			return maxRestarts;
		}

		@Override
		public String getProcessorId() {
			return getClass().getName();
		}

		@Override
		public StateChangeProcessor<?, ?> getStateChangeProcessor() {
			return this;
		}

		@Override
		public StateChangeProcessorCapabilities getCapabilities() {
			return capabilities;
		}
	}



	private class Mover implements StateChangeProcessor<ProcessTrace, MoverCallContext>, StateChangeProcessorMatch  {
		
		private MoverCallContext context;
		private MoverValidationContext validationContext;
		

		public Mover(MoverCallContext context, MoverValidationContext validationContext) {
			this.context = context;
			this.validationContext = validationContext;
		}
		
		public Mover() {
		}
		
		@Override
		public MoverCallContext onBeforeStateChange(BeforeStateChangeContext<ProcessTrace> context) throws StateChangeProcessorException {
			return this.context;
			
		}

		@Override
		public void onAfterStateChange(AfterStateChangeContext<ProcessTrace> context, MoverCallContext customContext) throws StateChangeProcessorException {
			// simply synchronously reset the trigger property that fired the restart 
			context.getProcessEntity().setRestart(false);
			EntityReference entityReference = this.context.getProcessReference();
			String encodedEntityReference = String.format("%s:%s", entityReference.getTypeSignature(), entityReference.getRefId()); 
				
			if (!validationContext.getRestartedProcessReferences().add(encodedEntityReference)) {
				context.overrideCapabilities(StateChangeProcessors.afterOnlyCapabilities());
				logger.warn("Skipped concurrent administrative restart for process: "+encodedEntityReference+ " from edge: "+customContext.getMoveFrom()+" to "+customContext.getMoveTo());
			}
		}

		@Override
		public void processStateChange(ProcessStateChangeContext<ProcessTrace> context, MoverCallContext customContext) throws StateChangeProcessorException {
			Object leavingState = customContext.getMoveFrom();
			Object enteringState = customContext.getMoveTo();
			Process processEntity = null;
			
			try {
				PersistenceGmSession systemSession = context.getSystemSession();
				processEntity = (Process)systemSession.query().entity(customContext.getProcessReference()).require();
				
				if (processEntity != null) {
					EntityType<Process> processType = processEntity.entityType();
					
					ProcessDefinitionInfo info = definitionInfoByName.get(customContext.getProcessDefinitionName());
					
					GmProperty gmProperty = info.processDefinition.getTrigger();
					Property stateProperty = processType.getProperty(gmProperty.getName());
					
					Object currentState = stateProperty.get(processEntity);
					
					
					String msg = String.format("manually restarting edge from state [%s] to state [%s]. ", leavingState, enteringState);
							
					tracer.build(processEntity, systemSession, customContext.getInitiator(), currentState)
						.message(msg)
						.trace(EVENT_ADMINISTRATIVE_RESTART);
	
					stateProperty.set(processEntity, leavingState);
					context.getSystemSession().commit();
					stateProperty.set(processEntity, enteringState);
					context.getSystemSession().commit();
				}
				
				
			} catch (Exception e) {
				logger.error(String.format("error while executing manual restart with restart edge from [%s] to [%s] to state [%s] for process %s", 
						leavingState, enteringState, processEntity), e);
			}
		}

		@Override
		public String getProcessorId() {
			return getClass().getName();
		}

		@Override
		public StateChangeProcessor<?, ?> getStateChangeProcessor() {
			return this;
		}

	}

	private class TransitionStateChangeProcessor implements StateChangeProcessor<Process, TransitionProcessorCallContext> {
		private TransitionProcessorCallContext context;

		public TransitionStateChangeProcessor(TransitionProcessorCallContext context) {
			this.context = context;
		}
		
		public TransitionStateChangeProcessor() {
		}

		@Override
		public TransitionProcessorCallContext onBeforeStateChange(BeforeStateChangeContext<Process> context) throws StateChangeProcessorException {
			
			return this.context;
		}
		
		@Override
		public void onAfterStateChange(AfterStateChangeContext<Process> context, TransitionProcessorCallContext customContext) throws StateChangeProcessorException {
			
			Object state = customContext.getEnteredState();
			String processDefinitionName = customContext.getProcessDefinitionName();
			ProcessDefinitionInfo definitionInfo = definitionInfoByName.get(processDefinitionName);
			
			Node node = definitionInfo.nodeByState.get(state);
			
			if (node instanceof StandardNode) {

				// ensure process activity to processing in case the state change is interpreted as reactivation 
								
				Process processEntity = null;
				try {
					processEntity = context.getSystemProcessEntity();
					ProcessActivity currentActivity = processEntity.getActivity();
					if (currentActivity != ProcessActivity.processing) {
						ProcessTrace trace = processEntity.getTrace();
						if (trace == null || !trace.getEvent().equals(EVENT_RESTART_TRANSITION)) {
							processEntity.setActivity(ProcessActivity.processing);
							context.getSystemSession().commit();
							traceBuilder(context, state, true)
							.edge(customContext.getLeftState(), customContext.getEnteredState())
							.trace(EVENT_PROCESS_RESUMED);
						}
					}
					
				} catch (GmSessionException e) {
					throw new StateChangeProcessorException("error while setting process into wait state: " + processEntity, e);
				}
			}			
		}

		@Override
		public void processStateChange(ProcessStateChangeContext<Process> context, TransitionProcessorCallContext customContext) throws StateChangeProcessorException {
			ProcessDefinitionInfo definitionInfo = definitionInfoByName.get(customContext.getProcessDefinitionName());
			Edge edgeCanditate = definitionInfo.edgesByStateChange.get(new Pair<Object, Object>(customContext.getLeftState(), customContext.getEnteredState()));
			Node enteredNode = definitionInfo.nodeByState.get(customContext.getEnteredState());
			ProcessDefinition processDefinition = definitionInfo.processDefinition;

			if (callTransitionProcessors(context, customContext, edgeCanditate, enteredNode, processDefinition)) {				
				processConditionsIfDemandedOrWait(context, customContext, enteredNode);
			}

			// write process end trace
			if (definitionInfo.drainNodes.contains(enteredNode)) {
				context.getSystemProcessEntity().setActivity(ProcessActivity.ended);
				traceBuilder(context, customContext.getEnteredState())
					.edge(customContext.getLeftState(), customContext.getEnteredState())
					.trace(EVENT_PROCESS_ENDED);
			}
		}
		
		private boolean callTransitionProcessors(ProcessStateChangeContext<Process> context, TransitionProcessorCallContext customContext, Edge edgeCanditate, Node enteredNode, ProcessDefinition processDefinition)
				throws StateChangeProcessorException {
			Map<TransitionProcessor, ContinueWithState> continueStates = new LinkedHashMap<TransitionProcessor, ContinueWithState>();
			Object leftState = customContext.getLeftState();
			Object enteredState = customContext.getEnteredState();
			
			for (TransitionProcessor transitionProcessor: customContext.getTransitionProcessors()) {
				String processorInfo = buildProcessorId(transitionProcessor);
				long duration = -1;
				try {
					tribefire.extension.process.api.TransitionProcessor<Process> actualTransitionProcessor = getTransitionProcessor(transitionProcessor);
					boolean isDeployed = true;
					
					// TODO Clarify how to check if the TP is deployed
					
					if (isDeployed) {
						String traceMsg = "calling transition processor " + processorInfo;
						
						traceBuilder(context, enteredState)
							.edge(leftState, enteredState)
							.details(buildProcessorPreCallDetails(transitionProcessor, ProcessorKind.transition))
							.message(traceMsg)
							.trace(EVENT_PRECALL_TRANSITION_PROCESSOR);
	
						TransitionProcessorContextImpl<Process> transitionProcessorContext = new TransitionProcessorContextImpl<Process>(context, leftState, enteredState);
	
						long startTime = System.currentTimeMillis();
						
						try {
							actualTransitionProcessor.process(transitionProcessorContext);
							context.commitIfNecessary();
						}
						finally {
							long endTime = System.currentTimeMillis();
							duration = endTime - startTime;
						}
						
						traceMsg = "completed transition processor " + processorInfo;
						
						traceBuilder(context, enteredState)
							.edge(leftState, enteredState)
							.details(buildProcessorPostCallDetails(transitionProcessor, ProcessorKind.transition, duration, true))
							.message(traceMsg)
							.trace(EVENT_POSTCALL_TRANSITION_PROCESSOR);
						
						ContinueWithState continueWithState = transitionProcessorContext.getContinueWithState();
						if (continueWithState != null) {
							continueStates.put(transitionProcessor, continueWithState);
						}
					}
				} catch (Exception e) {
					String traceMsg = "error while executing transition processor " + processorInfo;
					traceBuilder(context, enteredState)
						.edge(leftState, enteredState)
						.message(traceMsg)
						.details(buildProcessorPostCallDetails(transitionProcessor, ProcessorKind.transition, duration, false))
						.exception(e)
						.error(EVENT_ERROR_IN_TRANSITION_PROCESSOR);
					
					sendToErrorNode(context, customContext, enteredNode, edgeCanditate, enteredNode, processDefinition);
					break;
				}
			}
			// all TP are run here 
			
			// check state continuation

			String errorMessage = null;

			switch (continueStates.size()) {
				case 0:					
					// return true in order to process conditions
					return true;
				case 1: {
					// record distinct continuation demand
					Entry<TransitionProcessor, ContinueWithState> entry = continueStates.entrySet().iterator().next();
					TransitionProcessor demandingProcessor = entry.getKey(); 
					ContinueWithState continueWithState = entry.getValue();
					
					DistinctContinuationDemand continuationDemand = primeContinuationDemand(
							DistinctContinuationDemand.T.create(), demandingProcessor, continueWithState);
					
					Object demandedState = continueWithState.getState();
					
					traceBuilder(context, enteredState)
						.edge(leftState, enteredState)
						.message(String.format("transition processor %s demanded continuation with state [%s]", demandingProcessor, demandedState))
						.details(continuationDemand)
						.trace(EVENT_CONTINUATION_DEMAND);
					
					
					ProcessDefinitionInfo info = definitionInfos.get(processDefinition);
					Node demandedEnteringNode = info.nodeByState.get(demandedState);
					Edge edge = info.edgesByStateChange.get(new Pair<Object, Object>(enteredState, demandedState));
					
					errorMessage = "Cannot continue with demanded State '" + demandedState.toString() + " because ";
							
					if (demandedEnteringNode == null) {
						errorMessage += "there doesn't exist any node for it";
					}
					else if (edge == null) {
						errorMessage += "there is no edge towards it from current state '" + enteredState.toString() + "'.";
					}else {
						// Transition is legal
						// move process to the demanded state
						setProcessState(context, demandedState);
						
						// return false to veto against condition processing
						return false;
					}
				}
				break;
				default: {
					errorMessage = String.format("%d transition processors demanded continuation", continueStates.size());
				}
			}
			// We only get here if something went wrong with the continuation demand
			// record ambiguous continuation demand
			AmbiguousContinuationDemand ambiguosContinuationDemand = AmbiguousContinuationDemand.T.create();
			List<ContinuationDemand> demands = ambiguosContinuationDemand.getContinuationDemands();
			for (Entry<TransitionProcessor, ContinueWithState> ambiguousEntry: continueStates.entrySet()) {
				TransitionProcessor demandingProcessor = ambiguousEntry.getKey(); 
				ContinueWithState continueWithState = ambiguousEntry.getValue();
				ContinuationDemand ambiguousContinuationDemand = primeContinuationDemand(
						ContinuationDemand.T.create(), demandingProcessor, continueWithState);
				demands.add(ambiguousContinuationDemand);
			}
			
			traceBuilder(context, enteredState)
				.edge(leftState, enteredState)
				.message(errorMessage)
				.details(ambiguosContinuationDemand)
				.error(EVENT_ERROR_AMBIGUOUS_CONTINUATION_DEMAND);
			
			// send to error node after the ambiguity error
			sendToErrorNode(context, customContext, enteredNode, edgeCanditate, enteredNode, processDefinition);
			
			// return false to veto against condition processing
			return false;
		}

		private void processConditionsIfDemandedOrWait(ProcessStateChangeContext<Process> context, TransitionProcessorCallContext customContext, Node enteredNode) throws StateChangeProcessorException {
			try {
				// auto state change based on conditionals if there was no state change
				if (enteredNode instanceof StandardNode) {
					StandardNode standardNode = (StandardNode)enteredNode;
					Process processEntity = context.getSystemProcessEntity();
					String propertyName = context.getEntityProperty().getPropertyName();
					Property stateProperty = processEntity.entityType().getProperty(propertyName);
					context.getSystemSession().query().entity(processEntity).refresh();
					Object actualState = stateProperty.get(processEntity);
					
					// continue only if we are still processing the original entered state
					if (nullSafeEquals(actualState, customContext.getEnteredState())) {
						if (standardNode.getDecoupledInteraction() == null) {
							processConditions(context, processEntity, stateProperty, standardNode, customContext.getLeftState(), customContext.getEnteredState());
						}
						else { 
							// set process' activity to waiting..
							processEntity.setActivity( ProcessActivity.waiting);
							context.getSystemSession().commit();
							traceBuilder(context, enteredNode.getState())				
							.trace(EVENT_PROCESS_SUSPENDED);							
						}
					}
				}
			} catch (Exception e) {
				traceBuilder(context, customContext.getEnteredState())
					.edge(customContext.getLeftState(), customContext.getEnteredState())
					.message("error while checking conditions for automatic state change")
					.exception(e)
					.error(EVENT_INTERNAL_ERROR);
			}
		}
	}

	private void processConditions(ProcessStateChangeContext<Process> context, Process processEntity, Property stateProperty, StandardNode fromNode, Object edgeFrom, Object edgeTo) throws StateChangeProcessorException {
		List<ConditionalEdge> conditionalEdges = fromNode.getConditionalEdges();
		if (conditionalEdges != null && !conditionalEdges.isEmpty()) {

			BasicConditionProcessorContext<Process> conditionProcessorContext = new BasicConditionProcessorContext<Process>(context.getSession(), context.getSystemSession(),
					context.getProcessEntity(), processEntity);

			for (ConditionalEdge conditionalEdge : conditionalEdges) {
				tribefire.extension.process.model.deployment.ConditionProcessor condition = conditionalEdge.getCondition();

				Object state = conditionalEdge.getTo().getState();
				
				if (condition == null) {
					stateProperty.set(processEntity, state);
					traceBuilder(context, fromNode.getState())
						.edge(edgeFrom, edgeTo)
						.message(String.format("condition for state [%s] matched", state))
						.trace(EVENT_DEFAULT_CONDITION_MATCHED);
					
					return;
				}
				
				ConditionProcessor<Process> processor = getConditionProcessor(condition);
				String conditionInfo = buildProcessorId(condition);
				long duration = -1;
				try {
					String traceMsg = "calling condition processor " + conditionInfo;
					
					traceBuilder(context, fromNode.getState())
						.edge(edgeFrom, edgeTo)
						.details(buildProcessorPreCallDetails(condition, ProcessorKind.condition))
						.message(traceMsg)
						.trace(EVENT_PRECALL_CONDITION_PROCESSOR);

					long start = System.currentTimeMillis();
					boolean matched = false;
					try {
						matched = processor.matches(conditionProcessorContext);
					}
					finally {
						duration = System.currentTimeMillis() - start;
					}
					traceMsg = "called condition processor " + conditionInfo;
					traceBuilder(context, fromNode.getState())
						.edge(edgeFrom, edgeTo)
						.details(buildProcessorPostCallDetails(condition, ProcessorKind.condition, duration, true))
						.message(traceMsg)
						.trace(EVENT_POSTCALL_CONDITION_PROCESSOR);
					
					if (matched) {
						traceBuilder(context, fromNode.getState())
							.edge(edgeFrom, edgeTo)
							.message(String.format("condition for state [%s] matched", state))
							.trace(EVENT_CONDITION_MATCHED);
						stateProperty.set(processEntity, state);
						return;
					}

				} catch (Exception e) {
					traceBuilder(context, fromNode.getState())
						.edge(edgeFrom, edgeTo)
						.details(buildProcessorPostCallDetails(condition, ProcessorKind.condition, duration, false))
						.message("error while checking condition " + conditionInfo)
						.exception(e)
						.error(EVENT_ERROR_IN_CONDITION_PROCESSOR);
					break;
				}
			}
			
			// go back to waiting because no condition matched - probably other processing signals will eventually trigger condition evaluation again
			processEntity.setActivity(ProcessActivity.waiting);
		}

	}
	
	private ConditionProcessor<Process> getConditionProcessor(tribefire.extension.process.model.deployment.ConditionProcessor condition) {
		ConditionProcessor<Process> conditionProcessor = deployedComponentResolver.resolve(condition, tribefire.extension.process.model.deployment.ConditionProcessor.T);
		return conditionProcessor;
	}
	
	private tribefire.extension.process.api.TransitionProcessor<Process> getTransitionProcessor(tribefire.extension.process.model.deployment.TransitionProcessor transition) {
		tribefire.extension.process.api.TransitionProcessor<Process> transitionProcessor = // 
				deployedComponentResolver //
				.resolve(transition, tribefire.extension.process.model.deployment.TransitionProcessor.T,tribefire.extension.process.api.TransitionProcessor.class, c -> {/*intentionally left empty*/});
		
		return transitionProcessor;
	}
	
	private class TransitionTracer implements StateChangeProcessorMatch, StateChangeProcessor<Process, GenericEntity> {
		private Object leavingState;
		private Object enteringState;
		private ProcessDefinitionInfo processDefinitionInfo;

		public TransitionTracer(Object leavingState, Object enteringState, ProcessDefinitionInfo processDefinitionInfo) {
			this.leavingState = leavingState;
			this.enteringState = enteringState;
			this.processDefinitionInfo = processDefinitionInfo;
		}

		@Override
		public GenericEntity onBeforeStateChange(BeforeStateChangeContext<Process> context) throws StateChangeProcessorException {
			// nothing to do here
			return null;
		}

		@Override
		public void onAfterStateChange(AfterStateChangeContext<Process> context, GenericEntity customContext) throws StateChangeProcessorException {
			EntityProperty entityProperty = context.getEntityProperty();
			
			String propertyName = entityProperty.getPropertyName();
			Process processEntity = context.getSystemProcessEntity();
			EntityType<Process> entityType = processEntity.entityType();
			Object enteredState = entityType.getProperty(propertyName).get(processEntity);
			
			if (!nullSafeEquals(enteredState, enteringState)) {
				String traceMsg = String.format(
						"expected entered state [%s] differed from actual entered state [%s] after a state change from state [%s]", 
						enteredState, enteringState, leavingState);
				
				traceBuilder(context, enteredState)
					.edge(leavingState, enteringState)
					.message(traceMsg) 
					.error(EVENT_CORRUPTED_PROCESS_STATE);
			}
			else {
				ProcessTrace trace = processEntity.getTrace();
				
				String traceEvent = EVENT_EDGE_TRANSITION;
				
				String lastEvent = trace != null? trace.getEvent(): null;
				
				if (EVENT_ERROR_IN_TRANSITION_PROCESSOR.equals(lastEvent)) {
					traceEvent = EVENT_ERROR_TRANSITION;
				}
				else if (EVENT_OVERDUE_IN_STATE.equals(lastEvent))  {
					traceEvent = EVENT_OVERDUE_TRANSITION;
				}
				else if (EVENT_RESTART.equals(lastEvent) || EVENT_ADMINISTRATIVE_RESTART.equals(lastEvent))  {
					traceEvent = EVENT_RESTART_TRANSITION;
				}

				Date now = new Date();
				
				processEntity.setLastTransit(now);
				
				// calculate concrete overdue date
				long milliesToOverdue = getGracePeriodInMillies(enteredState);
				
				Date overdueAt = null;
				
				if (milliesToOverdue != -1) {
					overdueAt = new Date(now.getTime() + milliesToOverdue);
				}

				processEntity.setOverdueAt(overdueAt);
				
				traceBuilder(context, enteredState)
					.edge(leavingState, enteringState)
					.date(now)
					.trace(traceEvent);
			}
			

		}
		
		private long getGracePeriodInMillies(Object state) {
			Node node = processDefinitionInfo.nodeByState.get(state);
			
			if (!(node instanceof StandardNode)) {
				return -1;
			}
			
			StandardNode standardNode = (StandardNode)node;
			
			if (processDefinitionInfo.drainNodes.contains(standardNode)) {
				return -1;
			}
			else {
				TimeSpan defaultGracePeriod = processDefinitionInfo.processDefinition.getGracePeriod();
				TimeSpan gracePeriod = standardNode.getGracePeriod();
				
				if (gracePeriod == null)
					gracePeriod = defaultGracePeriod;
				
				if (gracePeriod != null) {
					double millies = TimeSpanConversion.fromTimeSpan(gracePeriod).unit(TimeUnit.milliSecond).toValue();
					return (long)millies;
				}
				else {
					return -1;
				}
			}
		}

		@Override
		public void processStateChange(ProcessStateChangeContext<Process> context, GenericEntity customContext) throws StateChangeProcessorException {
			// nothing to do here
		}

		@Override
		public String getProcessorId() {
			return null;
		}

		@Override
		public StateChangeProcessor<?, ?> getStateChangeProcessor() {
			return this;
		}

		@Override
		public StateChangeProcessorCapabilities getCapabilities() {
			return StateChangeProcessors.afterOnlyCapabilities();
		}
	}
	
	private class InvalidationMatch implements StateChangeProcessorMatch, StateChangeProcessor<Process, GenericEntity> {
		
		
		private Object leavingState;
		private Object enteringState;
		private Set<InvalidationReason> invalidParts;

		public InvalidationMatch(Object leavingState, Object enteringState, Set<InvalidationReason> invalidParts) {
			this.leavingState = leavingState;
			this.enteringState = enteringState;
			this.invalidParts = invalidParts;
		}

		@Override
		public GenericEntity onBeforeStateChange(BeforeStateChangeContext<Process> context) throws StateChangeProcessorException {
			traceBuilder(context, leavingState)
				.edge(leavingState, enteringState)
				.message("blocking manipulation because of reasons " + invalidParts)
				.error(EVENT_INVALID_TRANSITION);

			Process process = context.getProcessEntity();
			
			throw new StateChangeProcessorException(
					String.format("invalid process state transition from [%s] to [%s] for %s for reasons %s", 
							leavingState, enteringState, process, invalidParts));
		}

		@Override
		public void onAfterStateChange(AfterStateChangeContext<Process> context, GenericEntity customContext) throws StateChangeProcessorException {
			// will not be called and is of no means for that StateChangeProcessor
		}

		@Override
		public void processStateChange(ProcessStateChangeContext<Process> context, GenericEntity customContext) throws StateChangeProcessorException {
			// will not be called and is of no means for that StateChangeProcessor
		}

		@Override
		public String getProcessorId() {
			return null;
		}

		@Override
		public StateChangeProcessor<?, ?> getStateChangeProcessor() {
			return this;
		}

		@Override
		public StateChangeProcessorCapabilities getCapabilities() {
			return StateChangeProcessors.beforeOnlyCapabilities();
		}
	}
	
	protected static boolean nullSafeEquals(Object o1, Object o2) {
		return (o1 == null && o2 == null) ||
		(o1 != null && o1.equals(o2));
	}

	@Override
	public GenericEntity getWorkerIdentification() {
		return deployedProcessingEngine;
	}
	
	@Override
	public void start(WorkerContext workerContext) throws WorkerException {
		monitorFuture = workerContext.submit(new Monitor());
	}

	@Override
	public void stop(WorkerContext workerContext) throws WorkerException {
		
		if (monitorFuture != null) {
			logger.debug(() -> "Cancelling monitor due to a worker stop.");
		
			monitorFuture.cancel(true);
			
			monitorFuture = null;
		} else {
			logger.debug(() -> "No monitor future to cancel.");
		}
	}
	
	private class Monitor implements Runnable {
		@Override
		public void run() {
			long monitorIntervalInMillies = getMonitorIntervalInMillies();
			
			while (monitorFuture != null) {
				try {
					// find all accesses which have this processing engine assigned and are therefore relevant to be monitored
					List<IncrementalAccess> accessesToBeMonitored = determineAccessesToBeMonitored();

					// run over all access that where found to be relevant for monitoring in order to check for processes in overdue
					for (IncrementalAccess accessToBeMonitored: accessesToBeMonitored) {
						checkOverdue(accessToBeMonitored);
					}

					Thread.sleep(monitorIntervalInMillies);
					
				} catch (InterruptedException e) {
					// call for stop from the WorkerManager thus lets return without any further efforts
					logger.debug(() -> "ProcessingEngine got interrupted. Stopping operations now.");
					return;
				} catch (Exception e) {
					logger.error("error while checking for overdues processes ", e);
				}
			}
			
			logger.debug(() -> "ProcessingEngine monitor stopped.");
		}
	}

	@Override
	public boolean isSingleton() {
		return true;
	}
	
	private long getMonitorIntervalInMillies() {
		TimeSpan monitorIntervalSpan = deployedProcessingEngine.getMonitorInterval();
		if (monitorIntervalSpan == null) {
			monitorIntervalSpan = TimeSpan.T.create();
			monitorIntervalSpan.setUnit(TimeUnit.minute);
			monitorIntervalSpan.setValue(1);
		}
		
		double monitorIntervalInMillies = TimeSpanConversion
				.fromTimeSpan(monitorIntervalSpan)
				.unit(TimeUnit.milliSecond)
				.toValue();
		return (long)monitorIntervalInMillies;
	}

	private List<IncrementalAccess> determineAccessesToBeMonitored() {
		List<IncrementalAccess> accessesToBeMonitored = Collections.emptyList();
		try {
			PersistenceGmSession cortexSession = cortexSessionProvider.get();
			
			// find Aspects holding us
			// TODO Rework after moving StateProcessing into own extension
			SelectQuery aspectsQuery = new SelectQueryBuilder()
			.from(StateProcessingAspect.class, "stateProcessing")
			.join("stateProcessing", "processors", "processor")
			.where()
				.property("processor", null).eq().entity(deployedProcessingEngine)
			.select("stateProcessing")
			.done();
			
			List<StateProcessingAspect> aspects = cortexSession.query().select(aspectsQuery).list();
			Set<StateProcessingAspect> aspectsSet = new HashSet<StateProcessingAspect>(aspects);
			
			SelectQuery accessQuery = new SelectQueryBuilder()
			.from(IncrementalAccess.class, "access")
			.join("access", "aspectConfiguration", "aspectConfig")
			.join("aspectConfig", "aspects", "aspect")
			.where()
				.conjunction()
					.property("access", IncrementalAccess.deploymentStatus).eq(DeploymentStatus.deployed)
					.property("aspect", null).inEntities(aspectsSet)
					.property("aspect", AccessAspect.deploymentStatus).eq(DeploymentStatus.deployed)
				.close()
			.select("access")
			.done();
			
			accessesToBeMonitored = cortexSession.query().select(accessQuery).list();
		} catch (Exception e) {
			logger.error("error while determining accesses to be monitored for processing engine " + deployedProcessingEngine, e);
		}
		return accessesToBeMonitored;
	}

	private void checkOverdue(IncrementalAccess accessToBeMonitored) {
		try {
			PersistenceGmSession processSession = sessionFactory.newSession(accessToBeMonitored.getExternalId());
			
			Set<ProcessDefinition> processDefinitions = deployedProcessingEngine.getProcessDefinitions();
			
			if (processDefinitions != null) {
				for (ProcessDefinitionInfo processDefinitionInfo: definitionInfos.values()) {
					ProcessDefinition processDefinition = processDefinitionInfo.processDefinition;
					GmProperty gmProperty = processDefinition.getTrigger();
					if (gmProperty != null) {
						GmEntityType gmProcessType = processDefinition.triggerType();
						if (gmProcessType != null) {
							checkOverdue(processSession, processDefinitionInfo, processDefinition, gmProperty, gmProcessType);
						}
					}
					if (Thread.currentThread().isInterrupted())
						return;
				}
			}
		} catch (GmSessionException e) {
			logger.error("error while checking for overdues processes for access " + accessToBeMonitored, e);
		}
	}

	private void checkOverdue(PersistenceGmSession processSession, ProcessDefinitionInfo processDefinitionInfo, ProcessDefinition processDefinition, GmProperty gmProperty,
			GmEntityType gmProcessType) throws GmSessionException {
		while (true) {
			Date now = new Date();
			@SuppressWarnings("unchecked")
			EntityType<Process> processType = (EntityType<Process>) gmProcessType.reflectionType();
			Property property = processType.getProperty(gmProperty.getName());
			
			EntityQuery overdueQuery = EntityQueryBuilder
				.from(gmProcessType.getTypeSignature())
				.where()
					.conjunction()
						.property(Process.overdueAt).lt(now)
						.property(Process.overdueAt).ne(null)
					.close()
				.limit(100)
				.orderBy().property(Process.overdueAt)
				.orderingDirection(OrderingDirection.ascending)
				.done();
				
			
			// max > od < now
			List<Process> processesInOverdue = processSession.query().entities(overdueQuery).list();
			
			// finish when all overdue processes are seen
			if (processesInOverdue.isEmpty())
				return;
			
			for (Process processInOverdue: processesInOverdue) {
				Object state = property.get(processInOverdue);
				Node currentNode = processDefinitionInfo.nodeByState.get(state);
				Node overdueNode = currentNode.getOverdueNode();
				
				// first remove overdue to avoid repetition  
				Date overdueAt = processInOverdue.getOverdueAt();
				processInOverdue.setOverdueAt(null);
				tracer.build(processInOverdue, processSession, processSession.getSessionAuthorization().getUserId(), state)
					.message(String.format("overdue at [%s] was reached", overdueAt))
					.trace(EVENT_OVERDUE_IN_STATE);
				
				// fallback on default overdue node in case of missing node override
				if (overdueNode == null)
					overdueNode = processDefinition.getOverdueNode();
				
				// if the overdue node is the current node an infinit loop would be the consequence therefore we cut off this case
				if (currentNode == overdueNode) {
					tracer.build(processInOverdue, processSession, processSession.getSessionAuthorization().getUserId(), state)
						.message(String.format("overdue node is the same as the node which is in overdue state [%s]", state))
						.warn(EVENT_CYCLIC_OVERDUE_NODE);
				}
				else if (overdueNode != null) {
					property.set(processInOverdue, overdueNode.getState());
					processSession.commit();
				}
				else {
					// add a warning trace about missing overdue node just once
					ProcessTrace lastTrace = processInOverdue.getTrace();
					
					if (lastTrace != null && !lastTrace.getEvent().equals(EVENT_MISSING_OVERDUE_NODE)) {
						tracer.build(processInOverdue, processSession, processSession.getSessionAuthorization().getUserId(), state)
							.message(String.format("no overdue node found in fallback chain while processing process in overdue state [%s]", state))
							.warn(EVENT_MISSING_OVERDUE_NODE);
					}
				}
			}
		}
	}
	
	private ProcessorPreCall buildProcessorPreCallDetails(Deployable deployable, ProcessorKind kind) {
		ProcessorPreCall processorPreCall = primeProcessorDetails(ProcessorPreCall.T.create(), deployable, kind);
		return processorPreCall;
	}
	
	private ProcessorPostCall buildProcessorPostCallDetails(Deployable deployable, ProcessorKind kind, long duration, boolean completed) {
		ProcessorPostCall processorPostCall = primeProcessorDetails(ProcessorPostCall.T.create(), deployable, kind);
		processorPostCall.setDuration(duration);
		processorPostCall.setCompleted(completed);
		return processorPostCall;
	}
	
	private static <D extends ProcessorDetails> D primeProcessorDetails(D processorDetails, Deployable deployable, ProcessorKind kind) {
		processorDetails.setKind(kind);
		return primeHasDeployableInfo(processorDetails, deployable);
	}
	
	private static <D extends HasDeployableInfo> D primeHasDeployableInfo(D deployableInfo, Deployable deployable) {
		deployableInfo.setDeployableExternalId(deployable.getExternalId());
		deployableInfo.setDeployableType(deployable.entityType().getTypeSignature());
		return deployableInfo;
	}
	
	private static <D extends ContinuationDemand> D primeContinuationDemand(D continuationDemand, Deployable deployable, ContinueWithState continueWithState) {
		continuationDemand.setState(convertStateToString(continueWithState.getState()));
		return primeHasDeployableInfo(continuationDemand, deployable);
	}


	private static <P extends Process> void setProcessState(ProcessStateChangeContext<P> context, Object state) throws StateChangeProcessorException {
		String propertyName = context.getEntityProperty().getPropertyName();
		P processEntity = context.getSystemProcessEntity();
		EntityType<P> entityType = processEntity.entityType();
		Property property = entityType.getProperty(propertyName);
		property.set(processEntity, state);
	}

	private static String convertStateToString(Object value) {
		return value == null? null: value.toString();
	}

	private void sendToErrorNode(ProcessStateChangeContext<Process> context, TransitionProcessorCallContext customContext, Node node, HasErrorNode... candidates)
			throws StateChangeProcessorException {
		
		Node errorNode = null;
		
		for (HasErrorNode errorNodeSource: candidates) {
			if (errorNodeSource != null) {
				errorNode = errorNodeSource.getErrorNode();
				if (errorNode != null) 
					break;
			}
		}
		
		
		if (errorNode == node) {
			context.getSystemProcessEntity().setOverdueAt(null);
			
			traceBuilder(context, customContext.getEnteredState())
				.message(String.format("error node is the same as the node with the current state [%s]", customContext.getEnteredState()))
				.warn(EVENT_CYCLIC_ERROR_NODE);
		}
		else if (errorNode != null) {
			// found an error node thus put process in the according state
			setProcessState(context, errorNode.getState());
		}
		else {
			context.getSystemProcessEntity().setOverdueAt(null);
			
			// did not found an error node thus write a process trace about it an keep process in reached state
			traceBuilder(context, customContext.getEnteredState())
				.edge(customContext.getLeftState(), customContext.getEnteredState())
				.message("no error node found in fallback chain while processing error in transition")
				.warn(EVENT_MISSING_ERROR_NODE);
		}
	}
	
	
	
}
