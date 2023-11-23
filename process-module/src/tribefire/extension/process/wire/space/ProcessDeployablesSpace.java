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
package tribefire.extension.process.wire.space;

import com.braintribe.model.processing.deployment.api.ExpertContext;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.extension.process.processing.BasicProcessTracer;
import tribefire.extension.process.processing.ProcessingEngine;
import tribefire.extension.process.wire.contract.TracingFilterConfigurationContract;
import tribefire.module.wire.contract.TribefireWebPlatformContract;

@Managed
public class ProcessDeployablesSpace implements WireSpace {

	@Import
	private TracingFilterConfigurationContract tracingFilterConfiguration;
	
	@Import
	private TribefireWebPlatformContract tfPlatform;
	
	@Managed
	private BasicProcessTracer processTracer() {
		BasicProcessTracer bean = new BasicProcessTracer();
		bean.setTracingFilterConfiguration(tracingFilterConfiguration);
		return bean;
	}
	
	@Managed
	public ProcessingEngine processingEngine(ExpertContext<tribefire.extension.process.model.deployment.ProcessingEngine> context) {
		ProcessingEngine bean = new ProcessingEngine();
		bean.setCortexSessionSupplier(tfPlatform.systemUserRelated().cortexSessionSupplier());
		bean.setDeployedComponentResolver(tfPlatform.deployment().deployedComponentResolver());
		bean.setDeployedProcessingEngine(context.getDeployable());
		bean.setSessionFactory(tfPlatform.systemUserRelated().sessionFactory());
		bean.setTracer(processTracer());
		return bean;
		
	}
	
}
