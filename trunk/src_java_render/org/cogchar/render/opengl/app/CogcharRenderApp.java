/*
 *  Copyright 2012 by The Cogchar Project (www.cogchar.org).
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

import com.jme3.app.SimpleApplication;

import com.jme3.asset.plugins.UrlLocator;

import org.appdapter.api.module.Module;
import org.cogchar.render.opengl.bony.model.CogcharRenderModulator;
import org.cogchar.render.opengl.bony.sys.CogcharRenderContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**  A JMonkey-3 app that 1) Maintains at least one CogcharRenderContext and 
 * 2) Distributes updates and lifecycle ops via a CogcharRenderModulator, thereby 
 * bypassing the JMonkey "controls" layer.  
 * @author Stu B. <www.texpedient.com>
 */
public class CogcharRenderApp extends SimpleApplication {
	private		Logger							myLogger;
	private		CogcharRenderModulator			myRenderModulator;
	private		CogcharRenderContext			myRenderContext;
	
	public CogcharRenderApp() { 
		super();
		myLogger = LoggerFactory.getLogger(getClass());
	}
	protected Logger getLogger() {
		return myLogger;
	}
	protected void logInfo(String s) { 
		getLogger().info(s);
	}
	
	@Override public void simpleInitApp() {
		logInfo("CogcharRenderApp.simpleInitApp() - START");
		logInfo("%%%%%%% JmeSystem.isLowPermissions()=" + com.jme3.system.JmeSystem.isLowPermissions());
		logInfo("Disabling confusing JDK-Logger warnings from UrlLocator");		
		java.util.logging.Logger.getLogger(UrlLocator.class.getName()).setLevel(java.util.logging.Level.SEVERE);
		logInfo("init CogcharRenderModulator");		
		myRenderModulator = new CogcharRenderModulator();
		logInfo("init CogcharRenderContext");		
		myRenderContext = new CogcharRenderContext();
		myRenderContext.initJMonkeyStuff(assetManager, rootNode, guiNode);
		// Register context and modulator with well-known registry
		logInfo("CogcharRenderApp.simpleInitApp() - END");
	}
	
	@Override public void simpleUpdate(float tpf) {
		myRenderModulator.runOneCycle(tpf);
	}
	
	public void attachModule(Module<CogcharRenderModulator> m) { 
		myRenderModulator.attachModule(m);
	}
	public void detachModule(Module<CogcharRenderModulator> m) { 
		myRenderModulator.detachModule(m);
	}
	/** Generally these should be looked up via Registry, hence the method is protected. 
	 * 
	 * @return 
	 */
	protected CogcharRenderModulator getModulator() { 
		return myRenderModulator;
	}
	/** Generally these should be looked up via Registry, hence the method is protected. 
	 * 
	 * @return 
	 */
	
	protected CogcharRenderContext getRenderContext() { 
		return myRenderContext;
	}
}
