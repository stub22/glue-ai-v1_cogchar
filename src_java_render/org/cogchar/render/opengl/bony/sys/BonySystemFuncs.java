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
package org.cogchar.render.opengl.bony.sys;

import com.jme3.app.SimpleApplication;
import com.jme3.system.AppSettings;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class BonySystemFuncs {
	public static void setJMonkeySettings(SimpleApplication app, int width, int height) {
		// There is no getter for appSettings...?
		
		/* See Jmonkey examples    "TestCanvas.java" and "AppHarness.java"  */
		AppSettings settings = new AppSettings(true);
		settings.setWidth(width);
		settings.setHeight(height);
		// This explicit assetConfig workaround is necessary to prevent Jmonkey from resorting to getContextClassLoader()
		//  this(Thread.currentThread().getContextClassLoader().getResource("com/jme3/asset/Desktop.cfg"));
		// ... and this setting relies on a file in the "working directory" of the runtime, which 
		// probably needs some refinement.
		
		// See new workaround in Activator (actually SETTING the contextClassLoader!!)
		// settings.putString("AssetConfigURL", "file:./cogchar_jme3.cfg");
		// settings.setUseInput(false);
		app.setSettings(settings);
	}	
}
