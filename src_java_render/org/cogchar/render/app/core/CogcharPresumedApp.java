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


import org.cogchar.render.sys.context.CogcharRenderContext;
import com.jme3.system.AppSettings;
import com.jme3.asset.AssetManager;
import com.jme3.asset.plugins.UrlLocator;


import com.jme3.font.BitmapFont;
import com.jme3.input.FlyByCamera;
import com.jme3.renderer.ViewPort;
import org.cogchar.blob.emit.RenderConfigEmitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 *
 * @author Stu B. <www.texpedient.com>
 */
public abstract class CogcharPresumedApp<CRCT extends CogcharRenderContext> extends CogcharRenderApp<CRCT> implements WorkaroundAppStub {
	static Logger theFallbackLogger = LoggerFactory.getLogger(CogcharPresumedApp.class);
	
	private		Logger							myLogger;
	
	protected	RenderConfigEmitter				myConfigEmitter;

	public CogcharPresumedApp(RenderConfigEmitter rce) { 
		myConfigEmitter = rce;
		AppSettings someSettings = new AppSettings(rce.getAppSettingsDefloadFlag());
		String rendererName = rce.getLWJGL_RendererName();
		logInfo("**************************************************** LWJGL Renderer Name: " + rendererName);
		someSettings.setRenderer(rendererName); // "LWJGL-OpenGL2"); 
		setAppSettings(someSettings);
	}
	public CogcharPresumedApp() {
		this(new RenderConfigEmitter());
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
	public RenderConfigEmitter getConfigEmitter() { 
		return myConfigEmitter;
	}
	public final void setAppSettings(AppSettings someSettings) { 
		setSettings(someSettings);
		getRenderContext().registerJMonkeyAppSettings(someSettings); // Hmm this was registering settings, not someSettings. Where is settings, com.jme3.app.Application's field?
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
		someSettings.setFrameRate(60); // No need to run faster than this; we'd just be wasting resources
		setAppSettings(someSettings);
	}
	// Hide the diagnostic information
	protected void hideJmonkeyDebugInfo() {
		setDisplayFps(false);
		setDisplayStatView(false);
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
		theFallbackLogger.info("********************* CogcharPresumedApp.initialize() called");
		ClassLoader savedCL = Thread.currentThread().getContextClassLoader();
		ClassLoader frameworkCL = AssetManager.class.getClassLoader();
		theFallbackLogger.info("********************* contextCL = {}, frameworkCL = {}", savedCL, frameworkCL);
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
	@Override public FlyByCamera getFlyByCamera() {
		return flyCam;
	}	
	@Override	public ViewPort  getPrimaryAppViewPort() {
		return getViewPort();
	}

}
