/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
	protected JmonkeyAssetLoader	myAssetLoader;
	
	public DemoApp(DemoConfigEmitter ce) { 
		myConfigEmitter = ce;
		AppSettings settings = new AppSettings(ce.getAppSettingsDefloadFlag());
		settings.setRenderer(ce.getLWJGL_RendererName());		
		setSettings(settings);
	}
	public DemoApp() {
		this(new DemoConfigEmitter());
	}
	public void setAssetLoader(JmonkeyAssetLoader jmal) {
		myAssetLoader = jmal;
	}
	public JmonkeyAssetLoader getAssetLoader() {
		if (myAssetLoader == null) {
			myAssetLoader = new JmonkeyAssetLoader();
		}
		AssetManager am = myAssetLoader.getAssetManager();
		if (am == null) {
			myAssetLoader.setAssetManager(assetManager);
		}
		return myAssetLoader;
	}
	protected void initFonts() { 
		guiFont = getAssetLoader().loadFont(myConfigEmitter.getFontPath());
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
}
