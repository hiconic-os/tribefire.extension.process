// ============================================================================
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package tribefire.extension.process.processing.mgt.common;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;

import com.braintribe.exception.Exceptions;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.processing.lock.api.Locking;
import com.braintribe.model.processing.lock.api.ReentrableLocking;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

import tribefire.extension.process.data.model.ProcessItem;
import tribefire.extension.process.reason.model.CouldNotAcquireProcessLock;

public interface ProcessLockingTrait {

	EntityType<? extends ProcessItem> getItemEntityType();
	String getItemId();
	Locking getLocking();
	PersistenceGmSession systemSession();
	
	default Maybe<Lock> lockProcess() {
		return lockProcess(null);
	}
	
	
	default Maybe<Lock> lockProcess(String reentrantId) {
		PersistentEntityReference reference = PersistentEntityReference.T.create();
		
		reference.setTypeSignature(getItemEntityType().getTypeSignature());
		reference.setRefId(getItemId());
		
		// TODO: in order to avoid lockId syntax redundancy we should get lock more conveniently with help of locking api 
		String lockId = getItemEntityType().getTypeSignature() + "[" + getItemId() + "]@" + systemSession().getAccessId();  
		
		ReentrableLocking locking = reentrantId != null?
				getLocking().withReentranceId(reentrantId):
				getLocking();
		
		Lock lock = locking.forIdentifier("entity", lockId).writeLock();
		
		try {
			if (!lock.tryLock(5, TimeUnit.SECONDS)) {
				return Reasons.build(CouldNotAcquireProcessLock.T) //
						.text("Could not acquire lock for ProcessItem " + getItemEntityType().getTypeSignature() + "[id=" + getItemId() + "]") //
						.toMaybe();
			}
		} catch (InterruptedException e) {
			throw Exceptions.unchecked(e);
		}
		
		return Maybe.complete(lock);
	}
	
	default <T> Maybe<T> executeLocked(Supplier<Maybe<T>> executable) {
		return executeLocked(executable, null);
	}
	
	default <T> Maybe<T> executeLocked(Supplier<Maybe<T>> executable, String reentranceLockId) {
		Maybe<Lock> lockMaybe = lockProcess(reentranceLockId);
		
		if (lockMaybe.isUnsatisfied())
			return lockMaybe.whyUnsatisfied().asMaybe();
		
		Lock lock = lockMaybe.get();
		
		try {
			return executable.get();
		}
		finally {
			lock.unlock();
		}
	}
}
