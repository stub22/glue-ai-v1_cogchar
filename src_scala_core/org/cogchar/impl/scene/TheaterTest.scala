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
import scala.collection.mutable.HashMap;

import org.appdapter.core.log.{BasicDebugger, Loggable};
import org.appdapter.core.name.{Ident, FreeIdent};
import org.appdapter.core.item.{Item};
import org.appdapter.help.repo.{RepoClient}

import org.cogchar.api.perform.{Media, PerfChannel};
import org.cogchar.impl.perform.{DummyTextChan, FancyTime, PerfChannelNames};
import org.cogchar.platform.trigger.{CogcharScreenBox, CogcharActionTrigger, CogcharActionBinding, CogcharEventActionBinder};

/**  
 * @author Stu B. <www.texpedient.com>
 */

object TheaterTest extends BasicDebugger {

	def main(args: Array[String])  : Unit = {
		org.apache.log4j.BasicConfigurator.configure();
		org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.ALL);		
		test();
	}
		
	def test() : Unit = {
		val dbgCharName="PhonyChar";
		val dbgCharID = new FreeIdent(PerfChannelNames.NS_ccScnInst + dbgCharName, dbgCharName);
		val		thtr = new Theater(dbgCharID);

		// logInfo("Tricky: " + ChannelNames.getNumericChannelName("hmm", 22, 4));
		
		val dummySpeechChanID = PerfChannelNames.getOutChanIdent_SpeechMain();
		val dtc = new DummyTextChan(dummySpeechChanID);
		thtr.registerChannel(dtc);

		// val triplesFlexPath = "org/cogchar/test/assembly/ca_test.ttl";
		// val triplesFlexPath = "../org.cogchar.bundle.render.resources/src/main/resources/behavior/bhv_nugget_02.ttl";
		val triplesFlexPath = "org/cogchar/test/assembly/demo_scenes_A.ttl";
		loadSceneBookFromFile(thtr, triplesFlexPath, null, true);
		
		val sceneBook : SceneBook = thtr.getSceneBook;
		val ruledTestSceneName = "scn_004";
		val ruledTestSceneID : Ident = new FreeIdent(PerfChannelNames.NS_ccScnInst + ruledTestSceneName, ruledTestSceneName)
		val ruledTestSS : SceneSpec = sceneBook.findSceneSpec(ruledTestSceneID);
		
		val aSceneSpec : SceneSpec = sceneBook.allSceneSpecs().head;
		logInfo("Found first SceneSpec to build a trigger for: " + aSceneSpec);
		
		val trigHead : CogcharActionTrigger = org.cogchar.impl.trigger.FancyTriggerFacade.makeTriggerForScene(aSceneSpec);
	
		logInfo("Found ruled SceneSpec " + ruledTestSceneName + " to build a trigger for: " + ruledTestSS);
		
		val trigRuled : CogcharActionTrigger = org.cogchar.impl.trigger.FancyTriggerFacade.makeTriggerForScene(ruledTestSS);
		
		thtr.startThread();
		
		Thread.sleep(2000);
		trigHead.fire(thtr);
		// thtr.myBM.setDebugImportanceThreshold(Loggable.IMPO_LOLO);
		// thtr.myBM.runUntilDone(100);
		// 
		// 
		Thread.sleep(4000);
		logInfo("=======================================\nStarting ruled scene test");
		trigRuled.fire(thtr);
		Thread.sleep(4000);
		logInfo("********************** stopping thread");
		thtr.fullyStop(500);
		Thread.sleep(2000);
		
		logInfo("************************  BehaviorModulator Test #1 Finished ***************************************");
	}  
	def loadSceneBookFromFile(thtr : Theater, triplesFlexPath : String, optCL : ClassLoader, clearCachesFirst : Boolean ) {
		if (clearCachesFirst) {
			// Issue: this is a global operation, but we are loading one Theater/SceneBook per character.
			SceneBook.clearBuilderCaches();
		}
		val sceneBook = SceneBook.readSceneBookFromFile(triplesFlexPath, optCL);
		thtr.registerSceneBook(sceneBook);		
	}
	def loadSceneBookFromRepo(thtr : Theater, repoClient : RepoClient, chanGraphID : Ident, behavGraphID : Ident, clearCachesFirst : Boolean ) {
		if (clearCachesFirst) {
			// Issue: this is a global operation, but we are loading one Theater/SceneBook per character.
			SceneBook.clearBuilderCaches();
		}
		val sceneBook = SceneBook.readSceneBookFromRepo(repoClient, chanGraphID, behavGraphID);
		thtr.registerSceneBook(sceneBook);		
	}
	def loadTestSceneBook() : SceneBook = {
		val triplesFlexPath = "org/cogchar/test/assembly/demo_scenes_A.ttl";
		val sb = new SceneBook()
		// sb.loadSceneSpecsFromFile(triplesFlexPath, optResourceClassLoader);
		sb
		// val sb = 		thtr.loadSceneBookFromFile(triplesFlexPath, null, true);		
	} 
}

/*		val testSceneName = "scn_001";
//		val testSceneURI = 	SceneFieldNames.NS_ccScnInst + testSceneName;
//		val testSceneID =  new FreeIdent(testSceneURI, testSceneName);
		
		val testSceneSpec = mySceneBook.findSceneSpec(testSceneID);
		
		val scene = thtr.makeSceneFromBook(testSceneID);
		
		thtr.activateScene(scene);
*/	