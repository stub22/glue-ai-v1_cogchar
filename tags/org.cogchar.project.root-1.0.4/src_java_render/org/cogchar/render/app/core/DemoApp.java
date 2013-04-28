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
package org.cogchar.render.app.core;


import com.jme3.system.AppSettings;
import com.jme3.asset.AssetManager;
import com.jme3.asset.plugins.UrlLocator;


import com.jme3.font.BitmapFont;
import com.jme3.input.FlyByCamera;
import org.cogchar.blob.emit.DemoConfigEmitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 *
 * @author Stu B. <www.texpedient.com>
 */
public abstract class DemoApp<CRCT extends CogcharRenderContext> extends CogcharRenderApp<CRCT> implements AppStub {
	static Logger theFallbackLogger = LoggerFactory.getLogger(DemoApp.class);
	
	private		Logger							myLogger;
	
	protected	DemoConfigEmitter				myConfigEmitter;

	public DemoApp(DemoConfigEmitter ce) { 
		myConfigEmitter = ce;
		AppSettings someSettings = new AppSettings(ce.getAppSettingsDefloadFlag());
		String rendererName = ce.getLWJGL_RendererName();
		logInfo("**************************************************** LWJGL Renderer Name: " + rendererName);
		someSettings.setRenderer(rendererName); // "LWJGL-OpenGL2"); 
		setAppSettings(someSettings);
	}
	public DemoApp() {
		this(new DemoConfigEmitter());
	}
	protected void logInfo(String txt) {
		getLogger().info(txt);
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
	public DemoConfigEmitter getConfigEmitter() { 
		return myConfigEmitter;
	}
	public final void setAppSettings(AppSettings someSettings) { 
		setSettings(someSettings);
		getRenderContext().registerJMonkeyAppSettings(settings);		
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
		AppSettings someSettings = new AppSettings(myConfigEmitter.getAppSettingsDefloadFlag());
		someSettings.setRenderer(myConfigEmitter.getLWJGL_RendererName());
		someSettings.setWidth(myConfigEmitter.getCanvasWidth());
		someSettings.setHeight(myConfigEmitter.getCanvasHeight());
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
		CRCT crc = getRenderContext();
		crc.setAppStub(this);
		super.simpleInitApp();
	}

	@Override public void setAppSpeed(float val) {
		speed = val;
	}
	@Override public void setGuiFont(BitmapFont font) {
		guiFont = font;
	}
	@Override public FlyByCamera getFlyCam() {
		return flyCam;
	}
	
	/** A centred plus sign to help the player aim. */
	protected void initCrossHairs() {
		getRenderContext().initCrossHairs(settings);
	}	
	
	

}