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
package tribefire.cortex.services.process_engine_example.input;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.goofy.GoofyProcess;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.source.FileUploadSource;

import tribefire.cortex.services.process_engine_example.Tokens;

public class GoofyInputReceiver implements Consumer<File> {
	private static Logger logger = Logger.getLogger(GoofyInputReceiver.class);
	private Supplier<PersistenceGmSession> sessionProvider;

	@Required
	public void setSessionProvider(Supplier<PersistenceGmSession> sessionProvider) {
		this.sessionProvider = sessionProvider;
	}

	@Override
	public void accept(File file) throws RuntimeException {
		logger.info("Receiving file!");

		try {

			/* quick and dirty preread to get the parameters */

			Properties properties = new Properties();

			FileInputStream in = new FileInputStream(file);
			properties.load(in);
			in.close();

			String multiplicatorAsString = properties.getProperty("multiplicator");
			String bulkAsString = properties.getProperty("bulk");

			int multiplicator = multiplicatorAsString != null ? Integer.parseInt(multiplicatorAsString) : 1;
			int bulk = bulkAsString != null ? Integer.parseInt(bulkAsString) : 1;

			in = new FileInputStream(file);

			try {
				PersistenceGmSession session = sessionProvider.get();

				Resource resource = session.resources().create().sourceType(FileUploadSource.T).name(file.getName()).store(in);
				resource.setMimeType("application/goofy");
				int b = 0;
				for (int m = 0; m < multiplicator; b++, m++) {

					GoofyProcess goofyProcess = session.create(GoofyProcess.T);
					goofyProcess.setResource(resource);
					goofyProcess.setState(Tokens.decode);

					if ((b + 1) % bulk == 0) {
						session.commit();
					}
				}
				if (b % bulk != 0) {
					session.commit();
				}
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						logger.error("error while closing input stream for " + file);
					}
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("error while creating resource from input file " + file, e);
		}
	}
}
