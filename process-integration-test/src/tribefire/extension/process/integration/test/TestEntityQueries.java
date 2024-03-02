// ============================================================================
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package tribefire.extension.process.integration.test;

import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.query.building.EntityQueries;
import com.braintribe.model.query.EntityQuery;

public class TestEntityQueries extends EntityQueries {
	public static EntityQuery deployableByExternalId(EntityType<? extends Deployable> deploybleType, String externalId) {
		return from(deploybleType).where(eq(Deployable.externalId, externalId));
	}
}
