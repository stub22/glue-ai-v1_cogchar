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

import org.appdapter.core.log.BasicDebugger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class JdkLoggerMediator extends BasicDebugger {
	private Logger ulLogger, genLogger;

	public void setUp() {
		// TODO(ben): Not sure this is setup right
		getLogger().info("Setting general JDK-logger level (used by JME3) to 'WARNING'");
		genLogger = LoggerFactory.getLogger(JdkLoggerMediator.class);

		getLogger().info("Disabling confusing JDK-Logger warnings from UrlLocator");
		ulLogger = LoggerFactory.getLogger(JdkLoggerMediator.class);
	}
}
