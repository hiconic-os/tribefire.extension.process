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
