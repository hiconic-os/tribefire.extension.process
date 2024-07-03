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

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

import tribefire.extension.process.data.model.ProcessItem;

/**
 * Identifies a {@link ProcessItem} by its type and id
 * @author Dirk Scheffler
 *
 */
public interface ProcessIdentification extends GenericEntity {
	EntityType<ProcessIdentification> T = EntityTypes.T(ProcessIdentification.class);
	
	String itemType = "itemType";
	String itemId = "itemId";
	
	/**
	 * The short of full signature of the type of the {@link ProcessItem} to be identified  
	 * @return
	 */
	@Mandatory
	String getItemType();
	void setItemType(String itemType);
	
	/**
	 * The {@link ProcessItem#getId() persistence id} of the {@link ProcessItem} to be identified
	 * @return
	 */
	@Mandatory
	String getItemId();
	void setItemId(String itemId);
	
	static ProcessIdentification create(ProcessItem process) {
		ProcessIdentification pi = ProcessIdentification.T.create();
		pi.setItemType(process.entityType().getTypeSignature());
		pi.setItemId((String)process.getId());
		return pi;
	}
}
