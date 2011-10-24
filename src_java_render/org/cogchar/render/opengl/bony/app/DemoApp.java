/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cogchar.render.opengl.bony.app;

import com.jme3.app.SimpleApplication;
import com.jme3.system.AppSettings;
import com.jme3.light.Light;
/**
 *
 * @author pow
 */
public abstract class DemoApp extends SimpleApplication {
	// Set to OpenGL - 1 mode for 915GM graphics controller
	public static String DEFAULT_RENDERER_NAME = AppSettings.LWJGL_OPENGL_ANY;
	public static String PATH_DEFAULT_FONT = "Interface/Fonts/Default.fnt";
	public DemoApp(String lwjglRendererName) {
		AppSettings settings = new AppSettings(true);
		settings.setRenderer(lwjglRendererName);		
		setSettings(settings);			
	}
	public DemoApp() { 
		this(DEFAULT_RENDERER_NAME);
	}
	protected void initFonts() { 
		guiFont = assetManager.loadFont(PATH_DEFAULT_FONT);
	}
	protected void setAppSpeed(float val) {
		speed = val;
	}

	protected void addLightToRootNode(Light l) {
		rootNode.addLight(l);
	}	
}
