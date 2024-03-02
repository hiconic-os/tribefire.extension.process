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

import java.util.Set;
import java.util.stream.Stream;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

import tribefire.extension.process.data.model.ProcessItem;
import tribefire.extension.process.data.model.state.ProcessActivity;

public interface ProcessAndActivities extends ProcessIdentification {
	EntityType<ProcessAndActivities> T = EntityTypes.T(ProcessAndActivities.class);

	String activities = "activities";

	Set<ProcessActivity> getActivities();
	void setActivities(Set<ProcessActivity> activities);
	
	static ProcessAndActivities create(ProcessItem process, ProcessActivity... activities) {
		ProcessAndActivities processAndActivities = ProcessAndActivities.T.create();
		processAndActivities.setItemType(process.entityType().getTypeSignature());
		processAndActivities.setItemId((String)process.getId());
		Stream.of(activities).forEach(processAndActivities.getActivities()::add);
		return processAndActivities;
	}
}
