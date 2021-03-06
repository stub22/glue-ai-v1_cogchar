/*
 *  Copyright 2014 by The Cogchar Project (www.cogchar.org).
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

package org.cogchar.impl.web.util

import org.appdapter.core.name.Ident

/**
 * @author Stu B. <www.texpedient.com>
 */

object WebHelper {
	 
	def getUriPrefix(uri: Ident) : String = {
		uri.getAbsUriString.stripSuffix(uri.getLocalName)
	}
	
	val SINGLE_SLOT_TEMPLATE = "singleSlot"	
	val INITIAL_CONFIG_ID = "InitialConfig" // "sessionId" for initial config for new sessions

}
