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
package tribefire.extension.process.processing.condition;

import tribefire.extension.process.api.ConditionProcessor;
import tribefire.extension.process.model.data.Process;

public abstract class ConditionProcessors {
	
	private static StaticConditionProcessor<Process> alwaysMatching = new StaticConditionProcessor<Process>(true);
	private static StaticConditionProcessor<Process> neverMatching = new StaticConditionProcessor<Process>(false);
	
	private ConditionProcessors() {
	}
	
	@SuppressWarnings("unchecked")
	public static <S extends Process> ConditionProcessor<S> alwaysMatching() {
		return (ConditionProcessor<S>)alwaysMatching;
	}
	@SuppressWarnings("unchecked")
	public static <S extends Process> ConditionProcessor<S> neverMatching() {
		return (ConditionProcessor<S>)neverMatching;
	}

}
