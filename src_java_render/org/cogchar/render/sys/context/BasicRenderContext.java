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
package org.cogchar.render.sys.context;

import org.cogchar.render.sys.task.BasicCallableRenderTask;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetManager;
import com.jme3.input.InputManager;
import com.jme3.light.Light;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Node;
import java.io.InputStream;
import org.cogchar.platform.task.CallableTask;
import org.cogchar.render.opengl.scene.DeepSceneMgr;
import org.cogchar.render.sys.asset.AssetContext;
import org.cogchar.render.sys.registry.RenderRegistryClient;


import org.cogchar.render.sys.registry.BasicRenderRegistryClientFinderImpl;

/**
 * Named to differentiate it from JMonkey "RenderContext". This base class does not maintain much instance data.
 * However, some of its methods do have side effects on the application JMonkey state and registry state, therefore it
 * is recommended to have only one instance of this class in an application. Also, generally speaking the
 * registerJMonkeyRoots and completeInit methods should only be called once in an application.
 *
 * The main instance data is the pointer to a RenderRegistryClient, which is required to construct the context.
 *
 * The WorkaroundAppStub and AppSettings are more incidental and JME3 specific.
 *
 * @author Stu B. <www.texpedient.com>
 */
public abstract class BasicRenderContext extends BasicRenderRegistryClientFinderImpl { // extends RenderRegistryAware {

	public BasicRenderContext(RenderRegistryClient rrc) {
		super(rrc);
	}

	/**
	 * Normally called during CogcharRenderApp.simpleInitApp(), after JME3.SimpleApp variables are fully available to
	 * use as args here.<br/> This method should usually be called only once in an application's lifetime.<br/> TODO:
	 * Keep a flag and throw exception if it is called twice.
	 *
	 * @param assetMgr
	 * @param rootNode
	 * @param guiNode
	 */
	public void registerJMonkeyRoots(AssetManager assetMgr, Node rootNode, Node guiNode,
			AppStateManager stateMgr, InputManager inputMgr,
			RenderManager renderMgr) {
		RenderRegistryClient rrc = getRenderRegistryClient();
		rrc.putJme3AssetManager(assetMgr, null);
		rrc.putJme3RootDeepNode(rootNode, null);
		rrc.putJme3RootOverlayNode(guiNode, null); // 2d interface node from JME being plugged into registry
		rrc.putJme3AppStateManager(stateMgr, null);
		rrc.putJme3InputManager(inputMgr, null);
		rrc.putJme3RenderManager(renderMgr, null);

	}

	/**
	 * Normally called during CogcharRenderApp.simpleInitApp(), after registerJMonkeyRoots.<br/>
	 */
	public void completeInit() {
		resolveAssetContext();
	}

	
	/**
	 *
	 * Performs final resolution of default locators on the asset classpath, and registers our asset classloaders
	 * (previously submitted via JmonkeyAssetLocations passed to AssetContext.addAssetSource) with our singleton JME3
	 * assetManager.<br/>
	 *
	 * It has been necessary to do this "early" because SimpleApplication.update() tries to create some stuff that
	 * depends on the *default* classpath.
	 *
	 * So apparently we need to split into the "resolve default" and "resolve bonus" phases.
	 *
	 * This method should usually be called only once in an application's lifetime.<br/> * TODO: Keep a flag and throw
	 * exception if it is called twice.
	 */
	public void resolveAssetContext() {
		RenderRegistryClient rrc = getRenderRegistryClient();
		AssetContext ac = rrc.getAssetContext(null, null); // findOrMakeAssetContext(null, null);
		ac.ensureAllSourcesReged();

	}

	/**
	 * Subclasses override this method to recieve a callback on each JME3 update cycle, supplied by the app.
	 */
	public void doUpdate(float tpf) {
	}

	public Node loadModelOrNull(AssetManager amgr, String meshPath) {
		Node result = null;
		try {
			result = (Node) amgr.loadModel(meshPath);
		} catch (Throwable t) {
			logWarning("Cannot load model from meshPath=[" + meshPath + "]", t);
		}
		return result;
	}

	public InputStream openAssetStream(String assetName) {
		InputStream ais = null;
		try {
			AssetKey akey = new AssetKey(assetName);
			RenderRegistryClient rrc = getRenderRegistryClient();
			AssetManager assetMgr = rrc.getJme3AssetManager(null);
			AssetInfo ainf = assetMgr.locateAsset(akey);
			if (ainf != null) {
				ais = ainf.openStream();
			} else {
				getLogger().warn("Cannot find AssetInfo for   assetName: {}", assetName);
			}
		} catch (Throwable t) {
			getLogger().warn("Cannot open input stream for   assetName: {} ", assetName);
		}
		return ais;
	}

	protected void addLightToRootNode(Light l) {
		RenderRegistryClient rrc = getRenderRegistryClient();
		DeepSceneMgr dsm = rrc.getSceneDeepFacade(null);
		dsm.addLight(l);
	}

	abstract public void runTaskSafelyUntilComplete(CallableTask task) throws Throwable;
		
	/**
	 * Override this method to place your *initial* content into our context's OpenGL virtual world.
	 */
	abstract public void postInitLaunch();
/*
	protected void runAnyTaskSafelyUntilComplete(Task t) throws Throwable {
		CogcharRenderTask launcherCRT = CogcharRenderTask.makeRenderTaskFromAny(t);
		runTaskSafelyUntilComplete(launcherCRT);
	}
	* 
	*/ 
	public void runPostInitLaunchOnJmeThread() throws Throwable {
		CallableTask ct = new BasicCallableRenderTask(this) {
			@Override public void performWithClient(RenderRegistryClient rrc) throws Throwable {
				logInfo("%%%%%%%%%%%%%%%%%%% CogcharRenderTask on JME3 thread is calling postInitLaunch()");
				postInitLaunch();
				logInfo("%%%%%%%%%%%%%%%%%%% postInitLaunch() completed, Callable on JME3 thread is returning");
			}
		};
		runTaskSafelyUntilComplete(ct);
	}


}
