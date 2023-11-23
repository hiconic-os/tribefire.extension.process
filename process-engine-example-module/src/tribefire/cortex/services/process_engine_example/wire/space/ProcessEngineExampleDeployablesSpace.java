// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
// 
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package tribefire.cortex.services.process_engine_example.wire.space;

import java.io.File;

import com.braintribe.cartridge.common.processing.session.AccessIdBasedPersistenceGmSessionFactory;
import com.braintribe.model.goofydeployment.GoofyOutputer;
import com.braintribe.model.goofydeployment.GoofyWatcher;
import com.braintribe.model.processing.deployment.api.ExpertContext;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.cortex.services.process_engine_example.input.FolderWatcher;
import tribefire.cortex.services.process_engine_example.input.GoofyInputReceiver;
import tribefire.cortex.services.process_engine_example.processor.ClearanceCheckerProcessor;
import tribefire.cortex.services.process_engine_example.processor.ClearedCheckerProcessor;
import tribefire.cortex.services.process_engine_example.processor.DecodeProcessor;
import tribefire.cortex.services.process_engine_example.processor.ErrorCreatingProcessor;
import tribefire.cortex.services.process_engine_example.processor.HashProcessor;
import tribefire.cortex.services.process_engine_example.processor.OutputProcessor;
import tribefire.cortex.services.process_engine_example.processor.ValidateProcessor;
import tribefire.module.wire.contract.ModuleResourcesContract;
import tribefire.module.wire.contract.TribefireWebPlatformContract;

@Managed
public class ProcessEngineExampleDeployablesSpace implements WireSpace {

	@Import
	private TribefireWebPlatformContract tfPlaform;
	
	@Import
	private ModuleResourcesContract resources;

	@Managed
	public GoofyInputReceiver goofyInputReciever(ExpertContext<GoofyWatcher> context) {
		GoofyInputReceiver bean = new GoofyInputReceiver();
		bean.setSessionProvider(sessionProvider(context));

		return bean;
	}
	
	@Managed
	private AccessIdBasedPersistenceGmSessionFactory sessionProvider(ExpertContext<GoofyWatcher> context) {
		GoofyWatcher deployable = context.getDeployable();
		
		AccessIdBasedPersistenceGmSessionFactory bean = new AccessIdBasedPersistenceGmSessionFactory();
		bean.setAccessId(deployable.getAccess().getExternalId());
		bean.setSessionFactory(tfPlaform.requestUserRelated().sessionFactory());

		return bean;
	}

	@Managed
	public FolderWatcher goofyWatcher(ExpertContext<GoofyWatcher> context) {
		GoofyWatcher denotationType = context.getDeployable();
		
		FolderWatcher bean = new FolderWatcher();
		File watchedFileDirectory = resources.resource(denotationType.getWatchedFileDirectory()).asFile();
		bean.setDirectory(watchedFileDirectory);
		bean.setWorkerIdentification(denotationType);
		bean.setFileReceiver(goofyInputReciever(context));

		return bean;
	}

	@Managed
	public DecodeProcessor goofyDecoder() {
		return new DecodeProcessor();
	}

	@Managed
	public ErrorCreatingProcessor goofyErrorProducer() {
		return new ErrorCreatingProcessor();
	}

	@Managed
	public ClearanceCheckerProcessor goofyClearanceChecker() {
		return new ClearanceCheckerProcessor();
	}

	@Managed
	public ClearedCheckerProcessor goofyClearedChecker() {
		return new ClearedCheckerProcessor();
	}

	@Managed
	public HashProcessor goofyHashProcessor() {
		return new HashProcessor();
	}

	@Managed
	public ValidateProcessor goofyValidateProcessor() {
		return new ValidateProcessor();
	}

	@Managed
	public OutputProcessor goofyOutputer(ExpertContext<GoofyOutputer> context) {
		GoofyOutputer denotationType = context.getDeployable();
		
		OutputProcessor bean = new OutputProcessor();
		String outputDirName = denotationType.getOutputFileDirectory();
		File outputDirectory = null;
		if (outputDirName != null) {
			outputDirectory = resources.resource(outputDirName).asFile();
		}
		bean.setOutputDirectory(outputDirectory);
		
		return bean;
	}
}
