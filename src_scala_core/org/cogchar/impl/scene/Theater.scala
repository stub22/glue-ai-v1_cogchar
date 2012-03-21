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

import org.appdapter.core.log.{BasicDebugger, Loggable};
import org.appdapter.core.item.{Ident, Item, FreeIdent};
import scala.collection.mutable.HashMap;

import org.cogchar.api.perform.{Channel};
import org.cogchar.impl.perform.{DummyTextChan, ChannelNames};

/**
 * @author Stu B. <www.texpedient.com>
 */

class Theater extends BasicDebugger {
	val		myBM = new BehaviorModulator();
}
object Theater extends BasicDebugger {
	def main(args: Array[String]) {
		val triplesFlexPath = "org/cogchar/test/assembly/ca_test.ttl";
		val t = new Theater();
		val sb = new SceneBook();
		val sceneSpecList : List[SceneSpec] = sb.loadSceneSpecs(triplesFlexPath, null);
		sb.registerSceneSpecs(sceneSpecList);
		val testSceneName = "scn_001";
		val testSceneURI = 	SceneFieldNames.NS_ccScnInst + testSceneName;
		val tsIdent =  new FreeIdent(testSceneURI, testSceneName);
		val tscs = sb.findSceneSpec(tsIdent);
		logInfo("Found scene:" + tscs)
		
		val dummySpeechChanID = new FreeIdent(ChannelNames.I_speechOut, ChannelNames.N_speechOut);
		val dtc = new DummyTextChan(dummySpeechChanID);
		val chanSet = new java.util.HashSet[Channel]();
		chanSet.add(dtc);
		
		val scene = new BScene(tscs);
		scene.wirePerformanceChannels(chanSet);
		
		scene.registerBehaviors(t.myBM);
		
		t.myBM.setDebugImportanceThreshold(Loggable.IMPO_LOLO);
		t.myBM.runUntilDone(100);
		
		logInfo("************************  BehaviorModulator Test #1 Finished ***************************************");
	}  
}
