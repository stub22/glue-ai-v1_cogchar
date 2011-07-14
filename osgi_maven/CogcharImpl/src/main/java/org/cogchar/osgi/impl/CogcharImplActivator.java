/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cogchar.osgi.impl;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 *
 * @author Stu B. <www.texpedient.com>
 */
public class CogcharImplActivator implements BundleActivator {

	@Override
    public void start(BundleContext context) throws Exception {
        System.out.println("CogcharImplActivator.start()");
    }

	@Override
    public void stop(BundleContext context) throws Exception {
        System.out.println("CogcharImplActivator.stop()");
    }

}
