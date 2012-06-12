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
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;
import com.jme3.math.Vector3f;
import java.lang.String;
import java.util.HashMap;
import java.util.Map;
import org.cogchar.blob.emit.BonyConfigEmitter;
import org.cogchar.render.app.core.WorkaroundAppStub;
import org.cogchar.render.app.bony.BonyRenderContext;
import org.cogchar.api.humanoid.HumanoidBoneConfig;
import org.cogchar.api.humanoid.HumanoidFigureConfig;
import org.cogchar.render.model.humanoid.HumanoidFigureModule;
import org.cogchar.render.model.humanoid.HumanoidFigure;
import org.cogchar.render.sys.core.WorkaroundFuncsMustDie;
import org.cogchar.render.opengl.optic.CameraMgr;
// Below imports added for initHelpScreen - should go elsewhere eventually
import com.jme3.font.BitmapText;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import javax.swing.JFrame;
import org.cogchar.render.app.bony.BonyGameFeatureAdapter;
import org.cogchar.render.app.bony.BonyVirtualCharApp;
import org.cogchar.render.gui.bony.VirtualCharacterPanel;
import org.cogchar.render.sys.core.RenderRegistryClient;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class HumanoidRenderContext extends BonyRenderContext {

	private Map<Ident, HumanoidFigure> myFiguresByCharIdent = new HashMap<Ident, HumanoidFigure>();
	private BonyGameFeatureAdapter myGameFeatureAdapter;

	public HumanoidRenderContext(BonyConfigEmitter bce) {
		super(bce);
		myGameFeatureAdapter = new BonyGameFeatureAdapter(this);
	}

	@Override public void postInitLaunch() {
		super.postInitLaunch();

		AppSettings someSettings = getJMonkeyAppSettings();
		RenderRegistryClient rrc = getRenderRegistryClient();
		BonyGameFeatureAdapter.initCrossHairs(someSettings, rrc);
		initBasicTestPhysics();
		// We wait and do this later, possibly repeatedly.
		// initHumanoidStuff();
		initCameraAndLights();

		myGameFeatureAdapter.initFeatures();

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
			HumanoidFigureConfig hfc = bce.getHumanoidFigureConfigForChar(charIdent); 
			if (hfc.isComplete()) {
				hf = new HumanoidFigure(hfc);
				myFiguresByCharIdent.put(charIdent, hf);
			}
		}
		return hf;
	}


	public HumanoidFigure setupHumanoidFigure(Ident charIdent) {
		HumanoidFigure figure = getHumanoidFigure(charIdent);
		AssetManager amgr = findJme3AssetManager(null);
		Node rootNode = findJme3RootDeepNode(null);
		PhysicsSpace ps = getPhysicsSpace();
		figure.initStuff(amgr, rootNode, ps);
		HumanoidFigureModule hfm = new HumanoidFigureModule(figure, this);
		attachModule(hfm);
		return figure;
	}

	public void initHumanoidStuff() {
		BonyConfigEmitter bce = getBonyConfigEmitter();
		try {
			if (!bce.isMinimalSim()) {
				setupHumanoidFigure(bce.SINBAD_CHAR_IDENT());
			}
			if (bce.isZenoHome()) {
				setupHumanoidFigure (bce.ZENO_R50_CHAR_IDENT());
			}
		} catch (Throwable t) {
			logError("Problem in initHumanoidStuff(), eating exception to allow init to continue", t);
		}
	}

	// This method is getting to be vestigial - camera and light setup is now mostly handled from RDF	
	private void initCameraAndLights() {
		WorkaroundAppStub stub = getAppStub();
		stub.setAppSpeed(1.3f);  // BowlAtSinbad uses 1.3f - is defined in Application.java, is this physics related?
		FlyByCamera fbCam = stub.getFlyCam();
		fbCam.setMoveSpeed(50);
	}

	// This is still called by HumanoidPuppetActions to reset default camera position
	protected void setDefaultCameraLocation() {
		CameraMgr cmgr = findOrMakeOpticCameraFacade(null);
		cmgr.resetDefaultCamera();
	}

	public void toggleDebugSkeletons() {
		for (HumanoidFigure hf : myFiguresByCharIdent.values()) {
			hf.toggleDebugSkeleton();
		}
	}

	public BonyGameFeatureAdapter getGameFeatureAdapter() {
		return myGameFeatureAdapter;
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
					if (!helpDisplayed) {
						findOrMakeSceneFlatFacade(null).attachOverlaySpatial(helpBT);
						helpDisplayed = true;
					} else {
						findOrMakeSceneFlatFacade(null).detachOverlaySpatial(helpBT);
						helpDisplayed = false;
					}
				}
			}
		}, HELP_TAG);
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
