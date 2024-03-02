// ============================================================================
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package tribefire.extension.process.processing.base;

import java.util.logging.Level;

import com.braintribe.logging.Logger;
import com.braintribe.logging.Logger.LogLevel;

public class LogLab {
	private static final Logger logger = Logger.getLogger(LogLab.class);
	
	public static void main(String[] args) {
		logger.setLogLevel(LogLevel.DEBUG);
		
		java.util.logging.Logger.getLogger("").setLevel(Level.ALL);
		
		logger.info("Hello World");
		logger.debug("Debug");
	}
}
