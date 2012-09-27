/*
 *  Copyright 2012 by The Cogchar Project (www.cogchar.org).
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.cogchar.render.app.core;
import com.jme3.asset.plugins.UrlLocator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.appdapter.core.log.BasicDebugger;
/**
 * @author Stu B. <www.texpedient.com>
 */

public class JdkLoggerMediator extends BasicDebugger {
	private Logger ulLogger, genLogger;
	public void setUp() {
		logInfo("Setting general JDK-logger level (used by JME3) to 'WARNING'");
		genLogger = Logger.getLogger("");
		genLogger.setLevel(Level.WARNING);

		logInfo("Disabling confusing JDK-Logger warnings from UrlLocator");		
		ulLogger = Logger.getLogger(UrlLocator.class.getName());
		ulLogger.setLevel(Level.SEVERE);
	}
}
