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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import com.braintribe.cfg.Configurable;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.CloningContext;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.generic.reflection.StrategyOnCriterionMatch;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.utils.CompiledFormatter;
import tribefire.extension.process.model.data.Process;

import tribefire.extension.process.api.ProcessTracer;
import tribefire.extension.process.api.TraceBuilder;
import tribefire.extension.process.api.TraceContext;
import tribefire.extension.process.model.data.details.ProcessTraceDetails;
import tribefire.extension.process.model.data.tracing.ExceptionTrace;
import tribefire.extension.process.model.data.tracing.ProcessTrace;
import tribefire.extension.process.model.data.tracing.TraceKind;

/**
 * a generic generator for traces.. 
 * creates traces traces of type {@link ProcessTrace}<br/><br/>
 * Exceptions are wrapped into a {@link ExceptionTrace} and attached to the {@link ProcessTrace}. 
 * 
 * @author pit, dirk
 *
 */
public class BasicProcessTracer implements ProcessTracer {
	private static Logger log = Logger.getLogger(BasicProcessTracer.class);

	public final static String TFRUNTIME_TRACE_EVENT_INCLUDES = "TRIBEFIRE_TRACE_EVENT_INCLUDES";
	public final static String TFRUNTIME_TRACE_EVENT_EXCLUDES = "TRIBEFIRE_TRACE_EVENT_EXCLUDES";
	
	protected CompiledFormatter logMessageFormatter = new CompiledFormatter("%s trace event %s for %s when leaving state [%s] and entering state [%s] with message: %s");
	
	protected static long nextPropertyReload = -1L;
	protected ReentrantLock traceEventSetsLock = new ReentrantLock();
	protected Set<String> traceEventIncludes = Collections.emptySet();
	protected Set<String> traceEventExcludes = Collections.emptySet();
	
	private TracingFilterConfiguration tracingFilterConfiguration;
	
	@Configurable
	public void setTracingFilterConfiguration(TracingFilterConfiguration tracingFilterConfiguration) {
		this.tracingFilterConfiguration = tracingFilterConfiguration;
	}
	/**
	 * record the stack trace of an exception to a string 
	 * @param e - the exception 
	 * @return - the stack trace of the exception
	 */
	private static  String recordException( Throwable e) {
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter) {
			@Override
			public void println() {
				write('\n');
			}
		};
		e.printStackTrace(printWriter);
		return stringWriter.toString(); 
	}
	/**
	 * create a trace entity and add it to the traces .. 
	 * directly commits the created entities to the session.. 
	 * 
	 * @param process The process that the trace should be attached to
	 * @param session - the session used to create the {@link ProcessTrace}
	 * @param initiator - the initiator (who created the trace) 
	 * @param event - the event to write to the  trace  
	 * @param before - the state before (transition)
	 * @param after - the state after (transition) 
	 * @param msg - a text that declares the reason for the trace (may be null) 
	 * @param exception - the exception that needs to be traced (may be null) 
	 * @param loggerContextClass - optionally the logger context class for which a normal log output will be produced that repeats the process trace on volatile log 
	 */
	public  void attachProcessTrace(Process process, final PersistenceGmSession session, String initiator, String event, TraceKind kind, Object state, Object before, Object after, String msg, Throwable exception, ProcessTraceDetails details, Class<?> loggerContextClass, Date now) {

		if (loggerContextClass != null) {
			Logger logger = Logger.getLogger(loggerContextClass);
			
			String loggerMessage = null;
			try {
				loggerMessage = this.logMessageFormatter.format(initiator, event, process, before, after, msg);
			} catch(Exception e) {
				logger.error("Error while using compiled formatter.", e);
				loggerMessage = String.format("%s trace event %s for %s when leaving state [%s] and entering state [%s] with message: %s", initiator, event, process, before, after, msg);
			}
			
			switch (kind) {
			case error:
				logger.error(loggerMessage, exception);
				break;
			case info:
				logger.info(loggerMessage, exception);
				break;
			case trace:
				logger.trace(loggerMessage, exception);
				break;
			case warn:
				logger.warn(loggerMessage, exception);
				break;
			default:
				break;
			}
		}
		
		// Allow to specify environment properties to include/exclude events
		if (!addTrace(event)) {
			return;
		}
		
		// no tracing is possible if the process in not yet created but at least logging was done
		if (process == null)
			return ;
		
		Set<ProcessTrace> traces = process.getTraces();
		if (traces == null) {
			traces = new HashSet<ProcessTrace>();
			process.setTraces(traces);
		}
		
		try {
			ProcessTrace trace = session.create(ProcessTrace.T);
			// essential properties
			trace.setDate(now);		
			trace.setKind(kind);
			trace.setEvent( event);
			trace.setInitiator( initiator);
			
			// optional properties
			if (before != null)
				trace.setFromState(String.valueOf(before));
			
			if (after != null) 
				trace.setToState(String.valueOf(after));
			
			trace.setState(state != null? String.valueOf(state): null);
			
			if (msg != null)
				trace.setMsg( msg);
			
			if (exception != null) {
				ExceptionTrace exceptionTrace = session.create(ExceptionTrace.T);
				
				exceptionTrace.setException(exception.getClass().getName());
				exceptionTrace.setMessage(exception.getMessage());
				exceptionTrace.setStackTrace( recordException( exception));
				trace.setExceptionTrace(exceptionTrace);
			}
			
			if (details != null) {
				CloningContext cloningContext = new StandardCloningContext() {
					@Override
					public GenericEntity supplyRawClone(EntityType<? extends GenericEntity> entityType, GenericEntity instanceToBeCloned) {
						return session.create(entityType);
					}
				};
				details = (ProcessTraceDetails)GMF.getTypeReflection().getBaseType().clone(cloningContext, details, StrategyOnCriterionMatch.skip);
				trace.setDetails(details);
			}
			
			process.setTrace(trace);
			traces.add(trace);
			// commit to session?
			session.commit();
		} catch (GmSessionException e) {
			msg="cannot commit created trace";
			log.error( msg, e);
		}
	}
	
	protected boolean addTrace(String event) {
		
		if (event == null) {
			return true;
		}
		event = event.toLowerCase();
		
		Set<String> includes = traceEventIncludes;
		Set<String> excludes = traceEventExcludes;
		
		long now = System.currentTimeMillis();
		if (now > nextPropertyReload) {
			nextPropertyReload = now + 30000L;
			traceEventSetsLock.lock();
			try {
				if (tracingFilterConfiguration != null) {
					traceEventIncludes = tracingFilterConfiguration.traceEventIncludes();
					traceEventExcludes = tracingFilterConfiguration.traceEventExcludes();

					includes = traceEventIncludes;
					excludes = traceEventExcludes;
				} else {
					includes = Collections.emptySet();
					excludes = Collections.emptySet();
				}
				
			} finally {
				traceEventSetsLock.unlock();
			}
		}
		
		if (!includes.isEmpty()) {
			if (!includes.contains(event)) {
				return false;
			}
		}
		if (!excludes.isEmpty()) {
			if (excludes.contains(event)) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public TraceBuilder build(TraceContext traceContext) {
		return build(traceContext.getProcess(), traceContext.getSession(), traceContext.getInitiator(), traceContext.getState()).loggerContext(traceContext.getLoggerContextClass());
	}
	
	@Override
	public TraceBuilder build(final Process process, final PersistenceGmSession session, final String initiator, final Object currentState) {
		 return new TraceBuilder() {
			private String message;
			private Class<?> loggerContext;
			private Throwable exception;
			private Object leftState;
			private Object enteredState;
			private Object state = currentState;
			private Date date = new Date();
			private ProcessTraceDetails details;
			
			protected void attachProcessTrace(TraceKind traceKind, String event) {
				BasicProcessTracer.this.attachProcessTrace(process, session, initiator, event, traceKind, state, leftState, enteredState, message, exception, details, loggerContext, date);
			}
			
			@Override
			public void warn(String event) {
				attachProcessTrace(TraceKind.warn, event);
			}
			
			@Override
			public void trace(String event) {
				attachProcessTrace(TraceKind.trace, event);
			}
			
			@Override
			public void info(String event) {
				attachProcessTrace(TraceKind.info, event);
			}
			
			@Override
			public void error(String event) {
				attachProcessTrace(TraceKind.error, event);
			}
			
			@Override
			public TraceBuilder message(String message) {
				this.message = message;
				return this;
			}
			
			@Override
			public TraceBuilder loggerContext(Class<?> loggerContextClass) {
				this.loggerContext = loggerContextClass;
				return this;
			}
			
			
			@Override
			public TraceBuilder exception(Throwable throwable) {
				this.exception = throwable;
				return this;
			}
			
			@Override
			public TraceBuilder details(ProcessTraceDetails details) {
				this.details = details;
				return this;
			}
			
			
			@Override
			public TraceBuilder edge(Object leftState, Object enteredState) {
				this.leftState = leftState;
				this.enteredState = enteredState;
				return this;
			}
			
			@Override
			public TraceBuilder state(Object state) {
				this.state = state;
				return this;
			}
			
			@Override
			public TraceBuilder date(Date date) {
				this.date = date;
				return this;
			}
		};
	}
	
	
}
