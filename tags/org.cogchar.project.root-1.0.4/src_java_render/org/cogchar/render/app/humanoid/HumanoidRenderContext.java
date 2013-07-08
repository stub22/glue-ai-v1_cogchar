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
package org.cogchar.render.app.humanoid;

import org.appdapter.core.item.Ident;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.input.FlyByCamera;
import com.jme3.input.InputManager;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;
import java.lang.String;
import java.util.HashMap;
import java.util.Map;
import org.cogchar.blob.emit.BonyConfigEmitter;
import org.cogchar.render.app.core.AppStub;
import org.cogchar.render.app.bony.BonyStickFigureContext;
import org.cogchar.render.model.humanoid.HumanoidBoneConfig;
import org.cogchar.render.model.humanoid.HumanoidFigureModule;
import org.cogchar.render.model.humanoid.HumanoidFigure;
import org.cogchar.render.sys.core.WorkaroundFuncsMustDie;
import org.cogchar.render.sys.physics.ProjectileLauncher;
import org.cogchar.render.opengl.optic.CameraMgr;
// Below imports added for initHelpScreen - should go elsewhere eventually
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class HumanoidRenderContext extends BonyStickFigureContext {
	private	Map<Ident, HumanoidFigure>		myFiguresByCharIdent = new HashMap<Ident, HumanoidFigure>();

	
	public HumanoidRenderContext(BonyConfigEmitter bce) {
		super(bce);
	}
	@Override public void completeInit() { 
		super.completeInit();

		AppSettings someSettings = getJMonkeyAppSettings();
		initCrossHairs(someSettings);
		initBasicTestPhysics();
		initHumanoidStuff();
		initCameraAndLights();
		InputManager inputManager = findJme3InputManager(null);

		HumanoidPuppetActions.setupActionListeners(inputManager, this);	
		SceneActions.setupActionListeners(inputManager);
		
		WorkaroundFuncsMustDie.initScoreBoard(this);
                
		initHelpScreen(someSettings, inputManager);
	}

	public HumanoidFigure getHumanoidFigure(Ident charIdent) {
		HumanoidFigure hf = myFiguresByCharIdent.get(charIdent);
		if (hf == null) {
			BonyConfigEmitter bce = getBonyConfigEmitter();
			hf = new HumanoidFigure(bce, charIdent);
			myFiguresByCharIdent.put(charIdent, hf);
		}
		return hf;
	}

	private HumanoidFigure setupHumanoidFigure(Ident charIdent, HumanoidBoneConfig hbc, boolean usePhysics) {
		HumanoidFigure figure = null;
		BonyConfigEmitter bce = getBonyConfigEmitter();
		AssetManager amgr = findJme3AssetManager(null);
		Node rootNode = findJme3RootDeepNode(null);
		PhysicsSpace ps = null;
		if (usePhysics) {
			ps = getPhysicsSpace();
		}
		
		String meshPath = bce.getMeshPathForChar(charIdent);
		if (meshPath != null) {
			figure = getHumanoidFigure(charIdent);
			
			figure.initStuff(hbc, amgr, rootNode, ps, meshPath);
			//VirtCharPanel vcp = getVCPanel();
			//vcp.setMaxChannelNum(hbc.getConfiguredBoneCount() - 1);

			HumanoidFigureModule hfm = new HumanoidFigureModule(figure, this);
			attachModule(hfm);
		//	figure.boogie();
		//	figure.becomePuppet();
		} else {
			getLogger().warn("Skipping humanoid mesh load for charURI: " + charIdent);
		}
		return figure;
	}

	private void initHumanoidStuff() {
		BonyConfigEmitter bce = getBonyConfigEmitter();
		
		if (!bce.isMinimalSim()) {
			Ident sinbadIdent = bce.SINBAD_CHAR_IDENT();
			HumanoidBoneConfig sinbadHBC = new HumanoidBoneConfig();
			sinbadHBC.addSinbadDefaultBoneDescs();
			HumanoidFigure sinbadFigure = setupHumanoidFigure(sinbadIdent, sinbadHBC, true);
			sinbadFigure.movePosition(30.0f, 0.0f, -30.0f);
		}

		Ident extraRobotIdent = bce.ZENO_R50_CHAR_IDENT();
		HumanoidBoneConfig robotHBC = new HumanoidBoneConfig();
		robotHBC.addZenoDefaultBoneDescs();
		HumanoidFigure robotFigure = setupHumanoidFigure(extraRobotIdent, robotHBC, false);
		robotFigure.movePosition(0.0f, -5.0f, 0.0f);
	}

	// This method is getting to be vestigial - camera and light setup is now mostly handled from RDF	
	private void initCameraAndLights() {
		AppStub stub = getAppStub();
		stub.setAppSpeed(1.3f);  // BowlAtSinbad uses 1.3f - is defined in Application.java, is this physics related?
		FlyByCamera fbCam = stub.getFlyCam();
		fbCam.setMoveSpeed(50);	
	}
        
	// This is still called by HumanoidPuppetActions to reset default camera position
	protected void setDefaultCameraLocation(){    
		CameraMgr cmgr = findOrMakeOpticCameraFacade(null);
		cmgr.resetDefaultCamera();
	}
        
	public void toggleDebugSkeletons() { 
		for (HumanoidFigure hf : myFiguresByCharIdent.values()) {
			hf.toggleDebugSkeleton();
		}
	}
        
	// Does this best live here or further up in one of the context superclasses? Dunno, but should be easy enough to move it up later (w/o private); be sure to remove imports
	// In order to access registry, must live in a class that extends CogcharRenderContext
	private void initHelpScreen(AppSettings settings, InputManager inputManager) {
		final String HELP_TAG = "Help"; // Should be defined elsewhere or perhaps via RDF for goodness, but just for the moment
		final int HELP_KEY = com.jme3.input.KeyInput.KEY_H; // Same here - coming from RDF eventually
		KeyBindingTracker.addBinding(HELP_TAG, HELP_KEY); // Let's add ourselves to the help list!
		final BitmapText helpBT = findOrMakeSceneTextFacade(null).makeHelpScreen(0.6f, settings); // First argument sets text size, really shouldn't be hard-coded
		KeyTrigger keyTrig = new KeyTrigger(HELP_KEY); 
		inputManager.addMapping(HELP_TAG, keyTrig);
		inputManager.addListener(new ActionListener() {
			private boolean helpDisplayed = false;
			public void onAction(String name, boolean isPressed, float tpf) {
				if (isPressed) {
					if (!helpDisplayed) {findOrMakeSceneFlatFacade(null).attachOverlaySpatial(helpBT); helpDisplayed = true;}
						else {findOrMakeSceneFlatFacade(null).detachOverlaySpatial(helpBT); helpDisplayed = false;}   
				}        
			}
		}, HELP_TAG);
	}
        
		/*
		 * 
		 * The JME3 docs below are a horrible, inconsistent, incomplete, incorrect mess:
		 * 
		 * 
		 * http://jmonkeyengine.org/javadoc/com/jme3/math/Quaternion.html#Quaternion(float[])
		 * 
		 * public Quaternion fromAngles(float yaw,
                             float roll,
                             float pitch)
fromAngles builds a Quaternion from the Euler rotation angles (y,r,p). Note that we are applying in order: roll, pitch, yaw 
		 * 
		 * but we've ordered them in x, y, and z for convenience.
		 * 
Parameters:
yaw - the Euler yaw of rotation (in radians). (aka Bank, often rot around x)
roll - the Euler roll of rotation (in radians). (aka Heading, often rot around y)
pitch - the Euler pitch of rotation (in radians). (aka Attitude, often rot around z)
		 * 
		 * 
		 * 
		 * 
		 * 
		 */


	
	
}