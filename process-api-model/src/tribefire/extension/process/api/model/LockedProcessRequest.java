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
package tribefire.extension.process.api.model;

import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

import tribefire.extension.process.data.model.ProcessItem;

/**
 * Base request type for the *process manager* that relates to a specific {@link ProcessItem} by its type and id.
 * @author Dirk Scheffler
 *
 */
@Abstract
public interface LockedProcessRequest extends ProcessRequest {
	EntityType<LockedProcessRequest> T = EntityTypes.T(LockedProcessRequest.class);

	String reentranceLockId = "reentranceLockId";
	
	/**
	 * The id of an existing lock for reentrant distributed locking
	 */
	String getReentranceLockId();
	void setReentranceLockId(String reentranceLockId);
}
