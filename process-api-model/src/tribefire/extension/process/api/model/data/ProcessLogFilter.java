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
import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

import tribefire.extension.process.data.model.log.ProcessLogEvent;
import tribefire.extension.process.data.model.log.ProcessLogLevel;

public interface ProcessLogFilter extends GenericEntity {
	EntityType<ProcessLogFilter> T = EntityTypes.T(ProcessLogFilter.class);
	
	String levels = "levels";
	String events = "events";
	String after = "after";
	String before = "before";
	String messagePattern = "messagePattern";
	String fromOrder = "fromOrder";
	String toOrder ="toOrder";
	
	Set<ProcessLogLevel> getLevels();
	void setLevels(Set<ProcessLogLevel> levels);
	
	Set<ProcessLogEvent> getEvents();
	void setEvents(Set<ProcessLogEvent> levels);
	
	Date getAfter();
	void setAfter(Date date);
	
	Date getBefore();
	void setBefore(Date date);
	
	Integer getFromOrder();
	void setFromOrder(Integer fromOrder);

	Integer getToOrder();
	void setToOrder(Integer toOrder);
	
	String getMessagePattern();
	void setMessagePattern(String messagePattern);
}
