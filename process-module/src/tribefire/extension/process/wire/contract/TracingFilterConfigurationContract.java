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
package tribefire.extension.process.wire.contract;

import java.util.Set;

import com.braintribe.wire.api.annotation.Name;

import tribefire.extension.process.processing.TracingFilterConfiguration;
import tribefire.module.wire.contract.PropertyLookupContract;

public interface TracingFilterConfigurationContract extends PropertyLookupContract, TracingFilterConfiguration {

	@Override
	@Name("TRIBEFIRE_TRACE_EVENT_EXCLUDES")
	Set<String> traceEventExcludes();
	
	@Override
	@Name("TRIBEFIRE_TRACE_EVENT_INCLUDES")
	Set<String> traceEventIncludes();
	
}
