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
package tribefire.extension.process.model.data.tracing;

import java.util.Date;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.annotation.meta.Priority;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

import tribefire.extension.process.model.data.details.ProcessTraceDetails;

@SelectiveInformation("${event} [${state}]")
public interface ProcessTrace extends GenericEntity {

	EntityType<ProcessTrace> T = EntityTypes.T(ProcessTrace.class);

	String state = "state";
	String fromState = "fromState";
	String toState = "toState";
	String msg = "msg";
	String restart = "restart";
	String exceptionTrace = "exceptionTrace";
	String event = "event";
	String date = "date";
	String kind = "kind";
	String initiator = "initiator";
	String details = "details";

	@Priority(10)
	String getState();
	void setState(String state);
	
	@Priority(9)
	String getFromState();
	void setFromState(String fromState);

	@Priority(8)
	String getToState();
	void setToState(String toState);

	@Priority(7)
	String getMsg();
	void setMsg(String msg);
	
	@Priority(6)
	boolean getRestart();
	void setRestart(boolean restart);
	
	@Priority(5)
	ExceptionTrace getExceptionTrace();
	void setExceptionTrace(ExceptionTrace exceptionTrace);
	
	@Priority(4)
	String getEvent();
	void setEvent(String event);
	
	@Priority(3)
	Date getDate();
	void setDate(Date date);

	@Priority(2)
	TraceKind getKind();
	void setKind(TraceKind kind);
	
	@Priority(1)
	String getInitiator();
	void setInitiator(String initiator);
	
	@Priority(0.9)
	ProcessTraceDetails getDetails();
	void setDetails(ProcessTraceDetails details);
	
}
