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

package org.cogchar.impl.web.wire

import java.util.concurrent.ConcurrentHashMap
import scala.collection.mutable.ConcurrentMap
import scala.collection.JavaConversions._ // required to use java.util.concurrent.ConcurrentHashMap as a scala.collection.mutable.ConcurrentMap
import org.cogchar.impl.web.util.WebHelper
/**
 * Captures previous globalVariables from LifterState
 */

class WebappGlobalState {
	private val globalLifterVariablesByName:ConcurrentMap[String,String] = new DfltConcHashMap[String,String]
	
	def getGlobalVariable(key:String): String = { // returns value from "public" (global) app variables map
		var contents:String = null
		val globalVariables = globalLifterVariablesByName
		if (globalVariables contains key) contents = globalVariables(key)
		contents
	}
	def hasGlobalVariable(key:String) : Boolean = globalLifterVariablesByName contains key
		
	
	def exposeMapForHacks() : ConcurrentMap[String,String] = globalLifterVariablesByName
	def clearAndInit() = {
		globalLifterVariablesByName.clear
	}
	
	def getMaxControlCount() : Int = HashMapBindings.MAX_CONTROL_QUANTITY
  
	
	def getSingleSlotTemplateName() : String = WebHelper.SINGLE_SLOT_TEMPLATE
	
	def getSessionInitialConfigID() : String = WebHelper.INITIAL_CONFIG_ID	
}
