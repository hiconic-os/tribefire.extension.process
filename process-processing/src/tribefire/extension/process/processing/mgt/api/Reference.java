// ============================================================================
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package tribefire.extension.process.processing.mgt.api;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;

public interface Reference<E extends GenericEntity> extends Comparable<Reference<?>> {
	<I> I id();
	EntityType<E> type();
	
	static <T extends GenericEntity> Reference<T> of(T entity) {
		return new ReferenceImpl<>(entity.entityType(), entity.getId());
	}
	
	static <T extends GenericEntity> Reference<T> of(EntityType<T> type, Object id) {
		return new ReferenceImpl<>(type, id);
	}
}