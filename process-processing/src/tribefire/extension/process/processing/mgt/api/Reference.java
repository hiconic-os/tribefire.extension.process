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