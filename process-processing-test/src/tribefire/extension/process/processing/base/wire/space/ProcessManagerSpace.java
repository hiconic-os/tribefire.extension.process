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
// ============================================================================
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package tribefire.extension.process.processing.base.wire.space;

import static com.braintribe.wire.api.util.Lists.list;

import java.util.List;

import com.braintribe.gm.service.access.wire.common.contract.CommonAccessProcessingContract;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.scope.InstanceConfiguration;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.extension.process.processing.base.ProcessProcessingTestCommons;
import tribefire.extension.process.processing.base.wire.contract.ProcessProcessingTestConfigurationContract;
import tribefire.extension.process.processing.base.wire.contract.TestPlatformContract;
import tribefire.extension.process.processing.mgt.ProcessManager;

@Managed
public class ProcessManagerSpace implements WireSpace {
	
	@Import
	private TestPlatformContract testPlatform;
	
	@Import
	private CommonAccessProcessingContract commonAccessProcessing;
	
	@Import
	private ProcessProcessingTestConfigurationContract processingTestConfiguration;
	
	@Managed
	public ProcessManager processManager() {
		ProcessManager bean = new ProcessManager();
		bean.setDeployedComponentResolver(testPlatform.deployedComponentResolver());
		bean.setEvaluator(testPlatform.systemServiceRequestEvaluator());
		bean.setLocking(testPlatform.locking());
		bean.setMessagingSessionProvider(testPlatform.messagingSessionProvider());
		bean.setSystemSessionFactory(commonAccessProcessing.sessionFactory());
		bean.setMonitoredAccessIdsSupplier(this::monitoredAccessIds);
		bean.setTaskScheduler(testPlatform.taskScheduler());

		if (!processingTestConfiguration.suppressRevivalMonitor()) {
			InstanceConfiguration currentInstance = InstanceConfiguration.currentInstance();
			
			testPlatform.workerManager().deploy(bean);
			currentInstance.onDestroy(() -> testPlatform.workerManager().undeploy(bean));
		}

		return bean;
	}
	
	@Managed
	private List<String> monitoredAccessIds() {
		return list(ProcessProcessingTestCommons.ACCESS_ID_PROCESSES);
	}

}
