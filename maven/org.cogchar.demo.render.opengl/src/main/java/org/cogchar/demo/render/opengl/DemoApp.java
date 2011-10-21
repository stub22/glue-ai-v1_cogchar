/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cogchar.demo.render.opengl;

import com.jme3.app.SimpleApplication;
import com.jme3.system.AppSettings;

/**
 *
 * @author pow
 */
public abstract class DemoApp extends SimpleApplication {
	public DemoApp(String lwjglRendererName) {
		AppSettings settings = new AppSettings(true);
		settings.setRenderer(lwjglRendererName);		
		setSettings(settings);			
	}
}
