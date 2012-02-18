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
package org.cogchar.render.opengl.bony.app;

import com.jme3.animation.AnimControl;
import com.jme3.bullet.BulletAppState;
import com.jme3.input.InputManager;
import com.jme3.scene.Node;
import java.util.List;
import org.cogchar.blob.emit.BonyConfigEmitter;
import org.cogchar.render.opengl.bony.gui.VirtualCharacterPanel;
import org.cogchar.render.opengl.bony.model.DemoBonyWireframeRagdoll;
import org.cogchar.render.opengl.bony.model.SpatialManipFuncs;
import org.cogchar.render.opengl.bony.model.StickFigureTwister;
import org.cogchar.render.opengl.bony.sys.BonyRenderContext;
import org.cogchar.render.opengl.scene.ModelSpatialFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class BonyStickFigureContext extends BonyRenderContext {
	
	protected	StickFigureTwister			myTwister;	
	private		DemoBonyWireframeRagdoll	myExtraRagdoll;		
	
	public BonyStickFigureContext(BonyConfigEmitter bce) { 
		super(bce);
	}
	
	@Override public void completeInit() {
		super.completeInit();
		if (initStickFigureModel()) {
			myTwister = new StickFigureTwister(this);
			VirtualCharacterPanel vcp = getPanel();
			BodyController bodCont = vcp.getBodyController();
			if (bodCont != null) {
				myTwister.setBodyController(bodCont);
			}
			attachModule(myTwister);
		}
		initExtraRagdoll();
	}	
	public boolean initStickFigureModel() {
		// test1Node.setLocalScale(0.5f);

		BonyConfigEmitter bce = getBonyConfigEmitter(); 
		String sceneFilePath = bce.getStickFigureScenePath();
		
		if (sceneFilePath != null) {
			float sceneScale = bce.getStickFigureSceneScale();
			
			ModelSpatialFactory msf = findOrMakeSceneSpatialModelFacade(null);

			Node testSceneNode =  (Node) msf.makeSpatialFromMeshPath(sceneFilePath);
			getLogger().info("BonyStickFigure scene loaded: " + testSceneNode);

			SpatialManipFuncs.dumpNodeTree(testSceneNode, "   ");
			List<AnimControl> animControls = SpatialManipFuncs.findAnimControls(testSceneNode);

			getLogger().info("Found BSF animControls, about to reset: " + animControls);
			SpatialManipFuncs.resetBonesAndPrintInfo(animControls); 

			setAnimControls(animControls);

			// Material testSceneMat = new Material(assetManager, "resources/leo_hanson_tests/test3/test3.material");

			testSceneNode.setLocalScale(sceneScale);
				
			Node rootNode = findJme3RootDeepNode(null);
			rootNode.attachChild(testSceneNode);
			return true;
		} else {
			getLogger().warn("Skipping load for BonyStickFigure - gui controls should also be disabled");
			return false;
		}
		
	}		
	public void setScoringFlag(boolean f) {
		myTwister.setScoringFlag(f);
	}
	public boolean initExtraRagdoll() { 
		myExtraRagdoll = new DemoBonyWireframeRagdoll();
		BulletAppState bulletAppState = getBulletAppState();
		Node rootNode = findJme3RootDeepNode(null);
		myExtraRagdoll.realizeDollAndAttach(rootNode, bulletAppState);
		InputManager imgr = findJme3InputManager(null);
		myExtraRagdoll.registerTraditionalInputHandlers(imgr);
		attachModule(myExtraRagdoll);
		return true;
	}
}
