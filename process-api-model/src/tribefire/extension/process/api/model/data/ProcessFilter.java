// ============================================================================
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package tribefire.extension.process.api.model.data;

import java.util.Date;
import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

import tribefire.extension.process.data.model.state.ProcessActivity;

public interface ProcessFilter extends GenericEntity {
	EntityType<ProcessFilter> T = EntityTypes.T(ProcessFilter.class);
	
	String itemIds = "itemIds";
	String activities = "activities";
	String lastTransitBefore = "lastTransitBefore";
	String lastTransitAfter = "lastTransitAfter";
	String startedBefore = "startedBefore";
	String startedAfter = "startedAfter";
	String endedBefore = "endedBefore";
	String endedAfter = "endedAfter";
	
	Set<String> getItemIds();
	void setItemIds(Set<String> itemIds);
	
	Set<ProcessActivity> getActivities();
	void setActivities(Set<ProcessActivity> activities);
	
	Date getLastTransitBefore();
	void setLastTransitBefore(Date lastTransitBefore);
	
	Date getLastTransitAfter();
	void setLastTransitAfter(Date lastTransitAfter);
	
	Date getStartedBefore();
	void setStartedBefore(Date startedBefore);
	
	Date getStartedAfter();
	void setStartedAfter(Date startedAfter);
	
	Date getEndedBefore();
	void setEndedBefore(Date endedBefore);
	
	Date getEndedAfter();
	void setEndedAfter(Date endedAfter);
}