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
package org.cogchar.render.opengl.osgi;

import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import org.appdapter.core.log.BasicDebugger;
import org.appdapter.osgi.core.BundleActivatorBase;
import org.cogchar.blob.emit.BonyConfigEmitter;
import org.cogchar.render.app.bony.BonyVirtualCharApp;
import org.cogchar.render.app.bony.BonyRenderContext;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.cogchar.render.app.humanoid.HumanoidPuppetApp;
import org.cogchar.render.gui.bony.PanelUtils;
import org.cogchar.render.gui.bony.VirtualCharacterPanel;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class RenderBundleActivator extends BundleActivatorBase {
	

	static Logger theLogger = LoggerFactory.getLogger(RenderBundleActivator.class);
	// private BonyRenderContext myBonyRenderContext;

	@Override protected Logger getLogger() {
		return theLogger;
	}

	// This is the primary service export point for the bundle, as of 2011-08-30.
	/*
	public BonyRenderContext getBonyRenderContext() {
		return myBonyRenderContext;
	}
	* 
	*/ 

	@Override public void start(BundleContext bundleCtx) throws Exception {
		
		super.start(bundleCtx);
		
		String panelKind = "SLIM";
		
		// myBonyRenderContext = 
		RenderBundleUtils.buildBonyRenderContextInOSGi(bundleCtx, panelKind);
	
		theLogger.info("******************* start() is DONE!");
	}

	@Override public void stop(BundleContext bundleCtx) throws Exception {
		// Perhaps this should be done last, via a "finally" clause.
		super.stop(bundleCtx);
		RenderBundleUtils.shutdownBonyRenderContextInOSGi(bundleCtx);
		theLogger.info("stop() is DONE!");
	}
}
