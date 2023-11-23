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

import java.util.Date;

import com.braintribe.model.goofy.GoofyProcess;
import tribefire.extension.process.api.TransitionProcessor;
import tribefire.extension.process.api.TransitionProcessorContext;

import tribefire.cortex.services.process_engine_example.Tokens;

public class HashProcessor implements TransitionProcessor<GoofyProcess> {

	@Override
	public void process(TransitionProcessorContext<GoofyProcess> context) {
		GoofyProcess process = context.getProcess();

		Date date = process.getDate();
		Double number = process.getNumber();
		String name = process.getName();

		String hash = number + "-" + name + "-" + date.getTime();

		process.setHash(hash);

		context.continueWithState(Tokens.output);
	}

}
