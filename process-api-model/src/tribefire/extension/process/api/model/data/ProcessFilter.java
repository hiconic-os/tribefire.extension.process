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
