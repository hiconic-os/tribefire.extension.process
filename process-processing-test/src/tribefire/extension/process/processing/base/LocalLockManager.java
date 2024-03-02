// ============================================================================
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package tribefire.extension.process.processing.base;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.braintribe.model.processing.lock.api.LockBuilder;
import com.braintribe.model.processing.lock.api.LockManager;

public class LocalLockManager implements LockManager {

	private ConcurrentHashMap<String,LockBuilder> lockBuilders = new ConcurrentHashMap<>();
	
	@Override
	public LockBuilder forIdentifier(String id) {
		return lockBuilders.computeIfAbsent(id, i -> new LocalLockBuilder(id));
	}
	
	static class LocalLockBuilder implements LockBuilder {

		@SuppressWarnings("unused")
		private String id;
		private ReentrantLock lock = new ReentrantLock();

		public LocalLockBuilder(String id) {
			this.id = id;
		}

		@Override
		public Lock exclusive() {
			return lock;
		}

		@Override
		public Lock shared() {
			return lock;
		}

		@Override
		public LockBuilder caller(String callerSignature) {
			return this;
		}

		@Override
		public LockBuilder machine(String machineSignature) {
			return this;
		}

		@Override
		@Deprecated
		public LockBuilder lockTimeout(long time, TimeUnit unit) {
			return this;
		}

		@Override
		public LockBuilder lockTtl(long time, TimeUnit unit) {
			return this;
		}

		@Override
		public LockBuilder holderId(String holderId) {
			return this;
		}
		
	}
 
	@Override
	public String description() {
		return "local lock manager";
	}
}