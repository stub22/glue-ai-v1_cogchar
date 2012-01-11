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

import java.util.List;
import java.util.ArrayList;

import com.jme3.app.SimpleApplication;
import com.jme3.system.AppSettings;
import com.jme3.light.Light;
import com.jme3.asset.AssetManager;
import com.jme3.asset.plugins.UrlLocator;
import java.net.URL;


import org.cogchar.blob.emit.DemoConfigEmitter;
import org.cogchar.render.opengl.bony.sys.BonyAssetLocator;
import org.cogchar.render.opengl.bony.sys.DebugMeshLoader;
import org.cogchar.render.opengl.bony.sys.JmonkeyAssetLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 *
 * @author pow
 */
public abstract class DemoApp extends SimpleApplication {
	static Logger theLogger = LoggerFactory.getLogger(DemoApp.class);
	protected	DemoConfigEmitter				myConfigEmitter;
	private		List<JmonkeyAssetLocation>		myAssetSources = new ArrayList<JmonkeyAssetLocation>();
	private		ClassLoader						myFrameworkResourceClassLoader;
	
	public DemoApp(DemoConfigEmitter ce) { 
		myConfigEmitter = ce;
		AppSettings settings = new AppSettings(ce.getAppSettingsDefloadFlag());
		settings.setRenderer(ce.getLWJGL_RendererName());		
		setSettings(settings);
		myFrameworkResourceClassLoader = AssetManager.class.getClassLoader();
		JmonkeyAssetLocation frameJAL = new JmonkeyAssetLocation(AssetManager.class);
		addAssetSource(frameJAL);
	}
	public DemoApp() {
		this(new DemoConfigEmitter());
	}
	public void addAssetSource(JmonkeyAssetLocation jmal) {
		myAssetSources.add(jmal);
	}
	public void resolveAndRegisterAllAssetSources() { 

		// Optionally add a bonyAssetLocator here for debugging.
		for (JmonkeyAssetLocation jmal : myAssetSources) {
			jmal.resolve();
			jmal.registerLocators(assetManager);
		}

	}

	protected void initFonts() { 
		// getContentsAssetLoader().
		guiFont = assetManager.loadFont(myConfigEmitter.getFontPath());
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
    @Override  public void initialize() {
		/*
		// AssetManager does not exist yet.
		// It is created in Application.initialize() (if it does not exist yet)
		// , and used to load resources in SimpleApplication.initialize()
		// (e.g. loadFPSText() does assetManager.loadFont("Interface/Fonts/Default.fnt");)
		// so our only chance to intervene is right here (or to create AssetManager ourselves,
		// or use a custom config file for DesktopAssetManager.
		
		// Note that "update()" may also load assets, but by that time our simpleInitApp
		// should have run to install our custom locators.
		*/
		theLogger.info("********************* DemoApp.initialize() called,  framework resource CL =" + myFrameworkResourceClassLoader);
		ClassLoader savedCL = Thread.currentThread().getContextClassLoader();
		try {
			if (myFrameworkResourceClassLoader != null) {
				Thread.currentThread().setContextClassLoader(myFrameworkResourceClassLoader);
			}
			super.initialize();
		} finally {
			Thread.currentThread().setContextClassLoader(savedCL);
		}
		theLogger.info("********************* DemoApp.initialize() restored context class loader");
	}	

	@Override public void simpleInitApp() {
		theLogger.info("simpleInitApp() - START");
		theLogger.info("%%%%%%% JmeSystem.isLowPermissions()=" + com.jme3.system.JmeSystem.isLowPermissions());
		theLogger.info("Disabling confusing JDK-Logger warnings from UrlLocator");		
		java.util.logging.Logger.getLogger(UrlLocator.class.getName()).setLevel(java.util.logging.Level.SEVERE);
		
		//		theLogger.info("Unregistering default ClasspathLocator, which may fail if Default.cfg was not read by JMonkey.");
		// assetManager.unregisterLocator("/", com.jme3.asset.plugins.ClasspathLocator.class);	
		
		resolveAndRegisterAllAssetSources();

		// DebugMeshLoader helps with debugging.
		assetManager.registerLoader(DebugMeshLoader.class, "meshxml", "mesh.xml");
		
		theLogger.info("simpleInitApp() - END");
	}

}
