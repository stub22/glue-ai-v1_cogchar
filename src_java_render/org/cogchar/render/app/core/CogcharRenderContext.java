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
import com.jme3.font.BitmapFont;
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
import org.cogchar.render.sys.physics.DemoVectorFactory;
import org.cogchar.render.sys.physics.ProjectileLauncher;
import org.cogchar.render.opengl.optic.CameraMgr;
import org.cogchar.render.opengl.scene.DeepSceneMgr;
import org.cogchar.render.sys.core.AssetContext;
import org.cogchar.render.sys.core.BasicRenderRegistryClientImpl;
import org.cogchar.render.sys.core.RenderRegistryAware;
import org.cogchar.render.sys.core.RenderRegistryClient;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

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
	
	private		RenderRegistryClient		myRegClient;
	
	private		WorkaroundAppStub			myAppStub;
	
	private		AppSettings					myJme3AppSettings;
	
	public CogcharRenderContext() {
		myRegClient = new BasicRenderRegistryClientImpl();
	}
	
	public RenderRegistryClient getRenderRegistryClient() {
		return myRegClient;
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
		
	public void setAppStub(WorkaroundAppStub stub) {
		myAppStub = stub;
	}
	public WorkaroundAppStub getAppStub() {
		return myAppStub;
	}
	
	/**
	 *  Normally called during CogcharRenderApp.simpleInitApp(), after registerJMonkeyRoots.<br/>
	 */
	public void completeInit() {
		resolveAssetContext();
	}

	
	public void postInitLaunch() {

		
	}	
/*****	Performs final resolution of default locators on the asset classpath, and registers our asset 
	 *  classloaders  (previously submitted via JmonkeyAssetLocations passed to AssetContext.addAssetSource)
	 *  with our singleton JME3 assetManager.<br/>
	 * 
	 * It has been necessary to do this "early" because SimpleApplication.update() tries to 
	 * create some stuff that depends on the *default* classpath.
	 * 
	 * So apparently we need to split into the "resolve default" and "resolve bonus" phases.
	 * 
	 * 	This method should usually be called only once in an application's lifetime.<br/>
	 * * TODO: Keep a flag and throw exception if it is called twice.
	 */
	public void resolveAssetContext() { 
		AssetContext ac = findOrMakeAssetContext(null, null);
		ac.ensureAllSourcesReged();

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
	
	protected void addLightToRootNode(Light l) {
		DeepSceneMgr dsm = findOrMakeSceneDeepFacade(null);
		dsm.addLight(l);
	}
	
	
	
	public void runPostInitLaunchOnJmeThread() throws Throwable {
		WorkaroundAppStub appStub = getAppStub();
		java.util.concurrent.Future<Throwable> postInitFuture = appStub.enqueue(new java.util.concurrent.Callable<Throwable>() {

			public Throwable call() throws Exception {
				try {
					logInfo("%%%%%%%%%%%%%%%%%%% Callable on JME3 thread is calling postInitLaunch()");

					postInitLaunch();

					logInfo("%%%%%%%%%%%%%%%%%%% postInitLaunch() completed, Callable on JME3 thread is returning");
					return null;
				} catch (Throwable t) {

					return t;
				}
			}
		});

		logInfo("%%%%%%%%%%%%%%%%%%%%%%%%% Waiting for our postInitLaunch-bootPhase to complete()");
		Throwable fbpThrown = postInitFuture.get();
		if (fbpThrown != null) {
			throw new Exception("FinalBootPhase returned an error", fbpThrown);
		}
	}
	public Future<Object> enqueueCallable(Callable callThis) {
		WorkaroundAppStub was = getAppStub();
		return was.enqueue(callThis);
	}	
	public static interface Task {
		public void perform() throws Throwable;
	}
	public void runTaskOnJmeThreadAndWait(final Task task) throws Throwable {
		WorkaroundAppStub appStub = getAppStub();
		java.util.concurrent.Future<Throwable> taskFuture = appStub.enqueue(new java.util.concurrent.Callable<Throwable>() {

			public Throwable call() throws Exception {
				try {
					logInfo("%%%%%%%%%%%%%%%%%%% Callable on JME3 thread is calling task.perform()");

					task.perform();

					logInfo("%%%%%%%%%%%%%%%%%%% task.perform() completed, Callable on JME3 thread is returning");
					return null;
				} catch (Throwable t) {

					return t;
				}
			}
		});

		logInfo("%%%%%%%%%%%%%%%%%%%%%%%%% Waiting for our JME3 thread task to complete()");
		Throwable fbpThrown = taskFuture.get();
		if (fbpThrown != null) {
			throw new Exception("task.perform() threw an error", fbpThrown);
		}
	}		
}
