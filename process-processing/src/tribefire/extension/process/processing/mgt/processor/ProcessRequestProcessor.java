// ============================================================================
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package tribefire.extension.process.processing.mgt.processor;

import java.util.List;
import java.util.stream.Collectors;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.InvalidArgument;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmCustomType;
import com.braintribe.model.processing.lock.api.Locking;
import com.braintribe.model.processing.meta.oracle.EntityTypeOracle;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.query.building.EntityQueries;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.conditions.ValueComparison;

import tribefire.extension.process.api.model.ProcessRequest;
import tribefire.extension.process.data.model.ProcessItem;
import tribefire.extension.process.processing.mgt.api.Reference;
import tribefire.extension.process.processing.mgt.common.ProcessLockingTrait;
import tribefire.extension.process.reason.model.ProcessNotFound;

public abstract class ProcessRequestProcessor<R extends ProcessRequest, E> extends ProcessManagerRequestProcessor<R, E> implements ProcessLockingTrait {
	
	protected EntityType<? extends ProcessItem> itemEntityType;
	protected String itemId;
	protected Reference<? extends ProcessItem> itemReference;
	
	protected Reason validateRequest() {
		itemId = request.getItemId();

		if (itemId == null) {
			return Reasons.build(InvalidArgument.T).text(request.entityType().getShortName() + "." + ProcessRequest.itemId  + " must not be empty").toReason();
		}
		
		Maybe<EntityType<? extends ProcessItem>> typeMaybe = resolveType();  
		
		if (typeMaybe.isUnsatisfied())
			return typeMaybe.whyUnsatisfied();
		
		itemEntityType = typeMaybe.get();
		
		itemReference = Reference.of(itemEntityType, itemId);
		
		return null;
	}
	
	private Maybe<EntityType<? extends ProcessItem>> resolveType() {
		String itemType = request.getItemType();
		
		if (itemType == null) {
			return Reasons.build(InvalidArgument.T).text(request.entityType().getShortName() + "." + ProcessRequest.itemType  + " must not be empty").toMaybe();
		}
		
		ModelOracle oracle = context().getSystemSession().getModelAccessory().getOracle();
		
		EntityTypeOracle typeOracle = oracle.findEntityTypeOracle(itemType);
		
		if (typeOracle == null) {
			List<GmCustomType> types = oracle.findGmTypeBySimpleName(itemType);
			
			switch (types.size()) {
			case 0:
				return Reasons.build(InvalidArgument.T) //
						.text(request.entityType().getShortName() + "." + ProcessRequest.itemType  + " [" + itemType + "] refers to non-existing type").toMaybe();
			case 1:
				GmCustomType gmCustomType = types.get(0);
				if (!gmCustomType.isEntity())
					return Reasons.build(InvalidArgument.T) //
							.text(request.entityType().getShortName() + "." + ProcessRequest.itemType  + " [" + itemType + "] is not an entity type " + gmCustomType.getTypeSignature()).toMaybe();

				typeOracle = oracle.getEntityTypeOracle(gmCustomType.getTypeSignature());
			default:
				String ambigiousTypes = types.stream().map(GmCustomType::getTypeSignature).collect(Collectors.joining(","));
				return Reasons.build(InvalidArgument.T) //
						.text(request.entityType().getShortName() + "." + ProcessRequest.itemType  + " [" + itemType + "] refers to ambigious types " + ambigiousTypes).toMaybe();
			}
		}
		
		EntityType<?> type = typeOracle.asType();
		
		if (!ProcessItem.T.isAssignableFrom(type)) {
			return Reasons.build(InvalidArgument.T).text(request.entityType().getShortName() + "." + ProcessRequest.itemType + " [" + itemType + "] is not a ProcessItem type").toMaybe();
		}

		return Maybe.complete(type.cast());
	}
	
	public EntityType<? extends ProcessItem> getItemEntityType() {
		return itemEntityType;
	}
	
	public String getItemId() {
		return itemId;
	}
	
	public Locking getLocking() {
		return processManagerContext.locking;
	}
	
	public Reference<? extends ProcessItem> getItemReference() {
		return itemReference;
	}

	protected Maybe<ProcessItem> getProcessItem() {
		EntityQuery itemQuery = EntityQuery.create(itemEntityType).where(EntityQueries.eq(EntityQueries.property(ProcessItem.id), itemId));
		
		ProcessItem processItem = context().getSystemSession().query().entities(itemQuery).unique();
		
		if (processItem == null)
			return Reasons.build(ProcessNotFound.T).text("ProcessItem with id [" + itemId + "] not found").toMaybe();
		
		Reason invalidProcessReason = validateItem(processItem);
		
		if (invalidProcessReason != null)
			return invalidProcessReason.asMaybe();
		
		return Maybe.complete(processItem);
	}

	
	protected Reason validateItem(ProcessItem processItem) {
		return null;
	}

	
	@Override
	public Maybe<E> processReasoned() {
		
		Reason invalidationReason = validateRequest();
		
		if (invalidationReason != null)
			return invalidationReason.asMaybe();

		

		return processValidatedRequest();
	}
	
	protected abstract Maybe<E> processValidatedRequest();
}