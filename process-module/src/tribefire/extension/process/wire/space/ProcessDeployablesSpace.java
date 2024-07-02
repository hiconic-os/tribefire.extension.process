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
package tribefire.extension.process.wire.space;

import com.braintribe.model.processing.deployment.api.ExpertContext;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.extension.process.processing.mgt.MetaDataBasedMonitoredAccessesSupplier;
import tribefire.extension.process.processing.mgt.ProcessManager;
import tribefire.module.wire.contract.TribefireWebPlatformContract;

@Managed
public class ProcessDeployablesSpace implements WireSpace {

	@Import
	private TribefireWebPlatformContract tfPlatform;
	
	@Managed
	public ProcessManager processManager(ExpertContext<tribefire.extension.process.model.deployment.ProcessManager> context) {
		ProcessManager bean = new ProcessManager();
		bean.setDeployedComponentResolver(tfPlatform.deployment().deployedComponentResolver());
		bean.setEvaluator(tfPlatform.systemUserRelated().evaluator());
		bean.setLocking(tfPlatform.cluster().locking());
		bean.setMessagingSessionProvider(tfPlatform.messaging().sessionProvider());
		bean.setSystemSessionFactory(tfPlatform.systemUserRelated().sessionFactory());
		bean.setMonitoredAccessIdsSupplier(monitoredAccessesSupplier(context));
		return bean;
	}
	
	@Managed
	private MetaDataBasedMonitoredAccessesSupplier monitoredAccessesSupplier(ExpertContext<tribefire.extension.process.model.deployment.ProcessManager> context) {
		MetaDataBasedMonitoredAccessesSupplier bean = new MetaDataBasedMonitoredAccessesSupplier();
		bean.setDeployRegistry(tfPlatform.deployment().deployRegistry());
		bean.setExternalIdOfManager(context.getDeployableExternalId());
		bean.setModelAccessoryFactory(tfPlatform.systemUserRelated().modelAccessoryFactory());
		return bean;
	}
	
}
