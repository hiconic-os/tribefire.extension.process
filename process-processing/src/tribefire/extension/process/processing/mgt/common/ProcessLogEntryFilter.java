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
package tribefire.extension.process.processing.mgt.common;

import java.util.Date;
import java.util.Set;
import java.util.function.Predicate;

import tribefire.extension.process.api.model.data.ProcessLogFilter;
import tribefire.extension.process.data.model.log.ProcessLogEntry;
import tribefire.extension.process.data.model.log.ProcessLogEvent;
import tribefire.extension.process.data.model.log.ProcessLogLevel;

public class ProcessLogEntryFilter implements Predicate<ProcessLogEntry> {
	private ProcessLogFilter filter;

	public ProcessLogEntryFilter(ProcessLogFilter filter) {
		super();
		this.filter = filter;
	}

	@Override
	public boolean test(ProcessLogEntry e) {
		
		Date after = filter.getAfter();
		
		if (after != null && !e.getDate().after(after))
			return false;
		
		Date before = filter.getBefore();
		
		if (before != null && !e.getDate().before(before))
			return false;
		
		Set<ProcessLogEvent> events = filter.getEvents();
		
		if (!events.isEmpty() && !events.contains(e.getEvent()))
			return false;
		
		Set<ProcessLogLevel> levels = filter.getLevels();
		
		if (!levels.isEmpty() && !levels.contains(e.getLevel()))
			return false;
		
		String messagePattern = filter.getMessagePattern();
		String msg = e.getMsg();
		if (msg == null)
			msg = "";
		
		if (messagePattern != null && !messagePattern.matches(messagePattern))
			return false;
		
		return true;
	}
	
	

}
