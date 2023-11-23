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
package tribefire.extension.process.api;

import java.util.Date;
import tribefire.extension.process.model.data.details.ProcessTraceDetails;

public interface TraceBuilder {
	TraceBuilder message(String message);
	TraceBuilder exception(Throwable throwable);
	TraceBuilder edge(Object leftState, Object enteredState);
	TraceBuilder loggerContext(Class<?> loggerContextClass);
	TraceBuilder date(Date date);
	TraceBuilder state(Object state);
	TraceBuilder details(ProcessTraceDetails details);
	
	void error(String event);
	void warn(String event);
	void info(String event);
	void trace(String event);
}
