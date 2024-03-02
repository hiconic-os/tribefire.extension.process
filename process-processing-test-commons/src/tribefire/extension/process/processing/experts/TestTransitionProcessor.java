// ============================================================================
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package tribefire.extension.process.processing.experts;

import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.generic.reflection.EntityType;

import tribefire.extension.process.api.TransitionProcessor;
import tribefire.extension.process.test.model.TestProcess;

public abstract class TestTransitionProcessor<T extends TestProcess> extends TestProcessor implements TransitionProcessor<T> {

	private tribefire.extension.process.model.deployment.TransitionProcessor deployable;
	
	protected String getIdentifier() {
		return getClass().getSimpleName();
	}
	
	public tribefire.extension.process.model.deployment.TransitionProcessor getDeployable() {
		if (deployable == null) {
			deployable = tribefire.extension.process.model.deployment.TransitionProcessor.T.create();
			String identifier = getIdentifier();
			deployable.setExternalId("tp." + identifier);
			deployable.setName(identifier);
		}
		return deployable;
	}
	
	@Override
	public EntityType<? extends Deployable> getComponentType() {
		return tribefire.extension.process.model.deployment.TransitionProcessor.T;
	}
}
