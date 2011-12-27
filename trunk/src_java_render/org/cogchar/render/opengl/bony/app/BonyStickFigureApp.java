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

import org.cogchar.blob.emit.BonyConfigEmitter;
import org.cogchar.render.opengl.bony.sys.BonyRenderContext;
import org.cogchar.render.opengl.bony.sys.JmonkeyAssetLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * @author Stu B. <www.texpedient.com>
 */
public class BonyStickFigureApp extends BonyVirtualCharApp {
	static Logger theLogger = LoggerFactory.getLogger(BonyStickFigureApp.class);
	protected StickFigureTwister		myTwister;	

	
	public BonyStickFigureApp(BonyConfigEmitter bce) { // String lwjglRendererName, int canvasWidth, int canvasHeight, String sceneFilePath, float sceneScale) {
		super(bce); // lwjglRendererName, canvasWidth, canvasHeight);	
	}
	
	@Override public void simpleInitApp() {
		theLogger.info("simpleInitApp() - START");
		super.simpleInitApp();
		initStickFigureModel();
		BonyRenderContext bc = getBonyRenderContext();
		myTwister = new StickFigureTwister(bc);
		theLogger.info("simpleInitApp() - START");
	}
	public void setScoringFlag(boolean f) {
		myTwister.setScoringFlag(f);
	}

	public void initStickFigureModel() {
		// test1Node.setLocalScale(0.5f);
		BonyRenderContext bc = getBonyRenderContext();
		BonyConfigEmitter bce = getBonyConfigEmitter(); 
		String sceneFilePath = bce.getStickFigureScenePath();
		float sceneScale = bce.getStickFigureSceneScale();
		JmonkeyAssetLoader contentAL = getContentsAssetLoader();
		Node testSceneNode = (Node) contentAL.safelyLoadModel(sceneFilePath, true); //  assetManager.loadModel(sceneFilePath);
		theLogger.info("BonyStickFigure scene loaded: " + testSceneNode);

		SpatialManipFuncs.dumpNodeTree(testSceneNode, "   ");
		List<AnimControl> animControls = SpatialManipFuncs.findAnimControls(testSceneNode);

		theLogger.info("Found BSF animControls, about to reset: " + animControls);
		SpatialManipFuncs.resetBonesAndPrintInfo(animControls); 
		
		myContext.setAnimControls(animControls);

		// Material testSceneMat = new Material(assetManager, "resources/leo_hanson_tests/test3/test3.material");

		testSceneNode.setLocalScale(sceneScale);
		
		this.rootNode.attachChild(testSceneNode);
	}	
	@Override public void simpleUpdate(float tpf) {
		doUpdate(tpf);
	}
	private void doUpdate(float tpf) {
		myTwister.twist(tpf);
	}	
}
