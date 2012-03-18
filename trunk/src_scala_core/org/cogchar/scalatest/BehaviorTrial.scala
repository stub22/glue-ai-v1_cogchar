/*
 *  Copyright 2012 by The Cogchar Project (www.cogchar.org).
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

import scala.actors._
import Actor._

/**
 * @author Stu B. <www.texpedient.com>
 */

class  WorkThing {
	var emotedCount = 0	
	def sendEmoteMsgLater(a : Actor, waitMsec : Long) : Unit = {
		log("Will sendEmoteMsgLater to " + a + " now starting NEW innerActor")
		val innerActor = actor 
		{
			log("innerActor[" + self + "] has been created, and will now sleep for " + waitMsec + " msec")
			Thread.sleep(waitMsec)
			log("innerActor[" + self + "] finished sleeping, now sending 'Emote' message to immutably saved mainActor")
			a ! "Emote"
			log("innerActor[" + self + "] finished sending message, and mutable emotedCount is now " + emotedCount + ", sleeping two sec")
			Thread.sleep(2000)
			log("innerActor[" + self + "] finished SECOND nap!")
		}
		log ("emoteLater built innerActor[" + innerActor + "] and emotedCount is now " + emotedCount);
	}
	def startSillyActor() : Actor = {
		val sillyActor = actor {
			log ("sillyActor[" + self + "] - first line of syntax")

			log("sillyActor - calling sendEmoteMsgLater() for the first time")
	
			sendEmoteMsgLater(self, 1000);
		
			log("sillyActor - entering 'loop' dispatch construct")
			loop {
				log("sillyActor is at top of its 'loop', about to enter 'react', which does not return, but is called repeatedly")
				react {
					case "Emote" =>
						emotedCount += 1
						log("sillyActor[" + self + "] is reacting to an Emote message, emoted count is now " + emotedCount)
						if (emotedCount < 5) {
							log("sillyActor is calling emoteLater()")
							sendEmoteMsgLater(self, emotedCount * 1000);
							log("sillyActor finished calling emoteLater()")
						} else {
							log("sillyActor[" + self + " has now reacted 5 times, so we're not sending another message")
						}
						log("sillyActor has finished processing the 'Emote' message, emotedCount is now " + emotedCount)
						log("This particular invocation of react() is now ending, so control passes back into the actor-system loop construct")
					case msg =>
						log("************** sillyActor received OTHER message: " + msg + ", sleeping 15 sec, thereby blocking other messages")
						Thread.sleep(15000);
						log("sillyActor default message processor woke back up in middle of message processing")
				}
				/*  
				 *  http://stackoverflow.com/questions/1251666/scala-actors-receive-vs-react
				 *
				 *  As it is, the framework rather cleverly ends a react by throwing a SuspendActorException, which 
				 *  is caught by the looping code which then runs the react again via the andThen method.
				 */
				log("*************************** this method call after 'react' is never reached")
			}
			// This doesn't happen, process remains running
			log("sillyActor - ************************************  passed the loop construct")
		}
		log("startSillyActor() is returning new sillyActor to caller")
		sillyActor
	}
	def log(txt: String) {
		BehaviorTrial.log(txt)
	}
}
object BehaviorTrial {
	def log(txt : String) = {
		val tstamp = System.currentTimeMillis();
		val sec = tstamp / 1000
		val msec = tstamp - sec * 1000
		println("T[" + sec % 10000 + "." + msec + "] " + txt)
	}
	import org.appdapter.bind.rdf.jena.model.AssemblerUtils;
	
	def loadSceneSpecs() : List[SceneSpec] = { 
		// Set[Object] 
		val triplesPath = "org/cogchar/test/assembly/ca_test.ttl";
		val loadedStuff = AssemblerUtils.buildAllObjectsInRdfFile(triplesPath);
		log("Loaded " + loadedStuff.size() + " objects");
		log("Stuff: " + loadedStuff);
		val si = loadedStuff.iterator();
		var sceneSpecList = List[SceneSpec]()
		while (si.hasNext()) {
			val obj = si.next();
			if (obj.isInstanceOf[SceneSpec]) {
				sceneSpecList = sceneSpecList :+ obj.asInstanceOf[SceneSpec]
			}
		}
		println("===========================================================================================")
		println("SceneList: " + sceneSpecList);
		sceneSpecList;
		// for (Object o : loadedStuff) {
	}
	def actThreadingTest() { 
		log("actThreadingTest() START ------------------- ")
		log("System.properties=" + System.getProperties());
		log("The sillyActor is started automatically using the 'actor' utility method from class Actor");
		val wt = new WorkThing;
		val sa = wt.startSillyActor();
		log("Started sillyActor=" + sa)
		org.cogchar.test.assembly.AssemblyTest.main(null);
		log("main() sleeping for 2.5 sec")
		Thread.sleep(2500)
		log("main sending to sillyActor")
		sa ! "Hey, this message is from main(), and I am the boss of you, mr SillyActor"
		org.cogchar.test.assembly.AssemblyTest.main(null);		
		log("actThreadingTest() END ------------------- ")

	}
	def main(args: Array[String]) {
		val sceneList : List[SceneSpec] = loadSceneSpecs();
	}  
}
