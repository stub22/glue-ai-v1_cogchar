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

import org.appdapter.core.log.BasicDebugger;
import org.cogchar.render.app.bony.BonyRenderContext;
import org.cogchar.render.app.bony.BonyVirtualCharApp;
import org.cogchar.render.app.humanoid.HumanoidPuppetApp;
import org.cogchar.render.gui.bony.PanelUtils;
import org.cogchar.render.gui.bony.VirtualCharacterPanel;

import org.cogchar.blob.emit.RenderConfigEmitter;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.jme3.system.JmeSystem;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class RenderBundleUtils {
	static	BasicDebugger	theDbg = new BasicDebugger() {};
	
	public static BonyRenderContext getBonyRenderContext(BundleContext bundleCtx) {
		ServiceReference ref = bundleCtx.getServiceReference(BonyRenderContext.class.getName());
		if(ref == null){
			return null;
		}
		return (BonyRenderContext) bundleCtx.getService(ref);
	}	
	
	public static BonyRenderContext buildBonyRenderContextInOSGi(BundleContext bundleCtx, String panelKind) {
		BonyRenderContext	resultBRC;
		
		// IDE hints may show these symbols (from transitive deps) as undefined, but they should compile OK with maven.
		theDbg.logInfo("******************* Fetching VerySimpleRegistry");

		/*
		theLogger.info("******************* Registering assumed resource bundle with default AssetContext");
		AssetContext defAssetCtx = RenderRegistryFuncs.findOrMakeAssetContext(null, null);
		JmonkeyAssetLocation jmal = new JmonkeyAssetLocation(ResourceBundleActivator.class);
		defAssetCtx.addAssetSource(null);
		 * 
		 */
		
		theDbg.logInfo("******************* Creating BonyConfigEmitter, HumanoidPuppetApp");
		// TODO - lookup the sysContextURI from appropriate place.
		RenderConfigEmitter bce = new RenderConfigEmitter();
		BonyVirtualCharApp bvcApp = new HumanoidPuppetApp(bce);
		
		// Want to decide this "kind" based on further context (e.g. "are we in Netbeans?"  "are we in debug-mode?"),
		// which is a concrete reason to try to push this init out of the bundle activator, and perform on demand
		// instead.
		
		theDbg.logInfo("******************* Initializing VirtualCharacterPanel of kind " + panelKind + " with canvas");
		
		VirtualCharacterPanel vcp = PanelUtils.makeVCPanel(bce, panelKind);
		theDbg.logInfo("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Value of org.lwjgl.librarypath = " + System.getProperty("org.lwjgl.librarypath"));		
		theDbg.logInfo("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Forcing lowPermissions during initCharPanelWithCanvas(), to prevent JME3 forcing value into org.lwjgl.librarypath");

		JmeSystem.setLowPermissions(true);
		bvcApp.initCharPanelWithCanvas(vcp);
		JmeSystem.setLowPermissions(false);

		resultBRC = bvcApp.getBonyRenderContext();
		theDbg.logInfo("******************* Registering BonyRenderContext as OSGi service");
		bundleCtx.registerService(BonyRenderContext.class.getName(), resultBRC, null);
		return resultBRC;
	}
	
	public static void shutdownBonyRenderContextInOSGi(BundleContext bundleCtx) {
				// Attempt to cleanup OpenGL resources, which happens nicely in standalone demo if the window is X-ed.
		// (Probably cleanup is happening during dispose(), so direct call to that should work too).
		BonyRenderContext bc = getBonyRenderContext(bundleCtx);
		if (bc != null) {
			JFrame jf = bc.getFrame();
			if (jf != null) {
				theDbg.logInfo("Sending WINDOW_CLOSING event to BonyRenderContext.JFrame");
				WindowEvent windowClosing = new WindowEvent(jf, WindowEvent.WINDOW_CLOSING);
				jf.dispatchEvent(windowClosing);
			} else {
				theDbg.logInfo("BonyRenderContext returned null JFrame, so we have no window to close.");
			}
		} else {
			theDbg.logWarning("shutdown...() found null BonyRenderContext");
		}
	}
	
}
