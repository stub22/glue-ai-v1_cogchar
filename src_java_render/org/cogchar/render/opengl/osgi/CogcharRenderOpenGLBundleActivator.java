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



import org.appdapter.osgi.core.BundleActivatorBase;
import org.cogchar.render.opengl.bony.BonyContext;
import org.cogchar.render.opengl.bony.StickFigureTestMain;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
			
			// Our crude demo using test model exported by Leo from Maya.
			myBonyContext = StickFigureTestMain.initStickFigureApp(); 
			
			
				
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
	

}
