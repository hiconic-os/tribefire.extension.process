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
package tribefire.extension.process.model.data;

import java.util.Date;
import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

import tribefire.extension.process.model.data.tracing.ProcessTrace;

/**
 * base class for all processes that can be handled by the process engine<br/><br/>
 * 
 * the current {@link ProcessTrace}<br/>
 * a set of {@link ProcessTrace} (all traces of the process)<br/>
 * 
 * 
 * @author Pit, Dirk
 *
 */
@Abstract
public interface Process extends GenericEntity {

	EntityType<Process> T = EntityTypes.T(Process.class);
	
	String lastTransit = "lastTransit";
	String overdueAt = "overdueAt";
	String trace = "trace";
	String traces = "traces";
	String restartCounters = "restartCounters";
	String activity = "activity";

	Date getLastTransit();
	void setLastTransit(Date lastTransit);
	
	Date getOverdueAt();
	void setOverdueAt(Date overdueAt);
	
	ProcessTrace getTrace();
	void setTrace( ProcessTrace trace);
	
	Set<ProcessTrace> getTraces();
	void setTraces( Set<ProcessTrace> traces);
	
	Set<RestartCounter> getRestartCounters();
	void setRestartCounters(Set<RestartCounter> restartCounters);
	
	ProcessActivity getActivity();
	void setActivity(ProcessActivity activity);
	
}
