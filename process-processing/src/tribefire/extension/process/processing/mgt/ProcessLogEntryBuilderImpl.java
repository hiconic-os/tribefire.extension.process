// ============================================================================
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package tribefire.extension.process.processing.mgt;

import java.util.Date;
import java.util.function.Consumer;

import com.braintribe.model.generic.session.GmSession;

import tribefire.extension.process.data.model.log.ProcessLogEntry;
import tribefire.extension.process.data.model.log.ProcessLogEvent;
import tribefire.extension.process.data.model.log.ProcessLogLevel;

public class ProcessLogEntryBuilderImpl implements ProcessLogEntryBuilder {

	private ProcessLogEvent event;
	private GmSession session;
	private String msg;
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
		// TODO: trim total message to maximum possible length (discover by MaxLength MD) using "..."
		this.msg = msg;
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
			entry.setMsg(msg);
		
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

}
