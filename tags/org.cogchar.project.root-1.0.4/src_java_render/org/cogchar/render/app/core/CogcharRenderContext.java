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
package org.cogchar.render.app.core;

import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapText;
import com.jme3.input.FlyByCamera;
import com.jme3.input.InputManager;
import com.jme3.light.DirectionalLight;
import com.jme3.light.Light;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;
import java.io.InputStream;
import org.cogchar.render.app.core.AppStub;
import org.cogchar.render.sys.physics.DemoVectorFactory;
import org.cogchar.render.sys.physics.ProjectileLauncher;
import org.cogchar.render.opengl.optic.CameraMgr;
import org.cogchar.render.opengl.scene.DeepSceneMgr;
import org.cogchar.render.sys.core.AssetContext;
import org.cogchar.render.sys.core.RenderRegistryAware;

/**  Named to differentiate it from JMonkey "RenderContext".  
 * This base class does not maintain much instance data.
 * However, some of its methods do have side effects on the application
 * JMonkey state and registry state, therefore it is recommended to
 * have only one instance of this class in an application.  Also,
 * generally speaking the registerJMonkeyRoots and completeInit methods 
 * should only be called once in an application.
 * @author Stu B. <www.texpedient.com>
 */
public class CogcharRenderContext extends RenderRegistryAware {
	
	private		AppStub			myAppStub;
	
	private AppSettings		myJme3AppSettings;
	
	public CogcharRenderContext() {
	}
	/**
	 * Normally called during CogcharRenderApp.simpleInitApp(), after JME3.SimpleApp variables are fully 
	 * available to use as args here.<br/>
	 * This method should usually be called only once in an application's lifetime.<br/>
	 * TODO: Keep a flag and throw exception if it is called twice.
	 
	 * @param assetMgr
	 * @param rootNode
	 * @param guiNode 
	 */
	public void registerJMonkeyRoots(AssetManager assetMgr, Node rootNode, Node guiNode, 
					AppStateManager stateMgr, InputManager inputMgr,
					RenderManager renderMgr) { 
		registerJme3AssetManager(assetMgr, null);
		registerJme3RootDeepNode(rootNode, null);
		registerJme3RootOverlayNode(guiNode, null); // 2d interface node from JME being plugged into registry
		registerJme3AppStateManager(stateMgr, null);
		registerJme3InputManager(inputMgr, null);
		registerJme3RenderManager(renderMgr, null);
		
	}
	/**
	 * Hmmmm.
	 * @param settings 
	 */
	public void registerJMonkeyAppSettings(AppSettings settings) {
		myJme3AppSettings = settings;
	}
	
	protected AppSettings getJMonkeyAppSettings() { 
		return myJme3AppSettings;
	}
	
	public void registerJMonkeyDefaultCameras(Camera defCam, FlyByCamera fbc) {
		CameraMgr cm = findOrMakeOpticCameraFacade(null);
		cm.registerCommonCamera(CameraMgr.CommonCameras.DEFAULT, defCam);
	}
        
	/* Added so CameraMgr can create new cameras using JME3 settings from RDF 
	* We also call back to CameraMgr to register this new camera - that way
	* it's guaranteed to be taken care of in case some other code decides to
	* call this method.
	* 
	* UPDATE: seems we must clone the default camera; a camera created this way
	* does not seem to attach to the scene correctly (though it might with some extra steps) 

	public Camera registerNewCameraUsingJME3Settings(String cameraName) {
		CameraMgr cm = findOrMakeOpticCameraFacade(null);
		Camera newCamera = new Camera(myJme3AppSettings.getWidth(), myJme3AppSettings.getHeight());
		cm.registerNamedCamera(cameraName, newCamera);
		return newCamera;
	}
	*/


	// Right now this is just a public wrapper for addLightToRootNode
	// so LightFactory can add lights from RDF
	// BUT this won't work as is - at time LightFactory is loading from RDF, lights won't be added on main
	// render thread as needed using this method. See LightFactory for current kludge.
	/*
	public void addNewLightToJME3RootNode(Light l) {
		addLightToRootNode(l);
	}
	*/

	// Added so CameraMgr can create new viewports for cameras loaded from RDF
	public void addViewPort(String label, Camera c) {
		DeepSceneMgr dsm = findOrMakeSceneDeepFacade(null);
		dsm.addViewPort(label, c);
	}
	
	public void setAppStub(AppStub stub) {
		myAppStub = stub;
	}
	public AppStub getAppStub() {
		return myAppStub;
	}
	
	/**
	 *  Normally called during CogcharRenderApp.simpleInitApp(), after registerJMonkeyRoots.<br/>
	 *	Performs final resolution of the asset classpath, and registers our asset classloaders
	 *  (previously submitted via JmonkeyAssetLocations passed to AssetContext.addAssetSource)
	 *  with our singleton JME3 assetManager.<br/>
	 * 	This method should usually be called only once in an application's lifetime.<br/>
	 * * TODO: Keep a flag and throw exception if it is called twice.
	 */

	public void completeInit() {
		
		AssetContext ac = findOrMakeAssetContext(null, null);
		ac.resolveAndRegisterAllAssetSources();
		
	}
	
	protected void addLightToRootNode(Light l) {
		DeepSceneMgr dsm = findOrMakeSceneDeepFacade(null);
		dsm.addLight(l);
	}
	// Should be able to remove this now as light comes from RDF, but will leave it in for now in case something weird is calling it
	protected DirectionalLight makeDemoDirectionalLight() { 
		Vector3f dir = getDemoVectoryFactory().getUsualLightDirection();
		return findOrMakeOpticLightFacade(null).makeWhiteOpaqueDirectionalLight(dir);
	}
	// Should be able to remove this now as light comes from RDF, but will leave it in for now in case something weird is calling it
	public void addDemoDirLightToRootNode() { 
		addLightToRootNode(makeDemoDirectionalLight());
	}	
	/** A centred plus sign to help the player aim. */
	public void initCrossHairs(AppSettings settings) {
		findOrMakeSceneFlatFacade(null).detachAllOverlays();
		BitmapText crossBT = findOrMakeSceneTextFacade(null).makeCrossHairs(2.0f, settings);
		findOrMakeSceneFlatFacade(null).attachOverlaySpatial(crossBT);
	}	
	// Should be able to remove this now as light comes from RDF, but will leave it in for now in case something weird is calling it
	// Also should be able to get rid of fabulous DemoVectorFactory class!
	public DemoVectorFactory getDemoVectoryFactory() { 
		return new DemoVectorFactory();
	}
	public ProjectileLauncher makeProjectileLauncher() {
		return new ProjectileLauncher(findOrMakeMeshShapeFacade(null), findOrMakeOpticMaterialFacade(null, null));		
	}
	/**
	 *  Subclasses override this method to recieve a callback on each JME3 update cycle, supplied
	 *  by the app.  
	 */
	public void doUpdate(float tpf) {
		
	}
	
	public Node loadModelOrNull(AssetManager amgr, String meshPath) {
		Node result = null;
		try {
			result = (Node) amgr.loadModel(meshPath);
		} catch (Throwable t) {
			getLogger().warn("Cannot load model from meshPath=[" + meshPath + "]", t);
		}
		return result;
	}
	public InputStream openAssetStream(String assetName) {
		InputStream ais = null;
		try {
			AssetKey akey = new AssetKey(assetName);
			AssetManager assetMgr = findJme3AssetManager(null);
			AssetInfo ainf = assetMgr.locateAsset(akey);
			if (ainf != null) {
				ais = ainf.openStream();
			} else {
				getLogger().warn("Cannot find AssetInfo for   assetName: " + assetName);
			}
		} catch (Throwable t) {
			getLogger().warn("Cannot open input stream for   assetName: " + assetName, t);
		}
		return ais;
	}	
		
}
