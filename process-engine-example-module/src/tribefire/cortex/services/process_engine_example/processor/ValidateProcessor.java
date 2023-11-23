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
package tribefire.cortex.services.process_engine_example.processor;

import java.util.Calendar;
import java.util.Date;

import com.braintribe.model.goofy.GoofyProcess;
import tribefire.extension.process.api.TransitionProcessor;
import tribefire.extension.process.api.TransitionProcessorContext;

import tribefire.cortex.services.process_engine_example.Tokens;

public class ValidateProcessor implements TransitionProcessor<GoofyProcess> {

	@Override
	public void process(TransitionProcessorContext<GoofyProcess> context) {
		GoofyProcess process = context.getProcess();

		// date..
		Date date = process.getDate();
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		calendar.set(Calendar.MONTH, Calendar.JANUARY);
		calendar.set(Calendar.YEAR, 2000);
		Date after = calendar.getTime();
		if (date == null || date.before(after)) {
			context.continueWithState(Tokens.validateError);
			return;
		}

		// number
		Double number = process.getNumber();
		if (number == null || number < 0 || number > 100000) {
			context.continueWithState(Tokens.validateError);
			return;
		}

		// name
		String name = process.getName();
		if (name == null || name.length() == 0 || name.equalsIgnoreCase("scrooge")) {
			context.continueWithState(Tokens.validateError);
			return;
		}
	}

}
