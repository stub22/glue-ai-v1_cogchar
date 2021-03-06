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

package org.cogchar.impl.scene

import scala.actors._
// import Actor._
import org.appdapter.core.log.{BasicDebugger};

/**
 * Experimental code for actor messaging.
 * Not used for any Cogchar features, yet.
 * 
 * @author Stu B. <www.texpedient.com>
 */

class  WorkThing extends BasicDebugger {
	var emotedCount = 0	
	
	def getSelf = Actor.self
	def sendEmoteMsgLater(a : Actor, waitMsec : Long) : Unit = {
		log("Will sendEmoteMsgLater to " + a + " now starting NEW innerActor")
		val innerActor = Actor.actor 
		{
			log("innerActor[" + getSelf + "] has been created, and will now sleep for " + waitMsec + " msec")
			Thread.sleep(waitMsec)
			log("innerActor[" + getSelf + "] finished sleeping, now sending 'Emote' message to immutably saved mainActor")
			a ! "Emote"
			log("innerActor[" + getSelf + "] finished sending message, and mutable emotedCount is now " + emotedCount + ", sleeping two sec")
			Thread.sleep(2000)
			log("innerActor[" + getSelf + "] finished SECOND nap!")
		}
		log ("emoteLater built innerActor[" + innerActor + "] and emotedCount is now " + emotedCount);
	}
	def startSillyActor() : Actor = {
		val sillyActor = Actor.actor {
			log ("sillyActor[" + getSelf + "] - first line of syntax")

			log("sillyActor - calling sendEmoteMsgLater() for the first time")
	
			sendEmoteMsgLater(getSelf, 1000);
		
			log("sillyActor - entering 'loop' dispatch construct")
			Actor.loop {
				log("sillyActor is at top of its 'loop', about to enter 'react', which does not return, but is called repeatedly")
				Actor.react {
					case "Emote" =>
						emotedCount += 1
						log("sillyActor[" + getSelf + "] is reacting to an Emote message, emoted count is now " + emotedCount)
						if (emotedCount < 5) {
							log("sillyActor is calling emoteLater()")
							sendEmoteMsgLater(getSelf, emotedCount * 1000);
							log("sillyActor finished calling emoteLater()")
						} else {
							log("sillyActor[" + getSelf + " has now reacted 5 times, so we're not sending another message")
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
				log("*************************** this method call after 'react' is never reached - WE THINK!")
			}
			// This doesn't happen, process remains running
			log("sillyActor - ************************************  passed the loop construct")
		}
		log("startSillyActor() is returning new sillyActor to caller")
		sillyActor
	}
	def log(txt: String) {
		getLogger.info(txt)
	}
}
object BehaviorTrial extends BasicDebugger {
	def log(txt : String) = {
		val tstamp = System.currentTimeMillis();
		val sec = tstamp / 1000
		val msec = tstamp - sec * 1000
		getLogger.info("T[" + sec % 10000 + "." + msec + "] " + txt)
	}


	def actThreadingTest() { 
		log("actThreadingTest() START ------------------- ")
		log("System.properties=" + System.getProperties());
		log("The sillyActor is started automatically when using the 'actor' utility method from class Actor");
		val wt = new WorkThing;
		val sa = wt.startSillyActor();
		log("Started sillyActor=" + sa)
		// org.cogchar.test.assembly.AssemblyTest.main(null);
		log("main() sleeping for 2.5 sec")
		Thread.sleep(2500)
		log("main sending to sillyActor")
		sa ! "Hey, this message is from main(), and I am the boss of you, mr SillyActor"
		// org.cogchar.test.assembly.AssemblyTest.main(null);		
		log("actThreadingTest() END ------------------- ")

	}
	
	def main(args: Array[String]) : Unit = {
		// Must enable "compile" or "provided" scope for Log4J dep in order to compile this code.
		org.apache.log4j.BasicConfigurator.configure();
		org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.ALL);
		println("Configured Log4J, now running test")
		actThreadingTest() 
		println("Test is done")
	}
}
