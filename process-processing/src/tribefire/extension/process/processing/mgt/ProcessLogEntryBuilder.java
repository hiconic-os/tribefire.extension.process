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

import tribefire.extension.process.data.model.log.ProcessLogEntry;
import tribefire.extension.process.data.model.log.ProcessLogLevel;

public interface ProcessLogEntryBuilder {
	ProcessLogEntryBuilder msg(String msg);
	ProcessLogEntryBuilder level(ProcessLogLevel level);
	ProcessLogEntryBuilder date(Date date);
	ProcessLogEntryBuilder state(String state);
	ProcessLogEntryBuilder initiator(String initiator);
	ProcessLogEntryBuilder itemId(String itemId);
	ProcessLogEntryBuilder order(int order);
	ProcessLogEntry done();
}
