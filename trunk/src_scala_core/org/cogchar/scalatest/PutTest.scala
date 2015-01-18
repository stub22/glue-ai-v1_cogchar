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
		
		val outPuss = new WritingActor()
		// Starting amigo right away keeps us in compliance with Akka impl required in Scala 2.11.
		// http://docs.scala-lang.org/overviews/core/actors-migration-guide.html
		// Bullet #4
		// "Akka actors are automatically started when instantiated. Users will have to reshape their 
		// system so it starts all the actors right after their instantiation."
		// Also note that it is this first start call which appears to fire up the Actors threading system.
		// Without it, the program will exit immediately after main() ends.
		outPuss.start
		outPuss ! "why not just send a string, eh?"
		outPuss ! 25
		outPuss ! PTMsg("text inside a nice little PTMsg case class")
		outPuss ! StopMsg()
		outPuss ! PTMsg("Since the target WritingActor was already told to stop, this message will never be processed")
		outPuss ! PTMsg("Nor will this one")
	}
}
case class PTMsg(txt : String)

case class StopMsg()

// Here is the "old" Scala API summary, which we still rely on as of 2015-Jan.
// http://docs.scala-lang.org/overviews/core/actors.html
// We are trying to limit the fanciness of our Actors code, so that it will work with minimal changes under Scala 2.11.
class WritingActor  extends scala.actors.Actor with VarargsLogging {
	override  def act : Unit = {
		var myDoneFlag : Boolean = false
		// Loop and process messages
		val selfThing = "[self undefined in legacy Actors]"
		while (!myDoneFlag) {
			info2("WritingActor at top of loop for {} which is {}", selfThing, this)
			receive {
				// Blocking "receive" call uses scala actors runtime to wait for a message for this actor.
				case ptmsg : PTMsg => { 
					info1("WritingActor got nice PTMsg: [{}]", ptmsg)
				}
				case stopMsg : StopMsg => {
					info2("WritingActor got StopMsg {}, setting myDoneFlag for {}", stopMsg, this)
					myDoneFlag = true
				} 
				case other => {
					info1("WritingActor got other message: {}, absorbing it (rather than leaving 'unhandled')", other)
				}
			}
		}
		info2("WritingActor's work is done - exiting act() method for {} which is {}", selfThing, this)
    }
	
}
