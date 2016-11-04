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

package org.cogchar.blob.mbean

import org.appdapter.fancy.log.VarargsLogging
import scala.actors.Actor
import scala.reflect.BeanProperty
import java.lang.management.ManagementFactory
import javax.management.ObjectName
 
// Some definitions to simplify the code
private object JMXHelpers {
    implicit def string2objectName(name:String):ObjectName = new ObjectName(name)
    def jmxRegister(ob:Object, obname:ObjectName) =
      ManagementFactory.getPlatformMBeanServer.registerMBean(ob, obname)
}
import java.beans.ConstructorProperties; 

case class Booger @ConstructorProperties(Array("lastGreetingDate",  "greeting",  "sender", "funSet")) (
				@BeanProperty val lastGreetingDate: java.util.Date,
				@BeanProperty val greeting: String,
				@BeanProperty val sender: String,
				@BeanProperty val funSet: java.util.Set[String]) {
	
}

trait TrivialMXBean {
	def getBugCount : Int
	def doStuff : Unit
	def getOneBooger : Booger
	def getSomeBoogers : Array[Booger]
	def sendMeOneBooger(b : Booger)  // JConsole does not know how to offer a Booger construction form
	def sendMePrimdat( s : String, i : Int, f : Float) // java.util.Date does not work (with JConsole)
}
// Can be named anything because it is implememting an MXBean.
class TMBXImpl(val regName : String, initBC : Int) extends TrivialMXBean {
	// In theory this gives us an impl of getBugCount
	@BeanProperty	var bugCount : Int = initBC
	JMXHelpers.jmxRegister(this, JMXHelpers.string2objectName("JMXSandbox:name=" + regName))
	
	override def getOneBooger : Booger = {
		val someSet : Set[String] = Set("four", "eight")
		new Booger(new java.util.Date(), "howdy", "pally", scala.collection.JavaConversions.setAsJavaSet(someSet))
	}
	override def getSomeBoogers : Array[Booger] = {
		val setA : Set[String] = Set("three", "six")
		val setB : Set[String] = Set("seven", "two")
		val jsetA : java.util.Set[String] = scala.collection.JavaConversions.setAsJavaSet(setA)
		val jsetB : java.util.Set[String] = scala.collection.JavaConversions.setAsJavaSet(setB)
		val boogA = new Booger(null, "hail", "friend", jsetA)
		val boogB = new Booger(new java.util.Date(), "aloha", "amigo", jsetB)
		Array[Booger](boogA, boogB)
	}
	override def doStuff:Unit = {
		OMBTest.info1("TMBXImpl {} is doing stuff", regName)
	}
	override def sendMeOneBooger(b : Booger) = {
		OMBTest.info2("TMBXImpl {} received a booger {}", regName, b)
	}
	override def sendMePrimdat( s : String, i : Int, f : Float) = {
		OMBTest.info4("TMBXImpl {} received s={}, i={}, f={}", regName, s, i : Integer, f : java.lang.Float)
	}
}
object OMBTest extends VarargsLogging {
	def main(args: Array[String]) : Unit = {
		// Must enable "compile" or "provided" scope for Log4J dep in order to compile this code.
		org.apache.log4j.BasicConfigurator.configure();
		org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.ALL);
		info0("Starting OMBTest")
		val tmbxi_1 = new TMBXImpl("tmbxi_a1", 872)
		val tmbxi_2 = new TMBXImpl("tmbxi_b2", 369)
		Thread.sleep(60 * 60 * 1000)
	}
	
}

/*
 Related to the last point, another potential advantage of the MXBean over the Standard MBean is 
 that an MXBean implementation class can be in a different package than its defining MXBean interface.
 
 https://github.com/typesafehub/activator-akka-jmx-example/blob/master/src/main/scala/jmxexample/GreeterMXBean.scala
 
 * class GreetingHistoryMXView @ConstructorProperties(Array(
  "lastGreetingDate",
  "greeting",
  "sender")
) private (@BeanProperty val lastGreetingDate: java.util.Date,
  @BeanProperty val greeting: String,
  @BeanProperty val sender: String,
  @BeanProperty val randomSet: java.util.Set[String])
  
 
 // http://stackoverflow.com/a/24840520/5266
  def scalaToJavaSetConverter[T](scalaSet: Set[T]): java.util.Set[String] = {
    val javaSet = new java.util.HashSet[String]()
    scalaSet.foreach(entry => javaSet.add(entry.toString))
    javaSet
  }
 */