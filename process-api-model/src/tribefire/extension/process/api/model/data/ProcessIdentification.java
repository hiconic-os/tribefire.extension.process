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
