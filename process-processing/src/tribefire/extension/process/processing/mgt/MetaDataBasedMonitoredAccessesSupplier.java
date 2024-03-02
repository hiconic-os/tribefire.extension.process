// ============================================================================
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package tribefire.extension.process.processing.mgt;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import com.braintribe.cfg.LifecycleAware;
import com.braintribe.cfg.Required;
import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.extensiondeployment.ServiceProcessor;
import com.braintribe.model.extensiondeployment.meta.ProcessWith;
import com.braintribe.model.processing.deployment.api.DeployRegistry;
import com.braintribe.model.processing.deployment.api.DeployRegistryListener;
import com.braintribe.model.processing.deployment.api.DeployedUnit;
import com.braintribe.model.processing.session.api.managed.ModelAccessory;
import com.braintribe.model.processing.session.api.managed.ModelAccessoryFactory;

import tribefire.extension.process.api.model.ProcessManagerRequest;

public class MetaDataBasedMonitoredAccessesSupplier implements Supplier<List<String>>, LifecycleAware, DeployRegistryListener {
	private ModelAccessoryFactory modelAccessoryFactory;
	private DeployRegistry deployRegistry;
	private Set<String> monitoredAccessIds = ConcurrentHashMap.newKeySet();
	private String externalIdOfManager;

	@Required
	public void setExternalIdOfManager(String externalIdOfManager) {
		this.externalIdOfManager = externalIdOfManager;
	}
	
	@Required
	public void setDeployRegistry(DeployRegistry deployRegistry) {
		this.deployRegistry = deployRegistry;
	}
	
	@Required
	public void setModelAccessoryFactory(ModelAccessoryFactory modelAccessoryFactory) {
		this.modelAccessoryFactory = modelAccessoryFactory;
	}
	
	@Override
	public void postConstruct() {
		deployRegistry.addListener(this);
		
		for (Deployable deployable: deployRegistry.getDeployables()) {
			if (deployable instanceof IncrementalAccess) {
				addAccessIfMappedToManager(deployable.getExternalId());
			}
		}
	}
	
	@Override
	public void preDestroy() {
		deployRegistry.removeListener(this);
	}
	
	@Override
	public void onDeploy(Deployable deployable, DeployedUnit deployedUnit) {
		if (deployable instanceof IncrementalAccess) {
			addAccessIfMappedToManager(deployable.getExternalId());
		}
	}
	

	@Override
	public void onUndeploy(Deployable deployable, DeployedUnit deployedUnit) {
		if (deployable instanceof IncrementalAccess) {
			removeAccess(deployable.getExternalId());
		}
	}
	
	private void addAccessIfMappedToManager(String accessId) {
		ModelAccessory modelAccessory = modelAccessoryFactory.getForAccess(accessId);
		
		ProcessWith processWith = modelAccessory.getMetaData().entityType(ProcessManagerRequest.T).meta(ProcessWith.T).exclusive();
		
		if (processWith != null) {
			ServiceProcessor processor = processWith.getProcessor();
			
			if (processor != null) {
				if (externalIdOfManager.equals(processor.getExternalId())) {
					monitoredAccessIds.add(accessId);
				}
			}
		}
	}
	
	private void removeAccess(String accessId) {
		monitoredAccessIds.remove(accessId);
	}

	@Override
	public List<String> get() {
		synchronized (monitoredAccessIds) {
			return new ArrayList<>(monitoredAccessIds);
		}
	}
}
