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

package org.cogchar.impl.trigger

import org.appdapter.core.log.{BasicDebugger, Loggable};

import org.appdapter.core.item.{Ident, FreeIdent};
import org.cogchar.platform.trigger.{DummyBox, DummyTrigger, DummyBinding, DummyBinder};
import org.cogchar.impl.scene.{SceneSpec, BScene, SceneBook, Theater};

/**
 * @author Stu B. <www.texpedient.com>
 */

object FancyTrigger extends BasicDebugger {
	def makeTrigger(ss : SceneSpec) : DummyTrigger  = {
		val sceneID : Ident = ss.getIdent();
		val freeSceneID : FreeIdent = new FreeIdent(sceneID);
		val ndt = new DummyTrigger() { 
			// Note that we use *argument* theater, not enclosing one. 
			// So, this trigger can be used on any theater, but of course,
			// the matching sceneID must be found in the book of that theater!
			override def fire(db : DummyBox) : Unit = {
				val t : Theater = db.asInstanceOf[Theater];
				val scn : BScene = t.makeSceneFromBook(freeSceneID);
				t.activateScene(scn);
			}
		}
		ndt;
	}
	
	def registerTrigger(binder: DummyBinder, box: DummyBox, ss : SceneSpec) {
		val trigName : Option[String] = ss.myTrigName;
		trigName match {
			case	Some(tn) =>	{
				val trig = makeTrigger(ss);
				val binding = new FancyBinding(box, trig);
				binder.setBinding(tn, binding);
					
			}
			case _  => {
				logWarning("No trigName found for scene: " + ss);
			}
		}
	}
	def registerAllTriggers(binder: DummyBinder, box: DummyBox, sb : SceneBook) {
		for (val aSceneSpec : SceneSpec <- sb.mySceneSpecs.values) {
			registerTrigger(binder, box, aSceneSpec);
		}
	}
}
class FancyBinding(val myBox : DummyBox, val myTrig : DummyTrigger) extends DummyBinding {
	override def setTargetBox(box: DummyBox) {
		throw new RuntimeException("Cannot set target box on fancy binding");
	}
	override def setTargetTrigger(trig: DummyTrigger) {
		throw new RuntimeException("Cannot set target trigger on fancy binding");
	}
	override def perform() {
		if (myTrig != null) { 
			myTrig.fire(myBox);
		}
	}
}