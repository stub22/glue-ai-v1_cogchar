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

/**
 * @author Stu B. <www.texpedient.com>
 */

abstract class HandlerContext {
	val		myWebSessionState : WebSessionState
	// Control:  state:LifterState, sessionId:String, slotNum:Int, control:ControlConfig
	// Action:   state:LifterState, sessionId:String, slotNum:Int, control:ControlConfig, input:Array[String])	
	// Command:  state:LifterState, sessionId:String, slotNum:Int, command:String, input:Array[String])
	
}
class SlotHandlerContext() { 
}
class ControlHandlerContext {
}
