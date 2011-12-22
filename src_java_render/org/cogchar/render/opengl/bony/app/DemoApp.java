/*
 *  Copyright 2011 by The Cogchar Project (www.cogchar.org).
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
package org.cogchar.render.opengl.bony.app;

import com.jme3.app.SimpleApplication;
import com.jme3.system.AppSettings;
import com.jme3.light.Light;
import com.jme3.asset.AssetManager;

import org.cogchar.blob.emit.DemoConfigEmitter;
import org.cogchar.render.opengl.bony.sys.JmonkeyAssetLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 *
 * @author pow
 */
public abstract class DemoApp extends SimpleApplication {
	static Logger theLogger = LoggerFactory.getLogger(DemoApp.class);
	protected DemoConfigEmitter		myConfigEmitter;
	protected JmonkeyAssetLoader	myContentsAssetLoader, myFrameworkAssetLoader;
	
	public DemoApp(DemoConfigEmitter ce) { 
		myConfigEmitter = ce;
		AppSettings settings = new AppSettings(ce.getAppSettingsDefloadFlag());
		settings.setRenderer(ce.getLWJGL_RendererName());		
		setSettings(settings);
	}
	public DemoApp() {
		this(new DemoConfigEmitter());
	}
	public void setContentsAssetLoader(JmonkeyAssetLoader jmal) {
		myContentsAssetLoader = jmal;
	}
	public JmonkeyAssetLoader getContentsAssetLoader() {
		if (myContentsAssetLoader == null) {
			myContentsAssetLoader = new JmonkeyAssetLoader(AssetManager.class);
		}
		AssetManager am = myContentsAssetLoader.getAssetManager();
		if (am == null) {
			myContentsAssetLoader.setAssetManager(assetManager);
		}
		return myContentsAssetLoader;
	}
	public void setFrameworkAssetLoader(JmonkeyAssetLoader jmal) {
		myFrameworkAssetLoader = jmal;
	}
	public JmonkeyAssetLoader getFrameworkAssetLoader() {
		if (myFrameworkAssetLoader == null) {
			myFrameworkAssetLoader = new JmonkeyAssetLoader(AssetManager.class);
		}
		AssetManager am = myFrameworkAssetLoader.getAssetManager();
		if (am == null) {
			myFrameworkAssetLoader.setAssetManager(assetManager);
		}
		return myFrameworkAssetLoader;
	}	
	protected void initFonts() { 
		guiFont = getContentsAssetLoader().loadFont(myConfigEmitter.getFontPath());
	}
	protected void setAppSpeed(float val) {
		speed = val;
	}

	protected void addLightToRootNode(Light l) {
		rootNode.addLight(l);
	}	
	protected void applySettings() { 
	/* http://jmonkeyengine.org/wiki/doku.php/jme3:intermediate:appsettings
	 * Every class that extends jme3.app.SimpleApplication has properties 
	 * that can be configured by customizing a com.jme3.system.AppSettings 
	 * object. Configure the settings before you call app.start() on 
	 * the application object. If you change display settings during runtime, 
	 * call app.restart() to make them take effect.
	 */
		AppSettings settings = new AppSettings(myConfigEmitter.getAppSettingsDefloadFlag());
		settings.setRenderer(myConfigEmitter.getLWJGL_RendererName());
		settings.setWidth(myConfigEmitter.getCanvasWidth());
		settings.setHeight(myConfigEmitter.getCanvasHeight());
		setSettings(settings);			
	}	
	/*  This approach should work, as perhaps would not using SimpleApplication at all.
	 * (Because SimpleApplication loads a bunch of stuff).
	 * However, we are currently relying on PumaAppContext to set the frameworkAssetLoader
	 * before launching the runLoop.  Which way is "better" depends on ones assumptions
	 * about how our classes + resources are packaged into OSGi bundles.
	 * Note that our custom assets are handled by a separate classloader installed
	 * by  HumanoidPuppetApp.initHumanoidStuff.
	 */
    @Override  public void initialize() {
		// This works for the initialize() callback, but
		// then we fail in the update() callback.  
		
		theLogger.info("********************* DemoApp.initialize() called, installing framework asset classloader");
		JmonkeyAssetLoader frameworkAL = getFrameworkAssetLoader();
		frameworkAL.installClassLoader(true);
		try {
			super.initialize();
		} finally {
			frameworkAL.restoreClassLoader();
		}
		theLogger.info("********************* DemoApp.initialize() restored context class loader");
	}	
    @Override  public void update() {	
		// It's probably only on the first call, and...we may
		// not want to do this classpath switcherooing on every
		// update().  (Is there hidden cost in setting classloader?).
		JmonkeyAssetLoader frameworkAL = getFrameworkAssetLoader();
		frameworkAL.installClassLoader(false);
		try {
			super.update();
		} finally {
			frameworkAL.restoreClassLoader();
		}
	}

	/*
	 *     [java] com.jme3.asset.AssetNotFoundException: Common/MatDefs/Light/Lighting
vert
    [java]     at com.jme3.asset.DesktopAssetManager.loadAsset(DesktopAssetMana
er.java:268)
    [java]     at com.jme3.asset.DesktopAssetManager.loadShader(DesktopAssetMan
ger.java:395)
    [java]     at com.jme3.material.Technique.loadShader(Technique.java:220)
    [java]     at com.jme3.material.Technique.makeCurrent(Technique.java:205)
    [java]     at com.jme3.material.Material.selectTechnique(Material.java:872)

    [java]     at com.jme3.material.Material.autoSelectTechnique(Material.java:
86)
    [java]     at com.jme3.material.Material.render(Material.java:958)
    [java]     at com.jme3.renderer.RenderManager.renderGeometry(RenderManager.
ava:649)
    [java]     at com.jme3.renderer.queue.RenderQueue.renderGeometryList(Render
ueue.java:299)
    [java]     at com.jme3.renderer.queue.RenderQueue.renderQueue(RenderQueue.j
va:351)
    [java]     at com.jme3.renderer.RenderManager.renderViewPortQueues(RenderMa
ager.java:886)
    [java]     at com.jme3.renderer.RenderManager.flushQueue(RenderManager.java
842)
    [java]     at com.jme3.renderer.RenderManager.renderViewPort(RenderManager.
ava:1118)
    [java]     at com.jme3.renderer.RenderManager.render(RenderManager.java:116
)
    [java]     at com.jme3.app.SimpleApplication.update(SimpleApplication.java:
66)
	 */
}
