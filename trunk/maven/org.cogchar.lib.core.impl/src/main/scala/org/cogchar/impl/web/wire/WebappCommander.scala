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
import scala.xml.NodeSeq
import org.appdapter.core.name.{FreeIdent, Ident}
import org.cogchar.impl.web.util.HasLogger
import org.cogchar.api.web.{WebControl}
import org.cogchar.impl.web.config.{WebControlImpl, LiftAmbassador, LiftConfig, WebInstanceGlob}
/**
 * @author Stu B. <www.texpedient.com>
 */

trait WebappCommander {
	def initFromCogcharRDF(sessionId:String, liftConfig:LiftConfig) 
	def getInitialConfigId : String
	def getInitConfig : LiftConfig 
	def getXmlForControl(sessionId: String, slotNum:Int, controlDef:WebControl): NodeSeq
	def setControl(sessionId: String, slotNum: Int, slotHtml: NodeSeq) 
	def loadControlDefToState(sessionId:String, slotNum:Int, controlDef:WebControl)
	def exposedUpdateListeners(x : Any) : Unit
	
	def initializeSessionAndRedirectToNewTemplate(sessionId:String)
}
