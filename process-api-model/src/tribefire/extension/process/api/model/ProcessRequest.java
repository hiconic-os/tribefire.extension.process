// ============================================================================
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package tribefire.extension.process.api.model;

import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

import tribefire.extension.process.api.model.data.ProcessIdentification;
import tribefire.extension.process.data.model.ProcessItem;

/**
 * Base request type for the *process manager* that relates to a specific {@link ProcessItem} by its type and id.
 * @author Dirk Scheffler
 *
 */
@Abstract
public interface ProcessRequest extends ProcessManagerRequest, ProcessIdentification {
	EntityType<ProcessRequest> T = EntityTypes.T(ProcessRequest.class);
	
	default ProcessRequest item(ProcessItem process) {
		setItemType(process.entityType().getTypeSignature());
		setItemId((String)process.getId());
		return this;
	}
}
