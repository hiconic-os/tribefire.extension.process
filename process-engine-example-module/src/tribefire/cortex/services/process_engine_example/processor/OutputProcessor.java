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

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Properties;
import java.util.UUID;

import com.braintribe.codec.string.DateCodec;
import com.braintribe.logging.Logger;
import com.braintribe.model.goofy.GoofyProcess;
import tribefire.extension.process.api.TransitionProcessor;
import tribefire.extension.process.api.TransitionProcessorContext;

import tribefire.cortex.services.process_engine_example.Tokens;

/**
 * @author pit
 *
 */
public class OutputProcessor implements TransitionProcessor<GoofyProcess> {
	private static Logger logger = Logger.getLogger(OutputProcessor.class);

	private DateCodec dateCodec = new DateCodec("dd.MM.yyyy");
	private File outputDirectory;

//	@Required
	public void setOutputDirectory(File outputDirectory) {
		this.outputDirectory = outputDirectory;
	}

	@Override
	public void process(TransitionProcessorContext<GoofyProcess> context) {

		GoofyProcess process = context.getProcess();

		Properties properties = new Properties();
		try {

			properties.setProperty("date", dateCodec.encode(process.getDate()));
			properties.setProperty("name", process.getName());
			properties.setProperty("number", process.getNumber().toString());
			properties.setProperty("hash", process.getHash());
			
			if (outputDirectory == null) {
				logger.info("No output folder provided. Skipping output");
				return;
			}
			
			Writer writer = new OutputStreamWriter(new FileOutputStream(new File(outputDirectory, UUID.randomUUID().toString())), "UTF-8");
			try {
				properties.store(writer, "no comment says Goofy");
			} finally {
				writer.close();
			}			
		} catch (Exception e) {
			throw new GoofyProcessingException(e);
		} finally {
			context.continueWithState(Tokens.finalize);
		}

	}

}
