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

public interface ProcessingEngineConstants {
	static final String EVENT_EDGE_TRANSITION = "edge-transition";
	static final String EVENT_CORRUPTED_PROCESS_STATE = "corrupted-process-state";
	static final String EVENT_ERROR_TRANSITION = "error-transition";
	static final String EVENT_OVERDUE_TRANSITION = "overdue-transition";
	static final String EVENT_RESTART_TRANSITION = "restart-transition";
	
	static final String EVENT_PROCESS_SUSPENDED = "process-suspended";
	static final String EVENT_PROCESS_RESUMED = "process-resumed";
	static final String EVENT_PROCESS_ENDED = "process-ended";
	
	static final String EVENT_RESTART= "restart";
	static final String EVENT_ADMINISTRATIVE_RESTART = "administrative-restart";
	
	static final String EVENT_CONTINUATION_DEMAND = "continuation-demand";
	
	static final String EVENT_INVALID_TRANSITION = "invalid-transition";
	
	static final String EVENT_MISSING_ERROR_NODE = "missing-error-node";
	static final String EVENT_MISSING_OVERDUE_NODE = "missing-overdue-node";
	static final String EVENT_CYCLIC_OVERDUE_NODE = "cyclic-overdue-node";
	static final String EVENT_CYCLIC_ERROR_NODE = "cyclic-error-node";
	
	static final String EVENT_PRECALL_TRANSITION_PROCESSOR = "precall-transition-processor";
	static final String EVENT_PRECALL_CONDITION_PROCESSOR = "precall-condition-processor";
	static final String EVENT_DEFAULT_CONDITION_MATCHED = "default-condition-matched";
	static final String EVENT_CONDITION_MATCHED = "condition-matched";
	
	static final String EVENT_POSTCALL_TRANSITION_PROCESSOR = "postcall-transition-processor";
	static final String EVENT_POSTCALL_CONDITION_PROCESSOR = "postcall-condition-processor";
	
	static final String EVENT_ERROR_IN_TRANSITION_PROCESSOR = "error-in-transition-processor";
	static final String EVENT_ERROR_IN_CONDITION_PROCESSOR = "error-in-condition-processor";
	static final String EVENT_ERROR_AMBIGUOUS_CONTINUATION_DEMAND = "error-ambiguous-continuation-demand";
	
	static final String EVENT_INTERNAL_ERROR = "internal-error";
	static final String EVENT_OVERDUE_IN_STATE = "overdue-in-state";
	
	static final String EVENT_MAX_RESTART = "max-restart-reached";
}
