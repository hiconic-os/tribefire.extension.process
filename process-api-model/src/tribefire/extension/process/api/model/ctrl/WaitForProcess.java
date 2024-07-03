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
package tribefire.extension.process.api.model.ctrl;

import java.util.stream.Stream;

import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.time.TimeSpan;

import tribefire.extension.process.api.model.ProcessRequest;
import tribefire.extension.process.api.model.data.ProcessAndActivities;
import tribefire.extension.process.data.model.ProcessItem;
import tribefire.extension.process.data.model.state.ProcessActivity;

/**
 * WaitForProcess will wait for the identified {@link ProcessItem} to reach any of the 
 * @author Dirk Scheffler
 *
 */
public interface WaitForProcess extends ProcessRequest, ProcessAndActivities {
	EntityType<WaitForProcess> T = EntityTypes.T(WaitForProcess.class);
	
	String maxWait = "maxWait";
	
	TimeSpan getMaxWait();
	void setMaxWait(TimeSpan maxWait);
	
	static WaitForProcess create(ProcessItem item, ProcessActivity... activities) {
		WaitForProcess waitForProcess = WaitForProcess.T.create();
		Stream.of(activities).forEach(waitForProcess.getActivities()::add);
		waitForProcess.item(item);
		return waitForProcess;
	}
	
	@Override
	EvalContext<ProcessActivity> eval(Evaluator<ServiceRequest> evaluator);
}
