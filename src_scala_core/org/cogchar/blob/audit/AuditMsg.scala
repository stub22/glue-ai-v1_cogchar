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

/**
 * @author Stu B. <www.texpedient.com>
 */

trait AuditUri {	
}
trait AuditMsg {
	
}
class AuditJobAction(myJobUri : AuditUri, myActionUri : AuditUri)  {
	def getJobUri : AuditUri = myJobUri
	def getActionUri : AuditUri = myActionUri
}
trait AuditJobMsg[PayloadType] extends AuditMsg {
	def getJobUri : AuditUri
	def getJobAction : AuditJobAction
	def getPayload : PayloadType
}

class AuditJobMsgImpl[PayloadType](
		private val myAction : AuditJobAction, 
		private val myPayload : PayloadType) extends AuditJobMsg[PayloadType] {
	override def getJobUri : AuditUri = myAction.getJobUri
	override def getJobAction : AuditJobAction = myAction
	override def getPayload : PayloadType = myPayload
}

// Used by Biz classes to write a series of messages related to a "Job" context, which should usually
// not be kept open for longer than about one second, unless it's for some kind of inherently long
// operation, such as a remote batch transfer.  The actionUris are drawn from the ___ ontology.  (Circus?).
trait AuditJobHandle {
	def getJobUri : AuditUri
	def makeJobMsg[PayType](jobActionUri : AuditUri, payload : PayType) : AuditJobMsg[PayType] = {
		val jobAction = new AuditJobAction(getJobUri, jobActionUri)
		new AuditJobMsgImpl(jobAction, payload)
	}
}
/*
case class AJA_Begin extends AuditJobAction
case class AJA_Change extends AuditJobAction 
case class AJA_End extends AuditJobAction
// Some kind of expected failure.
case class AJA_Exception extends AuditJobAction 

// Some unexpected hard failure - this is used sparingly, for crash debugging.
case class AJA_Fail extends AuditJobAction
*/
// Use this 
object AuditJobMsgFactory {
	
	def makeJobMsg[PayType](jobAction : AuditJobAction, payload : PayType) : AuditJobMsg[PayType] = {
		new AuditJobMsgImpl(jobAction, payload)
	}

	def makeJobHandle() : AuditJobHandle = {
		val jobUri : AuditUri = null // TODO: assign random
		new AuditJobHandle {
			override def getJobUri : AuditUri = jobUri
		}
	}
//	def makeJobBeginMsg
}
