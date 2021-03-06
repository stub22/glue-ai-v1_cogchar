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
package org.cogchar.render.app.bony;

import com.jme3.animation.AnimControl;
import com.jme3.font.BitmapText;
import com.jme3.input.InputManager;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;
import java.util.List;
import org.cogchar.blob.emit.RenderConfigEmitter;
import org.cogchar.render.gui.bony.VirtualCharacterPanel;
import org.cogchar.render.model.bony.DemoBonyWireframeRagdoll;
import org.cogchar.render.model.bony.SpatialManipFuncs;
import org.cogchar.render.model.bony.StickFigureTwister;
import org.cogchar.render.opengl.optic.CameraMgr;
import org.cogchar.render.opengl.scene.ModelSpatialFactory;
import org.cogchar.render.sys.context.CoreFeatureAdapter;
// import org.cogchar.render.goody.physical.ProjectileLauncher;
import org.cogchar.render.sys.registry.RenderRegistryClient;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class BonyGameFeatureAdapter extends CoreFeatureAdapter {
	
	private		BonyRenderContext			myBRC;
	
	protected	StickFigureTwister			myTwister;	
	private		DemoBonyWireframeRagdoll	myExtraRagdoll;		
	

	
	private		boolean						myExtrasFlag;

	public BonyGameFeatureAdapter(BonyRenderContext brc) {
		super(brc);
		myBRC = brc;
	}
	protected RenderRegistryClient getRenderRegistyClient() { 
		return myBRC.getRenderRegistryClient();
	}
	public void initFeatures() {
		// When enabled, this call creates the extra winged red thing.
		// initExtraStickFigureModel()
		
		myTwister = new StickFigureTwister(myBRC);
		VirtualCharacterPanel vcp = myBRC.getPanel();
		BodyController bodCont = vcp.getBodyController();
		if (bodCont != null) {
			myTwister.setBodyController(bodCont);
		}
		myBRC.attachModule(myTwister);
			
		RenderConfigEmitter bce = myBRC.getConfigEmitter(); 
		if (!bce.isMinimalSim()) {
			initExtraRagdoll();
		}
		

		
	}
	public void toggleAnnoyingStuff() {
		
	}
	public boolean initExtraStickFigureModel() {
		// test1Node.setLocalScale(0.5f);

		RenderConfigEmitter rce = myBRC.getConfigEmitter(); 
		getLogger().info("rce.isMinimalSim={}", rce.isMinimalSim());
		String sceneFilePath = rce.getStickFigureScenePath();
		
		if (sceneFilePath != null) {
			float sceneScale = rce.getStickFigureSceneScale();
			
			RenderRegistryClient rrc = myBRC.getRenderRegistryClient();
			ModelSpatialFactory msf = rrc.getSceneSpatialModelFacade(null);

			Node testSceneNode =  (Node) msf.makeSpatialFromMeshPath(sceneFilePath);
			getLogger().info("BonyStickFigure scene loaded");
			getLogger().debug("Full scene dump: ", testSceneNode);

			SpatialManipFuncs.dumpNodeTree(testSceneNode, "   ");
			List<AnimControl> animControls = SpatialManipFuncs.findAnimControls(testSceneNode);

			getLogger().info("Found BSF animControls, about to reset");
			getLogger().debug("Full ctrls dump: ", animControls);
			SpatialManipFuncs.resetBonesAndPrintInfo(animControls); 

			myBRC.setAnimControls(animControls);

			// Material testSceneMat = new Material(assetManager, "resources/leo_hanson_tests/test3/test3.material");

			testSceneNode.setLocalScale(sceneScale);
				
			Node rootNode = rrc.getJme3RootDeepNode(null);
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
		RenderRegistryClient rrc = myBRC.getRenderRegistryClient();
		myExtraRagdoll = new DemoBonyWireframeRagdoll();

		Node rootNode = rrc.getJme3RootDeepNode(null);
		myExtraRagdoll.realizeDollAndAttach(rootNode, rrc.getJme3BulletPhysicsSpace());
		InputManager imgr = rrc.getJme3InputManager(null);
		myExtraRagdoll.registerTraditionalInputHandlers(imgr);
		myBRC.attachModule(myExtraRagdoll);
		return true;
	}
	/*
	public ProjectileLauncher getProjectileMgr() { 
		return myPrjctlMgr;
	}	
	*/

	
	// Now added as a Goody, but this method retained for demo projects
	// A centred plus sign to help the player aim.
	static public void initCrossHairs(AppSettings settings, RenderRegistryClient rrc) {
		rrc.getSceneFlatFacade(null).detachAllOverlays();
		BitmapText crossBT = rrc.getSceneTextFacade(null).makeCrossHairs(2.0f, settings);
		rrc.getSceneFlatFacade(null).attachOverlaySpatial(crossBT);
	}


		/*	
	public void cmdBoom() {

		Geometry prjctlGeom = new Geometry(GEOM_BOOM, myProjectileSphereMesh);
		prjctlGeom.setMaterial(myProjectileMaterial);
		prjctlGeom.setLocalTranslation(cam.getLocation());
		prjctlGeom.setLocalScale(myProjectileSize);
		myProjectileCollisionShape = new SphereCollisionShape(myProjectileSize);
		ThrowableBombRigidBodyControl prjctlNode = new ThrowableBombRigidBodyControl(assetManager, myProjectileCollisionShape, 1);
		prjctlNode.setForceFactor(8);
		prjctlNode.setExplosionRadius(20);
		prjctlNode.setCcdMotionThreshold(0.001f);
		prjctlNode.setLinearVelocity(cam.getDirection().mult(180));
		prjctlGeom.addControl(prjctlNode);
		rootNode.attachChild(prjctlGeom);
		getPhysicsSpace().add(prjctlNode);
		 * 
		
	}
*/	
}
