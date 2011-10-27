/*
 *  Copyright 2011 by The Cogchar Project (www.cogchar.org).
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

package org.cogchar.render.opengl.bony.app;

import com.jme3.animation.AnimControl;
import com.jme3.scene.Node;
import java.util.List;
import org.cogchar.render.opengl.bony.model.SpatialManipFuncs;
import org.cogchar.render.opengl.bony.model.StickFigureTwister;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class BonyStickFigureApp extends BonyVirtualCharApp {
	protected StickFigureTwister		myTwister;	
	private String					mySceneFilePath;
	private float					myLocalSceneScale;
	

	
	public BonyStickFigureApp(String lwjglRendererName, int canvasWidth, int canvasHeight, String sceneFilePath, float sceneScale) {
		super(lwjglRendererName, canvasWidth, canvasHeight);
		mySceneFilePath = sceneFilePath;
		myLocalSceneScale = sceneScale;
		
	}
	
	@Override public void simpleInitApp() {
		super.simpleInitApp();
		if (mySceneFilePath != null) {
			initStickFigureModel();
		}
		myTwister = new StickFigureTwister(myContext);
	}
	public void setScoringFlag(boolean f) {
		myTwister.setScoringFlag(f);
	}

	public void initStickFigureModel() {
		// test1Node.setLocalScale(0.5f);
		Node testSceneNode = (Node) assetManager.loadModel(mySceneFilePath);
		System.out.println("BonyStickFigure scene loaded: " + testSceneNode);

		SpatialManipFuncs.dumpNodeTree(testSceneNode, "   ");
		List<AnimControl> animControls = SpatialManipFuncs.findAnimControls(testSceneNode);

		System.out.println("Found BSF animControls, about to reset: " + animControls);
		SpatialManipFuncs.resetBonesAndPrintInfo(animControls); 
		
		myContext.setAnimControls(animControls);

		// Material testSceneMat = new Material(assetManager, "resources/leo_hanson_tests/test3/test3.material");

		testSceneNode.setLocalScale(myLocalSceneScale);
		
		this.rootNode.attachChild(testSceneNode);
	}	
	@Override public void simpleUpdate(float tpf) {
		doUpdate(tpf);
	}
	private void doUpdate(float tpf) {
		myTwister.twist(tpf);
	}	
}
