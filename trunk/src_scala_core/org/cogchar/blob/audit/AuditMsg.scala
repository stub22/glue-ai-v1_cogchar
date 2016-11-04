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

import org.appdapter.core.name.SerIdent
import java.io.Serializable

/**
 * @author Stu B. <www.texpedient.com>
 */

// Some AuditMsgs are serializable
trait AuditMsg {
	
}
// AuditUri is mostly equivalent to Ident.  
trait AuditUri extends Serializable {
	def getID : SerIdent
}

trait AJMsgHeader extends Serializable { 
	// Machine assigned and mostly meaningless -- random or sequential (which may imply ordering, yeah, but let's
	// not use that.  Use the timestamps.)
	def getJobUri : AuditUri
	// These two kind-URIs refer to app-specific Ontology types, which together allow us receivers to interpret the job.
	// We provide a starting library of kinds to build on.
	def getJobKindUri : AuditUri	
	def getActionKindUri : AuditUri	
	def getStampJavaMillisec : Long

}
// Payload may be not-readily-serializable, but can be conveyed to an Actor who will take care of that.
// The other aspects of the Msg should all be serializable - that's why we put them in a Header type.
trait AuditJobMsg[PayloadType] extends AuditMsg {
	def getHeader : AJMsgHeader
	def getPayload : PayloadType  
}
// JobKind will often refer to a circus recipe *type*.
class AJMsgHeaderNaiveImpl(myJobUri : AuditUri, myJobKindUri : AuditUri, myActionKindUri : AuditUri) extends AJMsgHeader {
	def getJobUri : AuditUri = myJobUri // Instance URI generated automatically
	// These kinds are from some app-specific onto, for we provide suggested template patterns.
	// "Kinds" may be thought of as either types or individuals.
	def getJobKindUri : AuditUri = myJobKindUri
	// Points to a subtype *or* individual-of-subtype of CircusActionToken.  
	def getActionKindUri : AuditUri = myActionKindUri 
	val	myStampMsec : Long = System.currentTimeMillis
	override def getStampJavaMillisec : Long = myStampMsec
}
class AuditUriNaiveImpl(auditID : SerIdent) extends AuditUri { 
	override def getID : SerIdent = auditID
}
class AJMsgNaiveImpl[PayloadType](
		private val myHeader : AJMsgHeader, 
		private val myPayload : PayloadType) extends AuditJobMsg[PayloadType] {
	override def getHeader : AJMsgHeader = myHeader
	override def getPayload : PayloadType = myPayload	
}

// Used by Biz classes to write a series of messages related to a "Job" context, which should usually
// not be kept open for longer than about one second, unless it's for some kind of inherently long
// operation, such as a remote batch transfer.  
// action(Kind)Uris are often drawn from 

trait AuditJobHandle {
	protected def getJobUri : AuditUri
	protected def getJobKindUri : AuditUri
	protected def getAuditSvc : AuditSenderSvc
	private def makeActionMsg[PayType](jobActionKindUri : AuditUri, payload : PayType) : AuditJobMsg[PayType] = {
		val msgHeader = new AJMsgHeaderNaiveImpl(getJobUri, getJobKindUri, jobActionKindUri)
		new AJMsgNaiveImpl(msgHeader, payload)
	}
	def sendActionMsg[PayType](actionKindUri : AuditUri, payload : PayType) : Unit = {
		val auditSvc = getAuditSvc
		val jobMsg = makeActionMsg(actionKindUri, payload)
		auditSvc.sendMsg(jobMsg)
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
class AuditJobFactory {
	
	def makeJobHandle(svc : AuditSenderSvc, jobKindUri : AuditUri) : AuditJobHandle = {
		val jobUri : AuditUri = null // TODO: assign random or timestamped
		new AuditJobHandle {
			override protected def getJobUri : AuditUri = jobUri
			override protected def getJobKindUri : AuditUri = jobKindUri
			override protected def getAuditSvc : AuditSenderSvc = svc
			
		}
		
	}
	
}
