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