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
package tribefire.extension.process.processing.mgt.processor;

import java.util.List;
import java.util.stream.Collectors;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.InvalidArgument;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmCustomType;
import com.braintribe.model.processing.accessrequest.api.AbstractStatefulAccessRequestProcessor;
import com.braintribe.model.processing.accessrequest.api.ReasonedStatefulProcessor;
import com.braintribe.model.processing.meta.oracle.EntityTypeOracle;
import com.braintribe.model.processing.meta.oracle.ModelOracle;

import tribefire.extension.process.api.model.ProcessManagerRequest;
import tribefire.extension.process.api.model.ProcessRequest;
import tribefire.extension.process.data.model.ProcessItem;
import tribefire.extension.process.processing.mgt.ProcessManagerContext;

public abstract class ProcessManagerRequestProcessor<R extends ProcessManagerRequest, E> extends AbstractStatefulAccessRequestProcessor<R, E> implements ReasonedStatefulProcessor<E>{
	
	protected R request;
	protected ProcessManagerContext processManagerContext;
	
	public void initProcessManagerContext(ProcessManagerContext processManagerContext) {
		this.processManagerContext = processManagerContext;
	}
	
	@Override
	public void configure() {
		request = context().getOriginalRequest();
	}
	
	protected Maybe<EntityType<? extends ProcessItem>> resolveType(String itemType) {
		
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

}