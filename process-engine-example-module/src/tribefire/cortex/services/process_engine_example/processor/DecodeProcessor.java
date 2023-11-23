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

import java.io.InputStreamReader;
import java.util.Properties;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.braintribe.codec.CodecException;
import com.braintribe.logging.Logger;
import com.braintribe.model.goofy.GoofyProcess;

import tribefire.cortex.services.process_engine_example.Tokens;
import tribefire.extension.process.api.TransitionProcessor;
import tribefire.extension.process.api.TransitionProcessorContext;

/**
 * exemplary decoding processor : read the properties file and import the values of the properties into the process
 * entity set the correct state if done, complain if any other properties are set
 *
 * @author pit
 *
 */
public class DecodeProcessor implements TransitionProcessor<GoofyProcess> {

	private static Logger log = Logger.getLogger(DecodeProcessor.class);
	private static String format = "dd.MM.yyyy";
	private DateTimeFormatter formatter = DateTimeFormat.forPattern(format);
	private boolean generateException;

	@Override
	public void process(TransitionProcessorContext<GoofyProcess> context) {

		if (generateException) {
			throw new UnsupportedOperationException("willingly thrown exception to simulate error in the first step processor");
		}
		GoofyProcess process = context.getProcess();

		try {
			InputStreamReader reader = new InputStreamReader(context.getSession().resources().openStream(process.getResource()));

			try {
				Properties properties = new Properties();
				properties.load(reader);
				for (String key : properties.stringPropertyNames()) {
					String value = properties.getProperty(key);
					if (key.equalsIgnoreCase("bulk") || key.equalsIgnoreCase("multiplicator")) {
						continue;
					}
					if (key.equals("date")) {
						try {
							process.setDate(formatter.parseLocalDate(value).toDate());
						} catch (CodecException e) {
							throw new GoofyProcessingException(
									"invalid date format for property [" + key + "] found, expected [" + format + "], found [" + value + "]");
						}
					} else if (key.equals("number")) {
						try {
							process.setNumber(Double.parseDouble(value));
						} catch (Exception e) {
							throw new GoofyProcessingException("invalid number format [" + value + "] for property [" + key + "] found ");
						}
					} else if (key.equals("name")) {
						process.setName(value);
					} else {
						throw new GoofyProcessingException("invalid property [" + key + "] found");
					}
				}
				// set validation request
				context.continueWithState(Tokens.validate);
			}

			finally {
				reader.close();
			}
		} catch (Exception e) {
			log.error("cannot read resource as " + e, e);
			context.continueWithState(Tokens.decodeError);
		}

	}

}
