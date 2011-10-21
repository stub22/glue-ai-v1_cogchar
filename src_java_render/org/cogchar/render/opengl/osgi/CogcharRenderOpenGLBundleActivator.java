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
import org.cogchar.render.opengl.bony.sys.BonyContext;
import org.cogchar.render.opengl.bony.demo.StickFigureTestMain;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jme3.system.AppSettings;

/**
 *
 * @author Stu B. <www.texpedient.com>
 */
public class CogcharRenderOpenGLBundleActivator extends BundleActivatorBase {
	static Logger theLogger = LoggerFactory.getLogger(CogcharRenderOpenGLBundleActivator.class);
	private	BonyContext		myBonyContext;
	@Override
	protected Logger getLogger() {
		return theLogger;
	}	
	
	// This is the primary service export point for the bundle, as of 2011-08-30.
	public BonyContext getBonyContext() { 
		return myBonyContext;
	}

	@Override public void start(BundleContext bundleCtx) throws Exception {
		super.start(bundleCtx);
		Bundle b = bundleCtx.getBundle();
		theLogger.info("bundle=" + b);
		

		ClassLoader tccl = Thread.currentThread().getContextClassLoader();
		theLogger.info("Saved old class loader: " + tccl);
		try {
			ClassLoader localLoader = getClass().getClassLoader();
			theLogger.info("Setting thread class loader to local loader: " + localLoader);
			Thread.currentThread().setContextClassLoader(localLoader);
			int canvasWidth = StickFigureTestMain.DEFAULT_CANVAS_WIDTH; 
			int canvasHeight = StickFigureTestMain.DEFAULT_CANVAS_HEIGHT;

			String lwjglRendererName = AppSettings.LWJGL_OPENGL_ANY;
				// String LWJGL_OPENGL1,
				//		LWJGL_OPENGL2, LWJGL_OPENGL3, LWJGL_OPENGL_ANY;
			System.out.println("********++++++++++++++++++++ Using: " + lwjglRendererName);
			// Setup our crude demo using test model exported by Leo from Maya.
			myBonyContext = StickFigureTestMain.initStickFigureApp(lwjglRendererName, 
							canvasWidth, canvasHeight); 
			/*
			 * At this point, the following setup is still required to be done by enclosing application.
			 * This ordering is somewhat strict, due to a lot of interlocking assumptions.
			 * 
			 *		BonyContext bc = *** Lookup the BonyContext as OSGi service.
			 *		VirtCharPanel vcp = bc.getPanel();
			 *		JFrame jf = vcp.makeEnclosingJFrame();
			 *		bc.setFrame(jf);
			 *		*** Setup frame to handle windowClosing and windowClosed as shown in StickFigureTestMain.java.
			 * 		BonyVirtualCharApp app = bc.getApp();
			 *		app.startJMonkeyCanvas();
			 *		((BonyStickFigureApp) app).setScoringFlag(true);	
			 */
			
				
			// OR: choose from JME tests, run in system OpenGL window.
			// Some tests are missing required libraries.
			// TestChooserWrapper.displayTestChooser(b, null);  

		} finally {
			theLogger.info("Restoring old class loader: " + tccl);
			Thread.currentThread().setContextClassLoader(tccl);
		}
		bundleCtx.registerService(BonyContext.class.getName(), myBonyContext, null);
		// System.out.println("Returned from CogcharImplActivator.start()");

    }
	@Override public void stop(BundleContext bundleCtx) throws Exception {
		super.stop(bundleCtx);
		// Attempt to cleanup OpenGL resources, which happens nicely in standalone demo if the window is X-ed.
		// (Probably cleanup is happening during dispose(), so direct call to that should work too).
		BonyContext bc = getBonyContext();
		if (bc != null) {
			JFrame jf = bc.getFrame();
			if (jf != null) {
				theLogger.info("Sending WINDOW_CLOSING event to BonyContext.JFrame");
				WindowEvent windowClosing = new WindowEvent(jf, WindowEvent.WINDOW_CLOSING);
				jf.dispatchEvent(windowClosing);	
			} else {
				theLogger.warn("BonyContext returned null JFrame, so we have no window to close.");
			}
		} else {
			theLogger.warn("stop() found null BonyContext");
		}
		theLogger.info("stop() is DONE!");
	}
}
