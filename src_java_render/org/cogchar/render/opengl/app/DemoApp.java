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
package org.cogchar.render.opengl.app;

import java.util.List;
import java.util.ArrayList;

import com.jme3.app.SimpleApplication;
import com.jme3.system.AppSettings;
import com.jme3.light.Light;
import com.jme3.asset.AssetManager;
import com.jme3.asset.plugins.UrlLocator;


import com.jme3.font.BitmapText;
import com.jme3.light.DirectionalLight;
import com.jme3.math.Vector3f;
import org.cogchar.blob.emit.DemoConfigEmitter;
import org.cogchar.render.opengl.mesh.DebugMeshLoader;
import org.cogchar.render.opengl.bony.sys.JmonkeyAssetLocation;
import org.cogchar.render.opengl.bony.world.DemoVectorFactory;
import org.cogchar.render.opengl.scene.GeomFactory;
import org.cogchar.render.opengl.scene.TextMgr;
import org.cogchar.render.opengl.mesh.MeshFactoryFacade;
import org.cogchar.render.opengl.optic.MatFactory;
import org.cogchar.render.opengl.optic.OpticFacade;
import org.cogchar.render.opengl.scene.SceneFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 *
 * @author Stu B. <www.texpedient.com>
 */
public abstract class DemoApp extends CogcharRenderApp {
	static Logger theFallbackLogger = LoggerFactory.getLogger(DemoApp.class);
	
	private		Logger							myLogger;
	
	protected	DemoConfigEmitter				myConfigEmitter;

	public DemoApp(DemoConfigEmitter ce) { 
		myConfigEmitter = ce;
		AppSettings settings = new AppSettings(ce.getAppSettingsDefloadFlag());
		settings.setRenderer("LWJGL-OpenGL2"); // ce.getLWJGL_RendererName());		
		setSettings(settings);

	}
	public DemoApp() {
		this(new DemoConfigEmitter());
	}
	protected Logger getLogger() {
		if (myLogger == null) {
			myLogger = LoggerFactory.getLogger(this.getClass());
			if (myLogger == null) {
				myLogger = theFallbackLogger;
			}
		}
		return myLogger;
	}
	public synchronized void setLogger(Logger l) {
		myLogger = l;
	}
	protected MeshFactoryFacade  getMeshFF() {
		return getRenderContext().getMeshFF();
	}
	protected OpticFacade getOpticFacade() { 
		return getRenderContext().getOpticFacade();
	}
	protected SceneFacade getSceneFacade() { 
		return getRenderContext().getSceneFacade();
	}
	protected MatFactory getMatMgr() {
		return getOpticFacade().getMatMgr();
	}
	protected GeomFactory getGeomFactory() { 
		return getSceneFacade().getGeomFactory();
	}
	protected TextMgr getTextMgr() {
		return getSceneFacade().getTextMgr();
	}
	public DemoVectorFactory getDemoVectoryFactory() { 
		return new DemoVectorFactory();
	}


	protected void initFonts() { 
		// getContentsAssetLoader().
		guiFont = assetManager.loadFont(myConfigEmitter.getFontPath());
	}
	protected void setAppSpeed(float val) {
		speed = val;
	}

	protected void applySettings() { 
	/* http://jmonkeyengine.org/wiki/doku.php/jme3:intermediate:appsettings
	 * Every class that extends jme3.app.SimpleApplication has properties 
	 * that can be configured by customizing a com.jme3.system.AppSettings 
	 * object. Configure the settings before you call app.start() on 
	 * the application object. If you change display settings during runtime, 
	 * call app.restart() to make them take effect.
		 * 
		 * 		See Jmonkey examples    "TestCanvas.java" and "AppHarness.java"  
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
		theFallbackLogger.info("********************* DemoApp.initialize() called");
		ClassLoader savedCL = Thread.currentThread().getContextClassLoader();
		ClassLoader frameworkCL = AssetManager.class.getClassLoader();
		theFallbackLogger.info("********************* contextCL = " + savedCL + ", frameworkCL = " + frameworkCL);
		try {
			if (frameworkCL != null) {
				Thread.currentThread().setContextClassLoader(frameworkCL);
			}
			super.initialize();
		} finally {
			Thread.currentThread().setContextClassLoader(savedCL);
		}
		theFallbackLogger.info("********************* DemoApp.initialize() restored context class loader");
	}	

	@Override public void simpleInitApp() {
		theFallbackLogger.info("simpleInitApp() - START");
		super.simpleInitApp();
		theFallbackLogger.info("%%%%%%% JmeSystem.isLowPermissions()=" + com.jme3.system.JmeSystem.isLowPermissions());
		theFallbackLogger.info("Disabling confusing JDK-Logger warnings from UrlLocator");		
		java.util.logging.Logger.getLogger(UrlLocator.class.getName()).setLevel(java.util.logging.Level.SEVERE);
		
		//		theFallbackLogger.info("Unregistering default ClasspathLocator, which may fail if Default.cfg was not read by JMonkey.");
		// assetManager.unregisterLocator("/", com.jme3.asset.plugins.ClasspathLocator.class);	
		
		theFallbackLogger.info("simpleInitApp() - END");
	}
	protected void addLightToRootNode(Light l) {
		getSceneFacade().getDeepSceneMgr().addLight(l);
	}	
	protected DirectionalLight makeDemoDirectionalLight() { 
		Vector3f dir = getDemoVectoryFactory().getUsualLightDirection();
		return getOpticFacade().getLightFactory().makeWhiteOpaqueDirectionalLight(dir);
	}
	protected void addDemoDirLightToRootNode() { 
		addLightToRootNode(makeDemoDirectionalLight());
	}
	protected void setupLight() { 
		addDemoDirLightToRootNode();
	}
	/** A centred plus sign to help the player aim. */
	protected void initCrossHairs() {
		getSceneFacade().getFlatOverlayMgr().detachAllOverlays();
		BitmapText crossBT = getTextMgr().makeCrossHairs(2.0f, settings);
		getSceneFacade().getFlatOverlayMgr().attachOverlaySpatial(crossBT);
	}	

}
