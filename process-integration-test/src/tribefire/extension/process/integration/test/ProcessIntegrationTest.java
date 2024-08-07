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

import org.junit.BeforeClass;
import org.junit.Test;

import com.braintribe.logging.Logger;
import com.braintribe.model.accessdeployment.aspect.AspectConfiguration;
import com.braintribe.model.accessdeployment.smood.CollaborativeSmoodAccess;
import com.braintribe.model.cortex.aspect.SecurityAspect;
import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.deployment.Module;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.meta.editor.BasicModelMetaDataEditor;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.user.Role;
import com.braintribe.model.user.User;
import com.braintribe.product.rat.imp.ImpApi;
import com.braintribe.testing.internal.tribefire.tests.AbstractTribefireQaTest;
import com.braintribe.utils.DateTools;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

import tribefire.extension.process._ProcessDataModel_;
import tribefire.extension.process._ProcessTestModel_;
import tribefire.extension.process.initializer.exports.ProcessInitializerExportsContract;
import tribefire.extension.process.integration.test.pd.wire.IntegrationTestProcessDefinitionWireModule;
import tribefire.extension.process.integration.test.pd.wire.contract.IntegrationTestProcessDefinitionContract;
import tribefire.extension.process.processing.base.pd.wire.contract.TestProcessRequirementsContract;
import tribefire.extension.process.processing.experts.TestProcessor;

/**
 * checks if all expected deployables are present and deployed, as well as expected demo entities are present
 *
 */
public class ProcessIntegrationTest extends AbstractTribefireQaTest {

	private static final String AUDIT_TEST_CONFIGURATION_MODEL = "tribefire.extension.audit:audit-test-configuration-model";
	private static final String DATA_AUDIT_TEST_CONFIGURATION_MODEL = "tribefire.extension.audit:data-audit-test-configuration-model";
	private static final String DATA_AUDIT_TEST_COMBINED_CONFIGURATION_MODEL = "tribefire.extension.audit:audit-test-combined-configuration-model";
	private static final String DATA_AUDIT_MODEL = "tribefire.extension.audit:data-audit-model";

	private static Logger log = Logger.getLogger(ProcessIntegrationTest.class);

	private static PersistenceGmSession dataSession = null;
	private static PersistenceGmSession auditSession = null;
	private static PersistenceGmSession combinedSession = null;
	private static PersistenceGmSession untrackedSession;

	@BeforeClass
	public static void initialize() throws Exception {

		log.info("Making sure that all expected deployables are there and deployed...");
		
		String uuid = DateTools.getCurrentDateString("yyyyMMddHHmmssSSS");

		ImpApi imp = apiFactory().build();

		Module module = imp.session().query().findEntity("module://tribefire.extension.process:process-module");

		PersistenceGmSession session = imp.session();
		
		try (WireContext<IntegrationTestProcessDefinitionContract> wireContext = Wire.context(new IntegrationTestProcessDefinitionWireModule(session))) {
			
			////////////////////////////////////////////////////////
			// wiring for separate accesses for data and auditing //
			////////////////////////////////////////////////////////
			
			GmMetaModel configuredDataModel = new ConfigurationModelBuilderManagedImpl(session, _ProcessDataModel_.reflection.groupId(), "process-test-data-model-" + uuid) //
				.addDependencyByGlobalId(ProcessInitializerExportsContract.CONFIGURED_DATA_MODEL_ID) //
				.addDependency(_ProcessTestModel_.reflection)
				.get();

			GmMetaModel configuredApiModel = new ConfigurationModelBuilderManagedImpl(session, _ProcessDataModel_.reflection.groupId(), "process-test-api-model-" + uuid)
					.addDependencyByGlobalId(ProcessInitializerExportsContract.CONFIGURED_DATA_MODEL_ID).get();
	
			String dataAccessExternalId = "access.test.process.data-" + uuid;
			String securityExternalId = "access-aspect.test.process.security-" + uuid;
			String aspectConfigurationId = "access-aspect-configuration.test.process-" + uuid;
			
			CollaborativeSmoodAccess dataAccess = session.create(CollaborativeSmoodAccess.T);
			dataAccess.setExternalId(dataAccessExternalId);
			dataAccess.setGlobalId(dataAccessExternalId);
			dataAccess.setMetaModel(configuredDataModel);
			dataAccess.setName("Process Test Data Smood");
			
			SecurityAspect securityAspect = session.create(SecurityAspect.T);
			securityAspect.setName("Security Aspect");
			securityAspect.setExternalId(securityExternalId);
			securityAspect.setGlobalId(securityExternalId);
			
			AspectConfiguration aspectConfiguration = session.create(AspectConfiguration.T);
			aspectConfiguration.setGlobalId(aspectConfigurationId);
			aspectConfiguration.getAspects().add(securityAspect);
			
			dataAccess.setAspectConfiguration(aspectConfiguration);
			
	
			session.commit();
			
			BasicModelMetaDataEditor dataEd = BasicModelMetaDataEditor.create(configuredDataModel).withSession(session).done();
			
			// TODO: configure process definitions on process types
			// dataEd.
			
			session.commit();
			
			TestProcessRequirementsContract processors = wireContext.contract().processors();
	
			imp.deployable(processors.failingByExceptionTransitionProcessor()).redeploy();
			imp.deployable(processors.failingByReasonTransitionProcessor()).redeploy();
			imp.deployable(processors.stateTaggingProcessor()).redeploy();
			imp.deployable(processors.selfRoutingProcessor()).redeploy();
			imp.deployable(processors.lt10000ConditionProcessor()).redeploy();
			imp.deployable(processors.lt1000ConditionProcessor()).redeploy();
			imp.deployable(dataAccess).redeploy();
	
			dataSession = imp.switchToAccess(dataAccessExternalId).session();
		}

		log.info("Test preparation finished successfully!");
	}
	
	private static <D extends Deployable> D acquireDeployable(PersistenceGmSession session, EntityType<D> deployableType, Class<?> expertClass) {
		String externalId = TestProcessor.externalId(deployableType, expertClass);
		D deployable = session.query().entities(TestEntityQueries.deployableByExternalId(deployableType, externalId)).first();
		
		if (deployable == null) {
			deployable = session.create(deployableType);
			deployable.setExternalId(externalId);
			deployable.setName(TestProcessor.name(expertClass));
		}
		
		return deployable;
	}
	
	
	@Test
	public void testCollectionsAndNonCollections() throws Exception {
		User user = dataSession.create(User.T);
		Role role1 = dataSession.create(Role.T);
		user.getRoles().add(role1);
		
		dataSession.commit();
	}
}