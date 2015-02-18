/*
 *  Copyright 2015 by The Cogchar Project (www.cogchar.org).
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

package org.cogchar.blob.circus
import org.appdapter.fancy.log.VarargsLogging

import org.cogchar.blob.audit._
/**
 */

object CircusTest extends VarargsLogging with AuditSvcFinderGlobal {
	def main(args: Array[String]) : Unit = {
		// Must enable "compile" or "provided" scope for Log4J dep in order to compile this code.
		org.apache.log4j.BasicConfigurator.configure();
		org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.ALL);
		info0("Starting CircusTest")
		val auditSvcTest = findSenderSvc
		
	}
}

