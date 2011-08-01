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

package org.cogchar.osgi.impl;

import org.cogchar.bony.BonyContext;
import org.cogchar.bony.StickFigureTestMain;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 *
 * @author Stu B. <www.texpedient.com>
 */
public class CogcharImplActivator implements BundleActivator {

	private	BonyContext		myBonyContext;
	@Override public void start(BundleContext bundleCtx) throws Exception {
        System.out.println("CogcharImplActivator.start(), bundleContext=" + bundleCtx);
		Bundle b = bundleCtx.getBundle();
		System.out.println("bundle=" + b);
		

		ClassLoader tccl = Thread.currentThread().getContextClassLoader();
		System.out.println("Saved old class loader: " + tccl);
		try {
			ClassLoader localLoader = getClass().getClassLoader();
			System.out.println("Setting thread class loader to local loader: " + localLoader);
			Thread.currentThread().setContextClassLoader(localLoader);
			
			// Our crude demo using test model exported by Leo from Maya.
			myBonyContext = StickFigureTestMain.initStickFigureApp(false); 
			
			
				
			// OR: choose from JME tests, run in system OpenGL window.
			// Some tests are missing required libraries.
			// TestChooserWrapper.displayTestChooser(b, null);  

		} finally {
			System.out.println("Restoring old class loader: " + tccl);
			Thread.currentThread().setContextClassLoader(tccl);
		}
		bundleCtx.registerService(BonyContext.class.getName(), myBonyContext, null);
		// System.out.println("Returned from CogcharImplActivator.start()");

    }
	@Override public void stop(BundleContext context) throws Exception {
        System.out.println("CogcharImplActivator.stop()");
		// TODO:  Use the BonyContext to stop JMonkey canvas....
    }
	public BonyContext getBonyContext() { 
		return myBonyContext;
	}
}
