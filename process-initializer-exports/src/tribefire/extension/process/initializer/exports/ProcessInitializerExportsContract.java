// ============================================================================
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package tribefire.extension.process.initializer.exports;

import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.cortex.initializer.support.impl.lookup.GlobalId;
import tribefire.cortex.initializer.support.impl.lookup.InstanceLookup;

@InstanceLookup
public interface ProcessInitializerExportsContract extends WireSpace {
	
	String CONFIGURED_API_MODEL_NAME = "tribefire.extension.process:configured-process-api-model" ; // TODO: replace by static ArtifactReflection fields access concatenation
	String CONFIGURED_DATA_MODEL_NAME = "tribefire.extension.process:configured-process-model"; // TODO: replace by static ArtifactReflection fields access concatenation
	
	String CONFIGURED_API_MODEL_ID = "model:" + CONFIGURED_API_MODEL_NAME;
	String CONFIGURED_DATA_MODEL_ID = "model:" + CONFIGURED_DATA_MODEL_NAME;
	
	@GlobalId(CONFIGURED_API_MODEL_ID)
	GmMetaModel configuredApiModel();
	
	@GlobalId(CONFIGURED_DATA_MODEL_ID)
	GmMetaModel configuredDataModel();
}
