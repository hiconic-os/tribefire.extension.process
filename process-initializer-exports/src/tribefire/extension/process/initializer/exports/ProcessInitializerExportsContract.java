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
package tribefire.extension.process.initializer.exports;

import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.cortex.initializer.support.impl.lookup.GlobalId;
import tribefire.cortex.initializer.support.impl.lookup.InstanceLookup;

@InstanceLookup
public interface ProcessInitializerExportsContract extends WireSpace {
	
	String CONFIGURED_API_MODEL_NAME = "tribefire.extension.process:configured-process-api-model" ; // TODO: replace by static ArtifactReflection fields access concatenation
	String CONFIGURED_DATA_MODEL_NAME = "tribefire.extension.process:configured-process-data-model"; // TODO: replace by static ArtifactReflection fields access concatenation
	
	String CONFIGURED_API_MODEL_ID = "model:" + CONFIGURED_API_MODEL_NAME;
	String CONFIGURED_DATA_MODEL_ID = "model:" + CONFIGURED_DATA_MODEL_NAME;
	
	@GlobalId(CONFIGURED_API_MODEL_ID)
	GmMetaModel configuredApiModel();
	
	@GlobalId(CONFIGURED_DATA_MODEL_ID)
	GmMetaModel configuredDataModel();
}
