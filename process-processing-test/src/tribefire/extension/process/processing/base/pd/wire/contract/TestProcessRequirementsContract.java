// ============================================================================
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package tribefire.extension.process.processing.base.pd.wire.contract;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.extension.process.model.deployment.ConditionProcessor;
import tribefire.extension.process.model.deployment.TransitionProcessor;
import tribefire.pd.editor.api.ProcessDefinitionEditor;

public interface TestProcessRequirementsContract extends WireSpace {

	ProcessDefinitionEditor newProcessEditor();
	
	<T extends GenericEntity> T create(EntityType<T> type);
	
	TransitionProcessor stateTaggingProcessor();

	TransitionProcessor failingByReasonTransitionProcessor();

	TransitionProcessor failingByExceptionTransitionProcessor();

	TransitionProcessor selfRoutingProcessor();

	ConditionProcessor lt1000ConditionProcessor();

	ConditionProcessor lt10000ConditionProcessor();

}
