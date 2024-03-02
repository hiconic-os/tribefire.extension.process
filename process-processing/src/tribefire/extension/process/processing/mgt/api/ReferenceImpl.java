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

import java.util.Objects;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;

class ReferenceImpl<E extends GenericEntity> implements Reference<E> {
	private Object id;
	private EntityType<E> type;
	
	public ReferenceImpl(EntityType<E> type, Object id) {
		super();
		this.id = Objects.requireNonNull(id, () -> "Argument id must not be null");
		this.type = type;
	}
	
	@Override
	public <I> I id() {
		return (I)id;
	}
	
	@Override
	public EntityType<E> type() {
		return type;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, type);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Reference<?> other = (Reference<?>) obj;
		return Objects.equals(id, other.id()) && Objects.equals(type, other.type());
	}
	
	@Override
	public int compareTo(Reference<?> o) {
		EntityType<?> et1 = type();
		EntityType<?> et2 = o.type();
		
		int res = et1.compareTo(et2);
		
		if (res != 0)
			return res;
		
		Comparable<Object> c1 = id();
		Comparable<Object> c2 = o.id();
		
		return c1.compareTo(c2);
	}
	
	@Override
	public String toString() {
		return type.getTypeSignature() + "[id=" + id + "]";
	}
}