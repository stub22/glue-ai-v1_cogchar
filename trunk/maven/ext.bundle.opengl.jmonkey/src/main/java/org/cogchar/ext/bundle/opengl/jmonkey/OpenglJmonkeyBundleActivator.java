package org.cogchar.ext.bundle.opengl.jmonkey;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class OpenglJmonkeyBundleActivator implements BundleActivator {

    public void start(BundleContext context) throws Exception {
        System.out.println("[System.out] - " + getClass().getName()+  " - start(" + context + ") [This msg unsynch with your logger]");
    }

    public void stop(BundleContext context) throws Exception {
		System.out.println("[System.out] - " + getClass().getName() + " - stop(" + context + ") [This msg unsynch with your logger]");
    }

}
