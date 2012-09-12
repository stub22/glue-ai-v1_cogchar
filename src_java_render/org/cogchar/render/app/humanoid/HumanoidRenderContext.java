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
import org.cogchar.render.sys.core.WorkaroundFuncsMustDie;
import org.cogchar.render.opengl.optic.CameraMgr;
// Below imports added for initHelpScreen - should go elsewhere eventually(?)
import com.jme3.font.BitmapText;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.concurrent.Callable;
import javax.swing.JFrame;
import org.cogchar.render.app.bony.BonyGameFeatureAdapter;
import org.cogchar.render.app.bony.BonyVirtualCharApp;
import org.cogchar.render.gui.bony.VirtualCharacterPanel;
import org.cogchar.render.sys.core.RenderRegistryClient;
import org.cogchar.render.app.core.CogcharRenderContext;


/**
 * @author Stu B. <www.texpedient.com>
 */
public class HumanoidRenderContext extends BonyRenderContext {

	private Map<Ident, HumanoidFigure> myFiguresByCharIdent = new HashMap<Ident, HumanoidFigure>();
	private BonyGameFeatureAdapter myGameFeatureAdapter;
	private UpdateInterface myUpdateInterface;

	public HumanoidRenderContext(RenderConfigEmitter rce) {
		super(rce);
		myGameFeatureAdapter = new BonyGameFeatureAdapter(this);
	}
	
	// What's this? Well, for better or worse I've made PumaAppContext the focal point of global config and graph
	// determination. At times, code "lower down" will want to request config updates. This is a first guess at
	// a method that will be general (thus the String key for the request type) and packaged in an interface we can 
	// pass around. PumaAppContext will set the interface on creation of the HRC.
	public interface UpdateInterface {
		public boolean updateConfig(String request);
	}
	
	public void setUpdateInterface(UpdateInterface theInterface) {
		myUpdateInterface = theInterface;
	}
	
	// ... and here's a method things that can see HRC can use to request a reload.
	public void requestConfigReload(String request) {
		if (myUpdateInterface != null) {
			myUpdateInterface.updateConfig(request);
		} else {
			logWarning("Update requested (" + request + "), but UpdateInterface not available in HumanoidRenderContext");
		}
	}

	@Override public void postInitLaunch() {
		super.postInitLaunch();

		AppSettings someSettings = getJMonkeyAppSettings();
		RenderRegistryClient rrc = getRenderRegistryClient();
		BonyGameFeatureAdapter.initCrossHairs(someSettings, rrc);
		initBasicTestPhysics();
		// We wait and do this later, possibly repeatedly.
		// initHumanoidStuff();
		// This is now done later, after all characters have been loaded:
		//initCameraAndLights(charWorldCl);

		myGameFeatureAdapter.initFeatures();

		//InputManager inputManager = findJme3InputManager(null);

		// Now done in initBindings, called by PumaAppContext along with initCinema
		//HumanoidPuppetActions.setupActionListeners(inputManager, this);
		//SceneActions.setupActionListeners(inputManager);

		WorkaroundFuncsMustDie.initScoreBoard(this);

		//initHelpScreen(someSettings, inputManager);
	}

	public HumanoidFigure getHumanoidFigure(Ident charIdent, HumanoidConfig hc, Ident bonyConfigGraph) {
		HumanoidFigure hf = myFiguresByCharIdent.get(charIdent);
		if (hf == null) {
			//BonyConfigEmitter bce = getBonyConfigEmitter();
			HumanoidFigureConfig hfc = new HumanoidFigureConfig(hc, getConfigEmitter(), bonyConfigGraph); 
			if (hfc.isComplete()) {
				hf = new HumanoidFigure(hfc);
				myFiguresByCharIdent.put(charIdent, hf);
			}
		}
		return hf;
	}

	// A few places want to just get the HumanoidFigure and aren't interested in possibly creating it
	// Those features don't want to have to worry about the graph idents, which are just for loading config
	// (CoreFeatureAdapter.attachToHumanoidBone, HumanoidPuppetActions.getSinbad)
	// I don't like overloading this method, but probably only a temporary fix
	public HumanoidFigure getHumanoidFigure(Ident charIdent) {
		return myFiguresByCharIdent.get(charIdent);
	}

	// Now does more, but does less on jME thread!
	public HumanoidFigure setupHumanoidFigure(Ident charIdent, Ident bonyConfigGraph, HumanoidConfig hc) throws Throwable {
		final HumanoidFigure figure = getHumanoidFigure(charIdent, hc, bonyConfigGraph);
		final AssetManager amgr = findJme3AssetManager(null);
		final Node rootNode = findJme3RootDeepNode(null);
		final PhysicsSpace ps = getPhysicsSpace();
		runTaskOnJmeThreadAndWait(new CogcharRenderContext.Task() {
			public void perform() throws Throwable {
				figure.initStuff(amgr, rootNode, ps);
			}
		});
		final HumanoidFigureModule hfm = new HumanoidFigureModule(figure, this);
		runTaskOnJmeThreadAndWait(new CogcharRenderContext.Task() {
			public void perform() throws Throwable {
				attachModule(hfm);
			}
		});
		figure.setModule(hfm);
		return figure;
	}
	
	public void detachHumanoidFigures() {
		final Node rootNode = findJme3RootDeepNode(null);
		final PhysicsSpace ps = getPhysicsSpace();
		Iterator<HumanoidFigure> currentFigureIterator = myFiguresByCharIdent.values().iterator();
		while (currentFigureIterator.hasNext()) {
			final HumanoidFigure aHumanoid = currentFigureIterator.next();
			enqueueCallable(new Callable<Void>() { // Do this on main render thread

				@Override
				public Void call() throws Exception {
					detachModule(aHumanoid.getModule());
					aHumanoid.detachFromVirtualWorld(rootNode, ps);
					return null;
				}
			});
		}
		myFiguresByCharIdent.clear();
	}
	
	public void initCinema() {
		WorkaroundAppStub stub = getAppStub();
		stub.setAppSpeed(1.3f);  // BowlAtSinbad uses 1.3f - is defined in Application.java, is this physics related?
		FlyByCamera fbCam = stub.getFlyByCamera();
		fbCam.setMoveSpeed(50);
		//initLightsCameraCinematics();
	}
	
	// Formerly performed in postInitLaunch, this is now called from PumaAppContext once the KeyBindingConfig is complete
	// Might make sense to just move this to PumaAppContext
	public void initBindings(KeyBindingConfig theConfig) {
		InputManager inputManager = findJme3InputManager(null);
		// If the help screen is displayed, we need to remove it since we'll be making a new one later
		if (currentHelpText != null) {
			enqueueCallable(new Callable<Void>() { // Do this on main render thread
				@Override
				public Void call() throws Exception {
					findOrMakeSceneFlatFacade(null).detachOverlaySpatial(currentHelpText);
					return null;
				}
			});
		}
		inputManager.clearMappings(); // May be a reload, so let's clear the mappings
		KeyBindingTracker.clearMap(); // If we do that, we'd better clear the KeyBindingTracker too
		// Since we just cleared mappings and are (for now at least) using the default FlyByCamera mappings, we must re-register them
		FlyByCamera fbCam = getAppStub().getFlyByCamera();
		fbCam.registerWithInput(inputManager);
		// Now we'll register the mappings in Cog Char based on theConfig
		HumanoidPuppetActions.setupActionListeners(inputManager, this, theConfig);
		SceneActions.setupActionListeners(inputManager, theConfig);
		// ... and finally set up the help screen now that the mappings are done
		AppSettings someSettings = getJMonkeyAppSettings();
		initHelpScreen(someSettings, inputManager, theConfig);
	}
	
	/* For now at least, these functions are moved to PumaAppContext - that way we doing all the config from one place
	private void initLightsCameraCinematics() {
		HumanoidRenderWorldMapper myRenderMapper = new HumanoidRenderWorldMapper();
		myRenderMapper.initLightsAndCamera(this);
		myRenderMapper.initCinematics(this);
	}
	*/
	
	/* Also moved to PumaAppContext
	public void reloadWorldConfig() {
		QueryInterface queryEmitter = QuerySheet.getInterface();
		queryEmitter.reloadSheetRepo();
		HumanoidRenderWorldMapper myRenderMapper = new HumanoidRenderWorldMapper();
		myRenderMapper.clearLights(this);
		myRenderMapper.clearCinematics(this);
		myRenderMapper.clearViewPorts(this);
		initLightsCameraCinematics();
	}
	*/ 

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
	private BitmapText currentHelpText; // We need to save this now, so it can be turned off automatically for reconfigs

	private void initHelpScreen(AppSettings settings, InputManager inputManager, KeyBindingConfig bindingConfig) {
		final String HELP_TAG = "Help"; // Perhaps should be defined elsewhere?
		final int NULL_KEY = -100; // This input not mapped to any key; we'll use it in the event of not finding one from bindingConfig
		int helpKey = NULL_KEY;
		String keyString = null;
		if (bindingConfig.myGeneralBindings.containsKey(HELP_TAG)) {
			keyString = bindingConfig.myGeneralBindings.get(HELP_TAG).boundKey;
		} else {
			logWarning("Attemping to retrieve key binding for help screen, but none is found");
		}
		try {
			if ((keyString.startsWith("AXIS")) || (keyString.startsWith("BUTTON"))) { // In this case, must be MouseInput
				logWarning("Mouse triggers not supported help screen");
			} else { // ... regular KeyInput
				Field keyField = KeyInput.class.getField("KEY_" + keyString.toUpperCase());
				helpKey = keyField.getInt(keyField);
			}
		} catch (Exception e) {
			logWarning("Error getting binding for help screen: " + e);
		}
		if (helpKey != NULL_KEY) {
			KeyBindingTracker.addBinding(HELP_TAG, helpKey); // Let's add ourselves to the help list!
			final BitmapText helpBT = findOrMakeSceneTextFacade(null).makeHelpScreen(0.6f, settings); // First argument sets text size, really shouldn't be hard-coded
			currentHelpText = helpBT;
			KeyTrigger keyTrig = new KeyTrigger(helpKey);
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
