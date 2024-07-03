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
package tribefire.extension.process.processing.experts;

import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.generic.reflection.EntityType;

public abstract class TestProcessor {
	public abstract Deployable getDeployable();
	public abstract EntityType<? extends Deployable> getComponentType();
	
	public static String externalId(EntityType<? extends Deployable> componentType, Class<?> expertClass) {
		String expertName = expertClass.getSimpleName();
		
		String componentName = componentType.getShortName();
		int size = componentName.length();
		
		StringBuilder builder = new StringBuilder(4 + size);
		
		for (int i = 0; i < size; i++) {
			char c = componentName.charAt(i);
			if (Character.isUpperCase(c))
				builder.append(Character.toLowerCase(c));
		}
		
		builder.append('.');
		builder.append(expertName);
		
		return builder.toString();
	}
	
	public static String name(Class<?> clazz) {
		return clazz.getSimpleName();
	}
}
