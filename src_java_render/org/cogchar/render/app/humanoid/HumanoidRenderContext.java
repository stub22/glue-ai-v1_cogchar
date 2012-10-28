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

import org.cogchar.render.app.trigger.SceneActions;
import org.cogchar.platform.gui.keybind.KeyBindingConfig;
import org.cogchar.platform.gui.keybind.KeyBindingTracker;
import org.appdapter.core.name.Ident;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.input.FlyByCamera;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;

import java.util.HashMap;
import java.util.Map;
import org.cogchar.blob.emit.RenderConfigEmitter;
import org.cogchar.render.app.core.WorkaroundAppStub;
import org.cogchar.render.app.bony.BonyRenderContext;
import org.cogchar.api.humanoid.HumanoidConfig;
import org.cogchar.api.humanoid.HumanoidFigureConfig;
import org.cogchar.render.model.humanoid.HumanoidFigureModule;
import org.cogchar.render.model.humanoid.HumanoidFigure;
import org.cogchar.render.model.humanoid.HumanoidFigureManager;
import org.cogchar.render.sys.context.WorkaroundFuncsMustDie;
import org.cogchar.render.opengl.optic.CameraMgr;
// Below imports added for initHelpScreen - should go elsewhere eventually(?)
import com.jme3.font.BitmapText;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.concurrent.Callable;
import javax.swing.JFrame;
import org.appdapter.help.repo.RepoClient;
import org.cogchar.render.app.bony.BonyGameFeatureAdapter;
import org.cogchar.render.app.bony.BonyVirtualCharApp;
import org.cogchar.render.sys.task.BasicCallableRenderTask;
import org.cogchar.render.gui.bony.VirtualCharacterPanel;
import org.cogchar.render.sys.registry.RenderRegistryClient;
import org.cogchar.render.opengl.scene.FlatOverlayMgr;
import org.cogchar.render.sys.input.VW_InputBindingFuncs;
import org.cogchar.platform.trigger.CommandSpace;
/**
 * @author Stu B. <www.texpedient.com>
 */
public class HumanoidRenderContext extends BonyRenderContext {

	private	HumanoidFigureManager	myHFM;
	private BonyGameFeatureAdapter	myGameFeatureAdapter;

	public HumanoidRenderContext(RenderConfigEmitter rce) {
		super(rce);
		myGameFeatureAdapter = new BonyGameFeatureAdapter(this);
		myHFM = new HumanoidFigureManager();
	}

	@Override public void postInitLaunch() {
		super.postInitLaunch();

		/**
		 * Here is our best chance at placing initial content in the V-world, as part of module "start up", as perceived
		 * by the end user. We have historically created a lot of content here for test purposes (e.g. some cross hairs,
		 * some physical world features, the wacky red stick figure, some debug text displays).
		 *
		 * Now we are methodically tying all those debug features back into our AppdapterRepo-based config.
		 *
		 */
		AppSettings someSettings = getJMonkeyAppSettings();
		RenderRegistryClient rrc = getRenderRegistryClient();
		BonyGameFeatureAdapter.initCrossHairs(someSettings, rrc);
		initBasicTestPhysics();
		
		myGameFeatureAdapter.initFeatures();
		WorkaroundFuncsMustDie.initScoreBoard(this);
	}

	public void initCinematicParameters() {
		WorkaroundAppStub stub = getAppStub();
		stub.setAppSpeed(1.3f);  // BowlAtSinbad uses 1.3f - is defined in Application.java, is this physics related?
		FlyByCamera fbCam = stub.getFlyByCamera();
		fbCam.setMoveSpeed(50);
		//initLightsCameraCinematics();
	}

	// Formerly performed in postInitLaunch, this is now called from PumaAppContext once the KeyBindingConfig is complete
	// Might make sense to just move this to PumaAppContext

	public void refreshInputBindingsAndHelpScreen(KeyBindingConfig keyBindConfig, CommandSpace cspace) {
		RenderRegistryClient rrc = getRenderRegistryClient();
		VW_InputBindingFuncs.setupKeyBindingsAndHelpScreen(rrc, keyBindConfig, getAppStub(), this, 
					getJMonkeyAppSettings(), cspace);
	}


	// This is still called by HumanoidPuppetActions to reset default camera position
	public void setDefaultCameraLocation() {
		RenderRegistryClient rrc = getRenderRegistryClient();
		CameraMgr cmgr = rrc.getOpticCameraFacade(null);
		cmgr.resetDefaultCamera();
	}

	public BonyGameFeatureAdapter getGameFeatureAdapter() {
		return myGameFeatureAdapter;
	}

	public HumanoidFigureManager getHumanoidFigureManager() { 
		return myHFM;
	}

	/**
	 * Second (and most crucial) stage of OpenGL init. This method blocks until the canvas initialization is complete,
	 * which requires that the simpleInitApp() methods have all completed.
	 *
	 * @param wrapInJFrameFlag
	 * @throws Exception
	 */
	public void startOpenGLCanvas(boolean wrapInJFrameFlag) throws Exception {

		if (wrapInJFrameFlag) {
			VirtualCharacterPanel vcp = getPanel();
			logInfo("Making enclosing JFrame for VirtCharPanel: " + vcp);
			// Frame must be packed after panel created, but created  before startJMonkey.  
			// If startJMonkey is called first, we often hang in frame.setVisible() as JMonkey tries
			// to do some magic restart deal that doesn't work as of jme3-alpha4-August_2011.

			// During the Frame-pack portion of this method, we get all the way to:
			//  CogcharPresumedApp - ********************* DemoApp.initialize() called
			JFrame jf = vcp.makeEnclosingJFrame("CCRK-PUMA Virtual World");
			logInfo("Got Enclosing Frame, adding to BonyRenderContext for WindowClose triggering: " + jf);
			// Frame will receive a close event when org.cogchar.bundle.render.opengl is STOPPED
			setFrame(jf);
		}
		BonyVirtualCharApp app = getApp();

		if (app.isCanvasStarted()) {
			logWarning("JMonkey Canvas was already started!");
		} else {

			logInfo("Starting JMonkey canvas - hold yer breath! [[[[[[[[[[[[[[[[[[[[[[[[[[");
			app.startJMonkeyCanvas();
			logInfo("]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]  Finished starting JMonkey canvas!");
		}
	}
}
