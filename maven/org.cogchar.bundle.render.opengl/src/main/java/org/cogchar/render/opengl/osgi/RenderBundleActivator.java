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
import org.appdapter.osgi.core.BundleActivatorBase;
import org.appdapter.osgi.registry.RegistryServiceFuncs;
import org.appdapter.api.registry.VerySimpleRegistry;
import org.cogchar.blob.emit.BonyConfigEmitter;
import org.cogchar.bundle.render.resources.ResourceBundleActivator;
import org.cogchar.render.opengl.bony.app.BonyVirtualCharApp;
import org.cogchar.render.opengl.bony.sys.BonyRenderContext;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.cogchar.render.opengl.bony.demo.HumanoidPuppetApp;
import org.cogchar.render.opengl.bony.gui.PanelUtils;
import org.cogchar.render.opengl.bony.gui.VirtualCharacterPanel;
import org.cogchar.render.opengl.bony.sys.JmonkeyAssetLocation;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class RenderBundleActivator extends BundleActivatorBase {

	static Logger theLogger = LoggerFactory.getLogger(RenderBundleActivator.class);
	private BonyRenderContext myBonyRenderContext;

	@Override protected Logger getLogger() {
		return theLogger;
	}

	// This is the primary service export point for the bundle, as of 2011-08-30.
	public BonyRenderContext getBonyRenderContext() {
		return myBonyRenderContext;
	}

	@Override public void start(BundleContext bundleCtx) throws Exception {
		
		super.start(bundleCtx);
		// IDE hints may show these symbols as undefined, but they should compile OK.		
		theLogger.info("******************* Fetching VerySimpleRegistry");

		VerySimpleRegistry vsr = RegistryServiceFuncs.getTheWellKnownRegistry(bundleCtx);
		
		theLogger.info("******************* Creating BonyConfigEmitter, HumanoidPuppetApp, and JmonkeyAssetLocation");
		BonyConfigEmitter bce = new BonyConfigEmitter();
		BonyVirtualCharApp bvcApp = new HumanoidPuppetApp(bce);
		JmonkeyAssetLocation jmal = new JmonkeyAssetLocation(ResourceBundleActivator.class);
		bvcApp.addAssetSource(jmal);   
		theLogger.info("******************* Initializing VirtualCharacterPanel with canvas");
		
		VirtualCharacterPanel vcp = PanelUtils.makeVCPanel(bce, "FULL");
		bvcApp.initCharPanelWithCanvas(vcp);

		myBonyRenderContext = bvcApp.getBonyRenderContext();
		theLogger.info("******************* Registering BonyRenderContext as OSGi service");
		bundleCtx.registerService(BonyRenderContext.class.getName(), myBonyRenderContext, null);

		theLogger.info("******************* start() is DONE!");
	}

	@Override public void stop(BundleContext bundleCtx) throws Exception {
		// Perhaps this should be done last, via a "finally" clause.
		super.stop(bundleCtx);
		// Attempt to cleanup OpenGL resources, which happens nicely in standalone demo if the window is X-ed.
		// (Probably cleanup is happening during dispose(), so direct call to that should work too).
		BonyRenderContext bc = getBonyRenderContext();
		if (bc != null) {
			JFrame jf = bc.getFrame();
			if (jf != null) {
				theLogger.info("Sending WINDOW_CLOSING event to BonyRenderContext.JFrame");
				WindowEvent windowClosing = new WindowEvent(jf, WindowEvent.WINDOW_CLOSING);
				jf.dispatchEvent(windowClosing);
			} else {
				theLogger.warn("BonyRenderContext returned null JFrame, so we have no window to close.");
			}
		} else {
			theLogger.warn("stop() found null BonyRenderContext");
		}
		theLogger.info("stop() is DONE!");
	}
}
