// ============================================================================
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package tribefire.extension.process.data.model.log;

import java.util.Date;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.annotation.ToStringInformation;
import com.braintribe.model.generic.annotation.meta.Indexed;
import com.braintribe.model.generic.annotation.meta.MaxLength;
import com.braintribe.model.generic.annotation.meta.Priority;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

import tribefire.extension.process.data.model.ProcessItem;

/**
 * ProcessLogEnty is used to write a persisted protocol of the different events that are happening to a {@link ProcessItem}.
 * It is modeled using only scalar properties in order to keep it very simple and can therefore be persisted and deleted in a
 * fast and uncomplicated way.
 * 
 * @author Dirk Scheffler
 */
@SelectiveInformation("${event} [${state}]")
@ToStringInformation("${date} ${event} at [${state}]: ${msg}")
public interface ProcessLogEntry extends GenericEntity {

	EntityType<ProcessLogEntry> T = EntityTypes.T(ProcessLogEntry.class);

	String itemId = "itemId";
	String state = "state";
	String msg = "msg";
	String event = "event";
	String date = "date";
	String order = "order";
	String level = "level";
	String initiator = "initiator";
	String traceDetails = "traceDetails";

	/**
	 * A weakly coupled reference to the {@link ProcessItem#getId()} for which this entry was recorded. It is weekly coupled in order
	 * to simplify deletion and work around typical problems of mapping process polymorphisms in databases (e.g SQL)
	 * @return
	 */
	@Indexed
	String getItemId();
	void setItemId(String itemId);
	
	/**
	 * The {@link ProcessItem#getState() state} the process was in when this entry was recorded
	 * @return
	 */
	@Priority(10)
	String getState();
	void setState(String state);

	/**
	 * An additional text message which gives a more detailed and human readable information accompanying {@link #getEvent()} 
	 * @return
	 */
	@Priority(8)
	@MaxLength(255)
	String getMsg();
	void setMsg(String msg);
	
	@MaxLength(2000)
	String getTraceDetails();
	void setTraceDetails(String traceDetails);
	
	/**
	 * A classification of that entry that is used to differentiate it with well known enum constants 
	 * @return
	 */
	@Priority(7)
	ProcessLogEvent getEvent();
	void setEvent(ProcessLogEvent event);
	
	/**
	 * The date at which this entry was recorded
	 * @return
	 */
	@Priority(6)
	Date getDate();
	void setDate(Date date);
	
	/**
	 * An order number that is used to sort a number of entries related to a process in the order of their recording as the {@link #getDate()} was not precise enough for that purpose.
	 * @return
	 */
	int getOrder();
	void setOrder(int order);

	/**
	 * Classifies the entry if it either just informative or has problematic aspect (e.g error or warning) 
	 * @return
	 */
	@Priority(5)
	ProcessLogLevel getLevel();
	void setLevel(ProcessLogLevel level);
	
	/**
	 * The user that is associated with the recording
	 * @return
	 */
	@Priority(4)
	String getInitiator();
	void setInitiator(String initiator);
}
