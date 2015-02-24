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

package org.cogchar.blob.audit
import org.appdapter.fancy.log.VarargsLogging
/**
 * @author Stu B. <www.texpedient.com>
 */

case class AdminMsg_Stop

// Here is the "old" Scala API summary, which we still rely on as of 2015-Jan.
// http://docs.scala-lang.org/overviews/core/actors.html
// We are trying to limit the fanciness of our Actors code, so that it will work with minimal changes under Scala 2.11.
class AuditSendActor  extends scala.actors.Actor with VarargsLogging {
	// Handles any AuditMsg we send it by writing to an Audit Log.
	private lazy val myAuditLogWriter : AuditLogWriter = new AuditLogWriter
		
	// The lifecycle of the actor 
	override  def act : Unit = {
		var myDoneFlag : Boolean = false

		// self is a method on the companion object
		def getSelf = scala.actors.Actor.self
		// val selfThing = "[self undefined in legacy Actors]"
		// Loop and process messages until the doneMarker is set.
		while (!myDoneFlag) {
			info2("AuditSendActor at top of loop for {} which is {}", getSelf, this)
			// Blocking "receive" call uses scala actors runtime to wait for a message for this actor, which is
			// then passed to the anonymous-partial-function contents of the block.
			receive {
				case amsg : AuditMsg => { 
					info1("AuditSendActor got regular AuditMsg: [{}], sending to audit log writer", amsg)
					myAuditLogWriter.absorbMessage(amsg)
				}
				case stopMsg : AdminMsg_Stop => {
					info2("AuditSendActor got AdminMsg_Stop {}, setting myDoneFlag for {}", stopMsg, this)
					myDoneFlag = true
				}
				case strngMsg : String => {
					info1("Got a string message, and explicitly matched it as: {}", strngMsg)	
				}
				// Best practice in production actors code is usually *not* to handle the wildcard case, so that
				// frameworks (e.g. Akka EventBus) can do something with the 'unhandled'.  However we are currently 
				// handling wildcard here, naively.  Interestingly, "other" shows up as a wrapped object
				// scala.actors.Reactor$$anon$3@6156ee8e
				case other => {
					info1("AuditSendActor got other message: {}, absorbing it (rather than leaving 'unhandled')", other)
				}
			}
		}
		info2("AuditSendActor's work is done - exiting act() method for {} which is {}", getSelf, this)
    }
	
}
