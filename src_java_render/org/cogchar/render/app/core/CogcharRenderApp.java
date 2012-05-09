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
package org.cogchar.render.app.core;

import com.jme3.app.SimpleApplication;

import com.jme3.asset.plugins.UrlLocator;

import org.appdapter.api.module.Module;
import org.cogchar.render.model.bony.CogcharRenderModulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**  A JMonkey-3 app that 1) Maintains at least one CogcharRenderContext and 
 * 2) Distributes updates and lifecycle ops via a CogcharRenderModulator, thereby 
 * bypassing the JMonkey "controls" layer.  
 * @author Stu B. <www.texpedient.com>
 */
public abstract class CogcharRenderApp<CRCT extends CogcharRenderContext> extends SimpleApplication {
	private		Logger							myLogger;

	private		CRCT							myRenderContext;
	
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

	/**
	 * An app should be able to construct a CRCT of proper type, without any side effects or dependencies
	 * on the environment.  This object then serves as a place to share handles to objects outside the
	 * rendering system, like the Swing GUI, which may be produced before the JME3app simpleInitApp() 
	 * method is called.
	 * @return 
	 */
	protected abstract CRCT makeCogcharRenderContext();
	
	protected CRCT getRenderContext() {
		if (myRenderContext == null) {
			myRenderContext = makeCogcharRenderContext();
		}
		return myRenderContext;
	}
	
	@Override public void simpleInitApp() {
		logInfo("CogcharRenderApp.simpleInitApp() - START");
		logInfo("%%%%%%% JmeSystem.isLowPermissions()=" + com.jme3.system.JmeSystem.isLowPermissions());
		
		logInfo("Disabling confusing JDK-Logger warnings from UrlLocator");		
		java.util.logging.Logger.getLogger(UrlLocator.class.getName()).setLevel(java.util.logging.Level.SEVERE);


		logInfo("fetch(/init) CogcharRenderContext");		
		myRenderContext = getRenderContext();
		
		myRenderContext.registerJMonkeyRoots(assetManager, rootNode, guiNode, stateManager, inputManager, renderManager);
		myRenderContext.registerJMonkeyDefaultCameras(cam, flyCam);
		try {
			myRenderContext.completeInit();
		} catch (Throwable t) {
			getLogger().error("Problem during Coghar-RenderContext.completeInit()", t);
		}
		
		// Register context and modulator with well-known registry
		logInfo("CogcharRenderApp.simpleInitApp() - END");
	}
	
	@Override public void simpleUpdate(float tpf) {
		getRenderContext().doUpdate(tpf);		
	}
		
}
