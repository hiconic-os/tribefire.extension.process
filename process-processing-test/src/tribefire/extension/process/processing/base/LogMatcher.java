// ============================================================================
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
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