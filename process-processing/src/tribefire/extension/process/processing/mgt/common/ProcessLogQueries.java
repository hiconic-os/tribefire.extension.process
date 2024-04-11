// ============================================================================
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package tribefire.extension.process.processing.mgt.common;

import java.util.Date;
import java.util.Optional;
import java.util.Set;

import com.braintribe.model.pagination.HasPagination;
import com.braintribe.model.processing.query.building.SelectQueries;
import com.braintribe.model.query.From;
import com.braintribe.model.query.OrderingDirection;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.Source;
import com.braintribe.model.query.conditions.Condition;
import com.braintribe.model.query.conditions.Conjunction;

import tribefire.extension.process.api.model.data.ProcessFilter;
import tribefire.extension.process.api.model.data.ProcessLogFilter;
import tribefire.extension.process.data.model.ProcessItem;
import tribefire.extension.process.data.model.log.ProcessLogEntry;
import tribefire.extension.process.data.model.log.ProcessLogEvent;
import tribefire.extension.process.data.model.log.ProcessLogLevel;
import tribefire.extension.process.data.model.state.ProcessActivity;

public class ProcessLogQueries extends SelectQueries {
	public static SelectQuery processes(ProcessFilter processFilter, boolean descending, HasPagination pagination) {
		From p = source(ProcessItem.T);
		
		Conjunction conditions = Conjunction.T.create();
		addProcessFilterCondition(p, processFilter, conditions);
		
		SelectQuery query = from(p) //
				.where(conditions) //
				.select(p);
		
		if (pagination != null) {
			query = query.paging(pagination.getPageOffset(), pagination.getPageLimit());
		}
		
		OrderingDirection direction = descending ? OrderingDirection.descending : OrderingDirection.ascending;

		query = query.orderBy(direction, property(p, ProcessItem.lastTransit));

		return query;		
	}
	
	public static SelectQuery logEntryIds(ProcessFilter processFilter, ProcessLogFilter logfilter) {
		From p = source(ProcessItem.T);
		From e = source(ProcessLogEntry.T);
		
		Conjunction conditions = Conjunction.T.create();

		// join condition
		conditions.add(eq(property(e, ProcessLogEntry.itemId), property(p, ProcessItem.id)));
		
		addlogFilterCondition(e, logfilter, conditions);
		addProcessFilterCondition(p, processFilter, conditions);

		return from(e) //
			.where(conditions) //
			.select(property(e, ProcessLogEntry.id));
	}
	
	public static SelectQuery logEntryIds(String itemId, ProcessLogFilter filter) {
		From e = source(ProcessLogEntry.T);
		Conjunction conditions = Conjunction.T.create();

		conditions.add(eq(property(e, ProcessLogEntry.itemId), itemId));
		
		addlogFilterCondition(e, filter, conditions);
		
		return from(e) //
				.where(conditions) //
				.select(property(e, ProcessLogEntry.id));
	}
	
	public static SelectQuery logEntries(String itemId, ProcessLogFilter filter, boolean descending, HasPagination pagination) {
		From e = source(ProcessLogEntry.T);
		Conjunction conditions = Conjunction.T.create();

		conditions.add(eq(property(e, ProcessLogEntry.itemId), itemId));
		
		addlogFilterCondition(e, filter, conditions);
		
		SelectQuery query = from(e) //
				.where(conditions);
		
		if (pagination != null) {
			query = query.paging(pagination.getPageOffset(), pagination.getPageLimit());
		}
		
		OrderingDirection direction = descending ? OrderingDirection.descending : OrderingDirection.ascending;

		query = query.orderBy(direction, property(e, ProcessLogEntry.order));

		return query;
	}
	
	
	private static void addProcessFilterCondition(From p, ProcessFilter filter, Conjunction conditions) {
		Set<String> itemIds = filter.getItemIds();
		
		switch (itemIds.size()) {
		case 0:
			break;
		case 1:
			conditions.add(eq(property(p, ProcessItem.id), itemIds.iterator().next()));
			break;
		default:
			conditions.add(in(property(p, ProcessItem.id), itemIds));
			break;
		}
		
		Set<ProcessActivity> activities = filter.getActivities();
		
		switch (activities.size()) {
		case 0:
			break;
		case 1:
			conditions.add(eq(property(p, ProcessItem.activity), activities.iterator().next()));
			break;
		default:
			conditions.add(in(property(p, ProcessItem.activity), activities));
			break;
		}
		
		afterDateCondition(p, filter.getStartedAfter(), ProcessItem.startedAt).ifPresent(conditions::add);
		beforeDateCondition(p, filter.getStartedBefore(), ProcessItem.startedAt).ifPresent(conditions::add);

		afterDateCondition(p, filter.getEndedAfter(), ProcessItem.endedAt).ifPresent(conditions::add);
		beforeDateCondition(p, filter.getEndedBefore(), ProcessItem.endedAt).ifPresent(conditions::add);
		
		afterDateCondition(p, filter.getLastTransitAfter(), ProcessItem.lastTransit).ifPresent(conditions::add);
		beforeDateCondition(p, filter.getLastTransitBefore(), ProcessItem.lastTransit).ifPresent(conditions::add);
	}
	
	private static Optional<Condition> afterDateCondition(Source s, Date date, String propertyName) {
		if (date == null)
			return Optional.empty();
		
		return Optional.of(ge(property(s, propertyName), date));
	}
	
	private static Optional<Condition> beforeDateCondition(Source s, Date date, String propertyName) {
		if (date == null)
			return Optional.empty();
		
		return Optional.of(lt(property(s, propertyName), date));
	}
	
	private static void addlogFilterCondition(From e, ProcessLogFilter filter, Conjunction conditions) {
		Set<ProcessLogEvent> events = filter.getEvents();
		
		if (!events.isEmpty()) {
			conditions.add(in(property(e, ProcessLogEntry.event), events));
		}
		
		Set<ProcessLogLevel> levels = filter.getLevels();
		
		if (!levels.isEmpty()) {
			conditions.add(in(property(e, ProcessLogEntry.level), levels));
		}
		
		String messagePattern = filter.getMessagePattern();
		
		if (messagePattern != null) {
			conditions.add(like(property(e, ProcessLogEntry.msg), messagePattern));
		}
		
		afterDateCondition(e, filter.getAfter(), ProcessLogEntry.date).ifPresent(conditions::add);
		beforeDateCondition(e, filter.getBefore(), ProcessLogEntry.date).ifPresent(conditions::add);
		
		Integer fromOrder = filter.getFromOrder();
		Integer toOrder = filter.getToOrder();
		
		if (fromOrder != null)
			conditions.add(ge(property(e, ProcessLogEntry.order), fromOrder));

		if (toOrder != null)
			conditions.add(le(property(e, ProcessLogEntry.order), toOrder));
	}
}