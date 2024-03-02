// ============================================================================
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package tribefire.extension.process.processing.experts;

import tribefire.extension.process.api.ConditionProcessorContext;
import tribefire.extension.process.test.model.AmountTestProcess;

public class Lt10000ConditionProcessor extends TestConditionProcessor<AmountTestProcess> {
	@Override
	public boolean matches(ConditionProcessorContext<AmountTestProcess> context) {
		return context.getProcess().getAmount() < 10000;
	}
}
