/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cogchar.osgi.impl;

import org.cogchar.bony.BonyVirtualCharApp;
import org.cogchar.bony.StickFigureTestMain;
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
			System.out.println("Setting thread class loader to local loader: " + localLoader);
			Thread.currentThread().setContextClassLoader(localLoader);
			
			// org.cogchar.osgi.scalatest.Wacko.hey();
			
			// Generally you want to run just ONE of the following main methods:
			StickFigureTestMain.main(null);  // Our crude demo using test model exported by Leo from Maya.
			
			// Most demos support camera nav using mouse and/or W,A,S,D and arrow keys

			// This is the most impressive relevant JME3 demo - recently updated with facial expressions!
			// jme3test.bullet.TestBoneRagdoll.main(null);    //  Spacebar to make him do a pushup, then shoot him ...

			// Esc-key to leave a demo.
			// Sometimes you can run another demo, sometimes you need to quit and restart.
			
			// TestChooserWrapper.displayTestChooser(b, null);  // Choose from JME tests, some of which work!
			// jme3test.helloworld.HelloAnimation.main(null);   // Press spacebar to walk
			// jme3test.bullet.TestBrickTower.main(null);       // shoot bricks
			// jme3test.animation.TestMotionPath(null);			// space, u, i, j, p
			// jme3test.model.anim.TestOgreComplexAnim(null);   // not interactive

		} finally {
			System.out.println("Restoring old class loader: " + tccl);
			Thread.currentThread().setContextClassLoader(tccl);
		}
		// System.out.println("Returned from StickFigureTestApp.main()");

    }

	@Override public void stop(BundleContext context) throws Exception {
        System.out.println("CogcharImplActivator.stop()");
    }

}
