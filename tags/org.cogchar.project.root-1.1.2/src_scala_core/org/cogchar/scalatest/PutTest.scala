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

package org.cogchar.scalatest
import org.appdapter.core.log.{BasicDebugger, Loggable};
import org.appdapter.fancy.log.VarargsLogging
import org.appdapter.core.name.{Ident, FreeIdent};
import org.appdapter.core.item.{Item, ItemFuncs, JenaResourceItem};
import org.appdapter.bind.rdf.jena.assembly.ItemAssemblyReader;
import org.apache.jena.riot.RDFDataMgr
import org.ontoware.rdfreactor.runtime.{ReactorResult}
import com.hp.hpl.jena
import org.ontoware.rdf2go

// import com.hp.hpl.jena.assembler.Assembler;
// import com.hp.hpl.jena.assembler.Mode;
// import scala.collection.mutable.HashSet



/**
 * @author Stu B. <www.texpedient.com>
 * 
 * Testing that we can write MDir-compatible data using actors at the data absorbers.
 * We keep in mind that MDir should mostly be promoted to Appdapter at some point, but
 * it saves us (glue.ai team) full-build-turnaround time to keep it here for now.
 */

object PutTest extends VarargsLogging {
	def main(args: Array[String]) : Unit = {
		// Must enable "compile" or "provided" scope for Log4J dep in order to compile this code.
		org.apache.log4j.BasicConfigurator.configure();
		org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.ALL);
		info0("Puttin MDir paths to the PutTest!")
		
		// Here we know our legacy actor-instance explicitly, which is not the Akka way.  We are supposed to use the 
		// Akka factory, which gives us back an ActorRef.  
		val outPuss = new WritingActor()
		// Starting outPuss right away keeps us in compliance with Akka impl required in Scala 2.11.
		// http://docs.scala-lang.org/overviews/core/actors-migration-guide.html
		// Bullet #4
		// "Akka actors are automatically started when instantiated. Users will have to reshape their 
		// system so it starts all the actors right after their instantiation."
		// 
		// Also note that it is this first start call which appears to fire up the Actors threading system.
		// Without it, the program will exit immediately after main() ends.
		outPuss.start
		outPuss ! "why not just send a string, eh?"  // This comes through as a Reactor
		outPuss ! 25
		outPuss ! PTMsg("text inside a nice little PTMsg case class")
		outPuss ! StopMsg() // Sending this allows the actors framework to shutdown, too, so the program can exit.
		outPuss ! PTMsg("Since the target WritingActor was already told to stop, this message will never be processed")
		outPuss ! PTMsg("Nor will this one")
		// Program will now exit, unless the StopMsg line above is commented out.
		info0("Main is done, program should exit soon")
	}
}
case class PTMsg(txt : String)

case class StopMsg()

case class Msg_CopyGraph

import org.cogchar.api.owrap.mdir._
import rdf2go.model.Model
trait UriWrap {
	def getR2GoURI : rdf2go.model.node.URI
	def getIdent : Ident
	def getJenaRes : jena.rdf.model.Resource
}
abstract class UriWrap_R2Go(r2goUri : rdf2go.model.node.URI) extends UriWrap {
	override def getR2GoURI : rdf2go.model.node.URI = r2goUri
}
object UWrapFactory {
	def makeUriWrap(r2goUri : rdf2go.model.node.URI) : UriWrap = ???
	def makeUriWrap(mthing : MThing) : UriWrap = makeUriWrap(mthing.asURI)
}
trait IndivWrap[IType <: MThing] {
	def getJavaClazz : Class[_ <: IType] 

	def getIndivUriWrap : UriWrap 
	
	def getClazzUriWrap : UriWrap = ???
	
	def getIndivInR2GoModel(mR2go : rdf2go.model.Model, writeTypeIfNeeded : Boolean) : IType = ???
	
	// Thus we can promote any IndivWrap to be a bound IndivWrap.
	def makeBoundIW(mR2go : rdf2go.model.Model, writeType : Boolean) : BoundIW[IType] = {
		val mthing : MThing = new MThing(mR2go, getIndivUriWrap.getR2GoURI, writeType)
		val strongIndiv : IType = mthing.castTo(getJavaClazz).asInstanceOf[IType]
		new DirectBoundIW(strongIndiv)
	}
	// rdf2go.model.Model
	// def getResInJenaModel(mJena : jena.rdf.model.Model) : 
}
trait BoundIW[IType <: MThing] extends IndivWrap[IType] {
	// The returned indiv should be connected to a readable open R2Go model (possibly in a TDB read trans).
	def getIndiv : IType = ???  
	
	def getBoundR2GoModel : rdf2go.model.Model = getIndiv.getModel
	
	override def makeBoundIW(mR2go : rdf2go.model.Model, writeType : Boolean) : BoundIW[IType] = {
		// If self already bound to that model, then self.
		super.makeBoundIW(mR2go, writeType)
	}
}

// We presume that myDirectIndiv is bound to some readable open M2Go model (possibly in a TDB read trans).
case class DirectBoundIW [IType <: MThing](private val myDirectIndiv : IType) extends BoundIW[IType] {
	override def getIndiv : IType = myDirectIndiv
	override def getJavaClazz : Class[_ <: IType] = myDirectIndiv.getClass
	private lazy val myIndivUriWrap = UWrapFactory.makeUriWrap(myDirectIndiv)
	override def getIndivUriWrap : UriWrap = myIndivUriWrap
	private lazy val myClazzUriWrap : UriWrap =  UWrapFactory.makeUriWrap(myDirectIndiv.getRDFSClassURI)
}
// Consider supplying classUri as additional param
case class OneIndivWrap[IType <: MThing](indivUriWrap : UriWrap, jClz : Class[_ <: IType]) extends IndivWrap[IType] {
	override def getIndivUriWrap : UriWrap  = indivUriWrap
	override def getJavaClazz : Class[_ <: IType] = jClz
}
object IWrapFactory {
	def makeIWrap[IType <: MThing](indiv : IType) : IndivWrap[IType] = ???
	def makeIWrap[IType <: MThing](indivUriWrap : UriWrap, clz : Class[IType]) : IndivWrap[IType] = ???
	
}



// We generally have a need for typed tuples containing RDF2Go instances in known states of resolution+backing.
abstract class GraphHostWrap[GHType <: GraphHost](rslvdJClz : Class[GHType]) {
	def getGraphHost : GHType 
}

trait GraphOpWrap[GOType <: GraphOp] {
	def getGraphOp : GOType
}
trait GPointerSetWrap { 
	def getGPointerSet : GraphPointerSet
}
trait AnyPointerWrap[APType <: Pointer] {
	def getPointer : APType
}

trait GraphPointerWrap extends AnyPointerWrap[GraphPointer] {
	// Onto has sub-variants:  GP_Open, GP_Release, GP_Snapshot - which from java perspective are more like immutable state than a type.
	def getGraphPointer : GraphPointer = getPointer
	def makeOpen : GraphPointerWrap
	def makeRelease : GraphPointerWrap
	def makeSnapshot : GraphPointerWrap
}

trait CPathEntityWrap[CEType <: ClasspathEntity]

trait MvnArtWrap[MAType <: MavenArtifact]

case class FunnyTestAct(op : GraphOpWrap[GraphOp])

// Idea is that we can build up an executable test plan using queries on a set of test data acts, which may itself
// be partly generated through inference rules.
case class FunnyTestStep(myPrevSteps : Set[FunnyTestStep], myAct : FunnyTestAct)
 

// Here is the "old" Scala API summary, which we still rely on as of 2015-Jan.
// http://docs.scala-lang.org/overviews/core/actors.html
// We are trying to limit the fanciness of our Actors code, so that it will work with minimal changes under Scala 2.11.
class WritingActor  extends scala.actors.Actor with VarargsLogging {
	// The lifecycle of the actor 
	override  def act : Unit = {
		var myDoneFlag : Boolean = false

		// self is a method on the companion object
		def getSelf = scala.actors.Actor.self
		// val selfThing = "[self undefined in legacy Actors]"
		// Loop and process messages until the doneMarker is set.
		while (!myDoneFlag) {
			info2("WritingActor at top of loop for {} which is {}", getSelf, this)
			// Blocking "receive" call uses scala actors runtime to wait for a message for this actor, which is
			// then passed to the anonymous-partial-function contents of the block.
			receive {
				case ptmsg : PTMsg => { 
					info1("WritingActor got nice PTMsg: [{}]", ptmsg)
				}
				case stopMsg : StopMsg => {
					info2("WritingActor got StopMsg {}, setting myDoneFlag for {}", stopMsg, this)
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
					info1("WritingActor got other message: {}, absorbing it (rather than leaving 'unhandled')", other)
				}
			}
		}
		info2("WritingActor's work is done - exiting act() method for {} which is {}", getSelf, this)
    }
	
}
