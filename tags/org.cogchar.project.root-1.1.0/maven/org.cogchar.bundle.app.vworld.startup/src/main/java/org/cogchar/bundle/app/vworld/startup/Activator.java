package org.cogchar.bundle.app.vworld.startup;


import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.appdapter.core.log.BasicDebugger;

/**
 * Currently this activator does nothing.
 */
public class Activator extends BasicDebugger implements BundleActivator {

    public void start(BundleContext context) throws Exception {

       // startVWorldLifecycle(context);   -- this is now in VirtualWorldFactory, and
		// is currently called from app-bundle activators like  *oglweb.*.Activator
		// The other registry-binding code that used to live in this activator is now in
		// VWorldMapperLifecycle.getBindings
    }

    public void stop(BundleContext context) throws Exception {
        // TODO add deactivation code here
    }

	
}
