/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cogchar.osgi.impl;

import org.cogchar.bony.TestChooserWrapper;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 *
 * @author Stu B. <www.texpedient.com>
 */
public class CogcharImplActivator implements BundleActivator {

	@Override public void start(BundleContext context) throws Exception {
        System.out.println("CogcharImplActivator.start(), bundleContext=" + context);
		Bundle b = context.getBundle();
		System.out.println("bundle=" + b);
		

		ClassLoader tccl = Thread.currentThread().getContextClassLoader();
		System.out.println("Saved old class loader: " + tccl);
		try {
			ClassLoader localLoader = getClass().getClassLoader();
			System.out.println("Setting thread loader to local loader: " + localLoader);
			Thread.currentThread().setContextClassLoader(localLoader);
			// WomanFaceTest.main(null);
			TestChooserWrapper.displayTestChooser(b, null);
			// jme3test.helloworld.HelloAnimation.main(null);
			// jme3test.TestChooser.main(null);
		} finally {
			System.out.println("Restoring old loader: " + tccl);
			Thread.currentThread().setContextClassLoader(tccl);
		}
		// System.out.println("Returned from WomanFaceTest.main()");

    }

	@Override public void stop(BundleContext context) throws Exception {
        System.out.println("CogcharImplActivator.stop()");
    }

}
