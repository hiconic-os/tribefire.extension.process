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
// ============================================================================
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package tribefire.extension.process.data.model;

import java.util.Date;

import com.braintribe.model.generic.StandardStringIdentifiable;
import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.annotation.meta.Indexed;
import com.braintribe.model.generic.annotation.meta.Unmodifiable;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

import tribefire.extension.process.data.model.log.ProcessLogEntry;
import tribefire.extension.process.data.model.state.ProcessActivity;
import tribefire.extension.process.data.model.state.ProcessControl;
import tribefire.extension.process.data.model.state.TransitionPhase;

/**
 * <p>
 * Abstract base type for all <b>modeled processes</b>.</h1>
 * 
 * <p>
 * A modeled process managed with the <b>process api</b> and handled by the <b>process manager</b> which routes it through a state graph defined by a <b>process definition</b>.
 * The routing from one state to another state given by nodes in the state graph involves
 * the execution of associated <b>transition processors</b> that can modify and influence the routing and <b>condition processors</b> that influence the routing.
 * 
 * <p>
 * The <b>process definition</b>, <b>process manager</b>, <b>transition processor</b>, <b>condition processor</b> are declared in the <i>tribefire.extension.process:process-deployment-model</i>.
 * The <b>process manager</b> expert implementation is bound by <i>tribefire.extension.process:process-module</i>. 
 * The <b>process manager</b> is instantiated and configured with <i>tribefire.extension.process:process-initializer</i>.
 * 
 * <p>
 * The <b>process api</b> is declared in the <i>tribefire.extension.process:process-api-model.</i>
 * 
 * <p>  
 * The properties of this type are for used for the following control purposes:
 * 
 * <ul>
 * 		<li><b>routing and transitioning</b>
 * 			<ul>
 * 				<li>{@link #getState()}
 * 				<li>{@link #getPreviousState()}
 * 				<li>{@link #getNextState()}
 * 				<li>{@link #getActivity()}
 * 				<li>{@link #getTransitionPhase()}
 * 				<li>{@link #getTransitionProcessorId()}
 * 				<li>{@link #getOverdueAt()}
 * 			</ul>
 * 		</li>
 * 		<li><b>lifecycle</b>
 * 			<ul>
 * 				<li>{@link #getStartedAt()}
 * 				<li>{@link #getLastTransit()}
 * 				<li>{@link #getEndedAt()}
 * 			</ul>
 * 		</li>
 * 		<li>*persisted logging*
 * 			<ul>
 * 				<li>{@link #getLogSequence()}
 * 			</ul>
 * 		</li>
 * </ul>
 * 
 * For each {@link ProcessItem} there associated {@link ProcessLogEntry log entries} that are loosely coupled by {@link ProcessLogEntry#getItemId()} in order
 * to work around the weak polymorphism support in certain databases (e.g SQL) and keep the connection between them very agile for fast deletion of either
 * the process or the log entries.
 * 
 * @author Dirk Scheffler
 */
@Abstract
public interface ProcessItem extends ProcessControl, StandardStringIdentifiable {
	EntityType<ProcessItem> T = EntityTypes.T(ProcessItem.class);
	
	String startedAt = "startedAt";
	String initiator = "initiator";
	String lastTransit = "lastTransit";
	String endedAt = "endedAt";
	String overdueAt = "overdueAt";
	String logSequence = "logSequence";

	/**
	 * The date at which the process was started.
	 */
	@Unmodifiable
	Date getStartedAt();
	void setStartedAt(Date startedAt);
	
	/**
	 * The initiator of the process.
	 */
	@Unmodifiable
	String getInitiator();
	void setInitiator(String initiator);

	/**
	 * The date of the last transition handling given by state changes, processor executions
	 */
	@Indexed
	@Unmodifiable
	Date getLastTransit();
	void setLastTransit(Date lastTransit);
	
	/**
	 * The date when the process came to an end in a drain nodes of the state graph
	 */
	@Unmodifiable
	Date getEndedAt();
	void setEndedAt(Date endedAt);
	
	/**
	 * A sequence used to give {@link ProcessLogEntry#getOrder() ProcessLogEntries} a well defined order as the {@link ProcessLogEntry#getDate()} is
	 * not precise enough to represent that order. 
	 */
	@Unmodifiable
	Integer getLogSequence();
	void setLogSequence(Integer logSequence);
	
	default int logSequence() {
		Integer seq = getLogSequence();
		if (seq == null)
			seq = 0;
		
		return seq;
	}
	
	@Override @Unmodifiable ProcessActivity getActivity();
	@Override @Unmodifiable String getState();
	@Override @Unmodifiable String getPreviousState();
	@Override String getNextState();
	@Override @Unmodifiable TransitionPhase getTransitionPhase();
	@Override @Unmodifiable String getTransitionProcessorId();
	
	@Override
	default String asString() {
		return entityType().getShortName() + "[" + getId() + "]";
	}
}
