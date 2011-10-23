/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cogchar.render.opengl.bony.app;

import com.jme3.app.SimpleApplication;
import com.jme3.system.AppSettings;

/**
 *
 * @author pow
 */
public abstract class DemoApp extends SimpleApplication {
	// Set to OpenGL - 1 mode for 915GM graphics controller
	protected static String DEFAULT_RENDERER_NAME = AppSettings.LWJGL_OPENGL_ANY;
	public DemoApp(String lwjglRendererName) {
		AppSettings settings = new AppSettings(true);
		settings.setRenderer(lwjglRendererName);		
		setSettings(settings);			
	}
}
