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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.braintribe.logging.Logger;

import tribefire.extension.process.data.model.ProcessItem;
import tribefire.extension.process.data.model.state.ProcessActivity;
import tribefire.extension.process.processing.mgt.api.Reference;

public class ProcessTerminationListening {
	private static final Logger logger = Logger.getLogger(ProcessTerminationListening.class);
	
	private Map<Reference<? extends ProcessItem>, ItemListeners> individualListeners = new ConcurrentHashMap<>();
	private Set<BiConsumer<Reference<? extends ProcessItem>, ProcessActivity>> listeners = ConcurrentHashMap.newKeySet();
	
	public void addProcessListener(BiConsumer<Reference<? extends ProcessItem>, ProcessActivity> listener) {
		listeners.add(listener);
	}
	
	public void removeProcessListener(BiConsumer<Reference<? extends ProcessItem>, ProcessActivity> listener) {
		listeners.remove(listener);
	}
	
	public void addProcessListener(Reference<? extends ProcessItem> processRef, Consumer<ProcessActivity> listener) {
		individualListeners.compute(processRef, (k,v) -> computeAdd(k, v, listener));
	}
	
	public void removeProcessListener(Reference<? extends ProcessItem> processRef, Consumer<ProcessActivity> listener) {
		individualListeners.compute(processRef, (k,v) -> computeRemove(k, v, listener));
	}
	
	public void notifyProcessListener(Reference<? extends ProcessItem> processRef, ProcessActivity activity) {

		for (BiConsumer<Reference<? extends ProcessItem>, ProcessActivity> listener: listeners) {
			try {
				listener.accept(processRef, activity);
			}
			catch (Exception e) {
				logger.error("error while calling runnable during notification of process ended", e);
			}
		}
		
		ItemListeners itemListeners = individualListeners.get(processRef);
		
		if (itemListeners != null) {
			Consumer<ProcessActivity> listeners[] = itemListeners.listeners();
			
			for (Consumer<ProcessActivity> listener: listeners) {
				try {
					listener.accept(activity);
				}
				catch (Exception e) {
					logger.error("error while calling runnable during notification of process ended", e);
				}
			}
		}
	}
	
	private static class ItemListeners {
		private List<Consumer<ProcessActivity>> listeners = new ArrayList<>();
		
		public synchronized void add(Consumer<ProcessActivity> listener) {
			listeners.add(listener);
		}
		
		public synchronized boolean remove(Consumer<ProcessActivity> listener) {
			listeners.remove(listener);
			return listeners.isEmpty();
		}
		
		public synchronized Consumer<ProcessActivity>[] listeners() {
			return (Consumer<ProcessActivity>[]) listeners.toArray(new Consumer<?>[listeners.size()]);
		}
	}
	
	private ItemListeners computeAdd(Reference<? extends ProcessItem> processRef, ItemListeners itemListeners, Consumer<ProcessActivity> listener) {
		if (itemListeners == null) {
			itemListeners = new ItemListeners();
		}
		
		itemListeners.add(listener);
		
		return itemListeners;
	}
	
	private ItemListeners computeRemove(Reference<? extends ProcessItem> processRef, ItemListeners itemListeners, Consumer<ProcessActivity> listener) {
		if (itemListeners == null) 
			return null;
		
		if (itemListeners.remove(listener))
			return null;
		
		return itemListeners;
	}
}