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
package tribefire.extension.process.integration.test.pd.wire.contract;

import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.wire.api.space.WireSpace;

public interface IntegrationTestProcessDefinitionRequirementsContract extends WireSpace {
	PersistenceGmSession session();
}
