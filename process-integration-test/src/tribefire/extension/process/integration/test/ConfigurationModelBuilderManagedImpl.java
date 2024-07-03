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
package tribefire.extension.process.integration.test;

import com.braintribe.common.artifact.ArtifactReflection;
import com.braintribe.model.generic.reflection.Model;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;

class ConfigurationModelBuilderManagedImpl {

	private final ManagedGmSession session;
	private final GmMetaModel model;

	/**
	 * @param session
	 *            The {@link ManagedGmSession} for queries and the new {@link GmMetaModel}.
	 * @param modelName
	 *            The name of the new model, its globalId will be `model:modelName`.
	 */
	public ConfigurationModelBuilderManagedImpl(ManagedGmSession session, String modelName, String version) {
		this.session = session;
		this.model = session.create(GmMetaModel.T, Model.modelGlobalId(modelName));
		this.model.setName(modelName);
		this.model.setVersion(version);
	}

	/**
	 * @param session
	 *            The {@link ManagedGmSession} for queries.
	 * @param model
	 *            An existing {@link GmMetaModel}, no new model will be created.
	 */
	public ConfigurationModelBuilderManagedImpl(ManagedGmSession session, GmMetaModel model) {
		this.session = session;
		this.model = model;
	}

	public ConfigurationModelBuilderManagedImpl addDependency(ArtifactReflection standardArtifactReflection) {
		if (!standardArtifactReflection.archetypes().contains("model"))
			throw new IllegalArgumentException("Artifact " + standardArtifactReflection + " is not a model");

		return addDependencyByName(standardArtifactReflection.name());
	}

	public ConfigurationModelBuilderManagedImpl addDependencyByName(String modelName) {
		return addDependencyByGlobalId(Model.modelGlobalId(modelName));
	}

	public ConfigurationModelBuilderManagedImpl addDependency(Model model) {
		return addDependencyByGlobalId(model.globalId());
	}

	public ConfigurationModelBuilderManagedImpl addDependencyByGlobalId(String globalId) {
		return addDependency((GmMetaModel) session.getEntityByGlobalId(globalId));
	}

	public ConfigurationModelBuilderManagedImpl addDependency(GmMetaModel model) {
		this.model.getDependencies().add(model);
		return this;
	}

	public GmMetaModel get() {
		return this.model;
	}

}