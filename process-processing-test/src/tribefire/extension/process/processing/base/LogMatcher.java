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
package tribefire.extension.process.processing.base;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.braintribe.model.generic.reflection.Property;
import com.braintribe.wire.api.util.Lists;

import tribefire.extension.process.api.model.data.ProcessLogFilter;
import tribefire.extension.process.data.model.log.ProcessLogEntry;
import tribefire.extension.process.data.model.log.ProcessLogEvent;
import tribefire.extension.process.processing.mgt.ProcessLogEntryBuilder;
import tribefire.extension.process.processing.mgt.ProcessLogEntryBuilderImpl;
import tribefire.extension.process.processing.mgt.common.ProcessLogEntryFilter;

public class LogMatcher {
	private ProcessLogFilter filter = ProcessLogFilter.T.create();
	private List<ProcessLogEntry> expectedEntries = new ArrayList<>();
	
	private static List<Property> properties = Lists.<String>list(
			ProcessLogEntry.event,
			ProcessLogEntry.msg,
			ProcessLogEntry.level,
			ProcessLogEntry.initiator,
			ProcessLogEntry.itemId,
			ProcessLogEntry.state
		).stream().map(ProcessLogEntry.T::getProperty).collect(Collectors.toList());
	
	public ProcessLogFilter getFilter() {
		return filter;
	}
	
	public ProcessLogEntryBuilder build(ProcessLogEvent event) {
		return new ProcessLogEntryBuilderImpl(event, this::addExpectedEntry);
	}
	
	private void addExpectedEntry(ProcessLogEntry e) {
		expectedEntries.add(e);
		filter.getEvents().add(e.getEvent());
	}
	
	public String validation(List<ProcessLogEntry> entries) {
		
		StringBuilder builder = new StringBuilder();
		
		List<ProcessLogEntry> givenEntries = entries.stream().filter(new ProcessLogEntryFilter(filter)).collect(Collectors.toList());

		int givenSize = givenEntries.size();
		int expectedSize = expectedEntries.size();
		int size = Math.max(givenSize, expectedSize);
		
		List<Function<ProcessLogEntry, Object>> getters = Lists.list(
			ProcessLogEntry::getEvent,
			ProcessLogEntry::getMsg,
			ProcessLogEntry::getLevel,
			ProcessLogEntry::getInitiator,
			ProcessLogEntry::getItemId,
			ProcessLogEntry::getState
		);
		
		for (int i = 0; i < size; i++) {
			ProcessLogEntry expected = findEntry(expectedEntries, i);
			
			ProcessLogEntry given = findEntry(givenEntries, i);

			if (expected == null) {
				builder.append("unexpected actual log entry: " + given + "\n");
				continue;
			}
			else if (given == null) {
				builder.append("missing actual log entry for expected entry: " + expected + "\n");
				continue;
			}
			
		
			for (Property property: properties) {
				if (!validate(expected, given, property)) {
					builder.append("expected log entry [" + expected + "] differs from actual [" + given + "]\n");
					break;
				}
			}
		}
		
		return builder.length() == 0? null: builder.toString();
	}

	private ProcessLogEntry findEntry(List<ProcessLogEntry> entries, int i) {
		if (i < entries.size())
			return entries.get(i);
		
		return null;
	}
	
	private boolean validate(ProcessLogEntry expected, ProcessLogEntry given, Property property) {
		Object expectedValue = property.get(expected);
		
		if (expectedValue == null)
			return true;
		
		Object givenValue = property.get(given);
		
		return expectedValue.equals(givenValue);
	}
}