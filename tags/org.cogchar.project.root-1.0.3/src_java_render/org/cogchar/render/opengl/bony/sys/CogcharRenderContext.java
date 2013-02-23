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
package org.cogchar.render.opengl.bony.sys;

import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapText;
import com.jme3.input.FlyByCamera;
import com.jme3.input.InputManager;
import com.jme3.light.DirectionalLight;
import com.jme3.light.Light;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;
import org.cogchar.render.opengl.app.AppStub;
import org.cogchar.render.opengl.bony.world.DemoVectorFactory;
import org.cogchar.render.opengl.bony.world.ProjectileLauncher;
import org.cogchar.render.opengl.optic.CameraMgr;
import org.cogchar.render.opengl.scene.DeepSceneMgr;
import org.slf4j.Logger;

/**  Named to differentiate it from JMonkey "RenderContext".  
 * This base class does not maintain any instance data.
 * However, some of its methods do have side effects on the application
 * JMonkey state and registry state, therefore it is recommended to
 * have only one instance of this class in an application.  Also,
 * generally speaking the registerJMonkeyRoots and completeInit methods 
 * should only be called once in an application.
 * @author Stu B. <www.texpedient.com>
 */
public class CogcharRenderContext extends RenderRegistryAware {
	private		Logger			myLogger;
	
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
					AppStateManager stateMgr, InputManager inputMgr) { 
		registerJme3AssetManager(assetMgr, null);
		registerJme3RootDeepNode(rootNode, null);
		registerJme3RootOverlayNode(guiNode, null);
		registerJme3AppStateManager(stateMgr, null);
		registerJme3InputManager(inputMgr, null);
		
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
	
	public void setAppStub(AppStub stub) {
		myAppStub = stub;
	}
	public AppStub getAppStub() {
		return myAppStub;
	}
	
	/**
	 *  Normally called during CogcharRenderApp.simpleInitApp(), after registerJMonkeyRoots.<br/>
	 *	Performs final resolution of the asset classpath, and registers our asset classloaders
	 *  (previously submitted via JmonkeyAssetLocation's passed to AssetContext.addAssetSource)
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
	protected DirectionalLight makeDemoDirectionalLight() { 
		Vector3f dir = getDemoVectoryFactory().getUsualLightDirection();
		return findOrMakeOpticLightFacade(null).makeWhiteOpaqueDirectionalLight(dir);
	}
	public void addDemoDirLightToRootNode() { 
		addLightToRootNode(makeDemoDirectionalLight());
	}	
	/** A centred plus sign to help the player aim. */
	public void initCrossHairs(AppSettings settings) {
		findOrMakeSceneFlatFacade(null).detachAllOverlays();
		BitmapText crossBT = findOrMakeSceneTextFacade(null).makeCrossHairs(2.0f, settings);
		findOrMakeSceneFlatFacade(null).attachOverlaySpatial(crossBT);
	}	
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
		
}