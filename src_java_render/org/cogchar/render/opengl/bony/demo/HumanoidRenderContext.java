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
package org.cogchar.render.opengl.bony.demo;

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
import org.cogchar.render.opengl.app.AppStub;
import org.cogchar.render.opengl.bony.app.BonyStickFigureContext;
import org.cogchar.render.opengl.bony.model.HumanoidBoneConfig;
import org.cogchar.render.opengl.bony.model.HumanoidFigureModule;
import org.cogchar.render.opengl.bony.model.HumanoidFigure;
import org.cogchar.render.opengl.bony.sys.WorkaroundFuncsMustDie;
import org.cogchar.render.opengl.bony.world.ProjectileLauncher;
import org.cogchar.render.opengl.optic.CameraMgr;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class HumanoidRenderContext extends BonyStickFigureContext {
	private	Map<String, HumanoidFigure>		myFiguresByCharURI = new HashMap<String, HumanoidFigure>();

	
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
		
		WorkaroundFuncsMustDie.initScoreBoard(this);
	}

	public HumanoidFigure getHumanoidFigure(String charURI) {
		HumanoidFigure hf = myFiguresByCharURI.get(charURI);
		if (hf == null) {
			BonyConfigEmitter bce = getBonyConfigEmitter();
			hf = new HumanoidFigure(bce, charURI);
			myFiguresByCharURI.put(charURI, hf);
		}
		return hf;
	}

	private HumanoidFigure setupHumanoidFigure(String charURI, HumanoidBoneConfig hbc, boolean usePhysics) {
		HumanoidFigure figure = null;
		BonyConfigEmitter bce = getBonyConfigEmitter();
		AssetManager amgr = findJme3AssetManager(null);
		Node rootNode = findJme3RootDeepNode(null);
		PhysicsSpace ps = null;
		if (usePhysics) {
			ps = getPhysicsSpace();
		}
		
		String meshPath = bce.getMeshPathForChar(charURI);
		if (meshPath != null) {
			figure = getHumanoidFigure(charURI);
			
			figure.initStuff(hbc, amgr, rootNode, ps, meshPath);
			//VirtCharPanel vcp = getVCPanel();
			//vcp.setMaxChannelNum(hbc.getConfiguredBoneCount() - 1);

			HumanoidFigureModule hfm = new HumanoidFigureModule(figure, this);
			attachModule(hfm);
		//	figure.boogie();
		//	figure.becomePuppet();
		} else {
			getLogger().warn("Skipping humanoid mesh load for charURI: " + charURI);
		}
		return figure;
	}

	private void initHumanoidStuff() {
		BonyConfigEmitter bce = getBonyConfigEmitter();
		
		if (!bce.isMinimalSim()) {
			String sinbadURI = bce.SINBAD_CHAR_URI();
			HumanoidBoneConfig sinbadHBC = new HumanoidBoneConfig();
			sinbadHBC.addSinbadDefaultBoneDescs();
			HumanoidFigure sinbadFigure = setupHumanoidFigure(sinbadURI, sinbadHBC, true);
			sinbadFigure.movePosition(30.0f, 0.0f, -30.0f);
		}

		String extraRobotURI = bce.ZENO_R50_CHAR_URI();
		HumanoidBoneConfig robotHBC = new HumanoidBoneConfig();
		robotHBC.addZenoDefaultBoneDescs();
		HumanoidFigure robotFigure = setupHumanoidFigure(extraRobotURI, robotHBC, false);
		robotFigure.movePosition(0.0f, -5.0f, 0.0f);
	}

	private void initCameraAndLights() {
		AppStub stub = getAppStub();
        setDefaultCameraLocation();
		stub.setAppSpeed(1.3f);
		FlyByCamera fbCam = stub.getFlyCam();
		fbCam.setMoveSpeed(200);
		setupLight();	
	}
    protected void setDefaultCameraLocation(){
		CameraMgr cmgr = findOrMakeOpticCameraFacade(null);
		Camera defCam = cmgr.getCommonCamera(CameraMgr.CommonCameras.DEFAULT);
		defCam.setLocation(new Vector3f(0.0f, 40.0f, 80.0f));
		
		defCam.lookAt(new Vector3f(0.0f, 0.0f, 0.0f), Vector3f.UNIT_Y);
		// float camEulerAngles[] = {2.0f, 0.0f, 0.0f};
	//	Quaternion camRot = new Quaternion();
	//	camRot.fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_X);
	//	defCam.setRotation(camRot);
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
