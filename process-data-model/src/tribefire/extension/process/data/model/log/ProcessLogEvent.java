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
package tribefire.extension.process.data.model.log;

import com.braintribe.model.generic.base.EnumBase;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.EnumTypes;

public enum ProcessLogEvent implements EnumBase {
	STATE_CHANGED,
	ERROR_TRANSITION,
	OVERDUE_TRANSITION,
	RESTART_TRANSITION,
	INVALID_TRANSITION(ProcessLogLevel.ERROR),
	
	PROCESS_STARTED,
	PROCESS_SUSPENDED,
	PROCESS_RESUMED,
	PROCESS_RECOVERED,
	PROCESS_HALTED,
	PROCESS_ENDED,
	
	NEXT_STATE_SELECTED,
	
	PROCESS_IS_OVERDUE,
	
	CONDITION_MATCHED,
	
	PROCESSOR_EXECUTED,
	ERROR_IN_PROCESSOR(ProcessLogLevel.ERROR),
	
	CONDITION_EVALUATED,
	ERROR_IN_CONDITION(ProcessLogLevel.ERROR),
	
	UNDETERMINED_NEXT_NODE(ProcessLogLevel.ERROR),
	
	INTERNAL_ERROR(ProcessLogLevel.ERROR),
	
	ILLEGAL_TRANSITION_PHASE(ProcessLogLevel.ERROR),
	
	CUSTOM_EVENT;
	
	public static final EnumType T = EnumTypes.T(ProcessLogEvent.class);
	private ProcessLogLevel defaultLevel;
	
	@Override
	public EnumType type() {
		return T;
	}
	
	private ProcessLogEvent(ProcessLogLevel defaultLevel) {
		this.defaultLevel = defaultLevel;
	}
	
	private ProcessLogEvent() {
		this(ProcessLogLevel.INFO);
	}
	
	
	public ProcessLogLevel defaultLevel() {
		return defaultLevel;
	}
}
