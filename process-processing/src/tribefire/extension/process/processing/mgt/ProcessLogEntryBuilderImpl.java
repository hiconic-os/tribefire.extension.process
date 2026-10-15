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
package tribefire.extension.process.processing.mgt;

import java.util.Date;
import java.util.Optional;
import java.util.function.Consumer;

import com.braintribe.model.generic.session.GmSession;
import com.braintribe.model.meta.data.constraint.MaxLength;
import com.braintribe.model.processing.meta.cmd.builders.EntityMdResolver;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;

import tribefire.extension.process.data.model.log.ProcessLogEntry;
import tribefire.extension.process.data.model.log.ProcessLogEvent;
import tribefire.extension.process.data.model.log.ProcessLogLevel;

public class ProcessLogEntryBuilderImpl implements ProcessLogEntryBuilder {

	private ProcessLogEvent event;
	private GmSession session;
	private String msg;
	private String traceDetails;
	private String tracebackId;
	private ProcessLogLevel level;
	private Date date = new Date();
	private String state;
	private String initiator;
	private String itemId;
	private Consumer<ProcessLogEntry> consumer;
	private int order;

	public ProcessLogEntryBuilderImpl(ProcessLogEvent event) {
		this.event = event;
		this.level = event.defaultLevel();
	}
	
	public ProcessLogEntryBuilderImpl(ProcessLogEvent event, Consumer<ProcessLogEntry> consumer) {
		this.event = event;
		this.consumer = consumer;
		this.level = event.defaultLevel();
	}
	
	public ProcessLogEntryBuilderImpl(GmSession session, ProcessLogEvent event, Consumer<ProcessLogEntry> consumer) {
		this(session, event);
		this.consumer = consumer;
	}
	
	public ProcessLogEntryBuilderImpl(GmSession session, ProcessLogEvent event) {
		this(event);
		this.session = session;
	}
	
	@Override
	public ProcessLogEntryBuilder msg(String msg) {
		this.msg = msg;
		return this;
	}

	@Override
	public ProcessLogEntryBuilder traceDetails(String traceDetails) {
		this.traceDetails = traceDetails;
		return this;
	}

	@Override
	public ProcessLogEntryBuilder tracebackId(String tracebackId) {
		this.tracebackId = tracebackId;
		return this;
	}

	@Override
	public ProcessLogEntryBuilder level(ProcessLogLevel level) {
		this.level = level;
		return this;
	}

	@Override
	public ProcessLogEntryBuilder date(Date date) {
		this.date = date;
		return this;
	}
	
	@Override
	public ProcessLogEntryBuilder order(int order) {
		this.order = order;
		return this;
	}

	@Override
	public ProcessLogEntryBuilder state(String state) {
		this.state = state;
		return this;
	}

	@Override
	public ProcessLogEntryBuilder initiator(String initiator) {
		this.initiator = initiator;
		return this;
	}

	@Override
	public ProcessLogEntryBuilder itemId(String itemId) {
		this.itemId = itemId;
		return this;
	}

	@Override
	public ProcessLogEntry done() {
		ProcessLogEntry entry = session != null? session.create(ProcessLogEntry.T): ProcessLogEntry.T.create();
		entry.setDate(date);
		entry.setEvent(event);
		entry.setLevel(level);
		entry.setItemId(itemId);
		
		if (msg != null)
			entry.setMsg(limit(msg, ProcessLogEntry.msg, 255));

		if (traceDetails != null) {
			String limitedDetails = limit(traceDetails, ProcessLogEntry.traceDetails, 2000);
			entry.setTraceDetails(limitedDetails);
		}

		if (tracebackId != null)
			entry.setTracebackId(limit(tracebackId, ProcessLogEntry.tracebackId, 64));
		
		if (initiator != null)
			entry.setInitiator(initiator);
		
		if (itemId != null)
			entry.setItemId(itemId);
		
		if (state != null)
			entry.setState(state);
		
		entry.setOrder(order);

		if (consumer != null)
			consumer.accept(entry);
		
		return entry;
	}

	private String limit(String value, String property, int fallbackLimit) {
		int maxLength = maxLength(property, fallbackLimit);
		if (maxLength < 0)
			return value;
		if (value.length() <= maxLength)
			return value;
		if (maxLength <= 3)
			return value.substring(0, maxLength);

		int end = maxLength - 3;
		if (end > 0 && end < value.length() && Character.isHighSurrogate(value.charAt(end - 1)))
			end--;
		return value.substring(0, end) + "...";
	}

	private int maxLength(String property, int fallbackLimit) {
		if (!(session instanceof ManagedGmSession))
			return fallbackLimit;

		ManagedGmSession managedSession = (ManagedGmSession) session;
		EntityMdResolver mdResolver = managedSession.getModelAccessory().getCmdResolver().getMetaData().entityType(ProcessLogEntry.T);
		long configured = Optional.ofNullable(mdResolver.property(property).meta(MaxLength.T).exclusive()) //
				.map(MaxLength::getLength) //
				.orElse((long) fallbackLimit);
		return configured > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) configured;
	}

}
