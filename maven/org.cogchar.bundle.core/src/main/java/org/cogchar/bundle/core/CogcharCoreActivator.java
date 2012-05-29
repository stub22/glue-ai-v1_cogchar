package org.cogchar.bundle.core;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import org.appdapter.osgi.core.BundleActivatorBase;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;



public class CogcharCoreActivator  extends BundleActivatorBase {


    @Override public void start(BundleContext context) throws Exception {
		// We assume some other bundle has configured SLF4J for us.
		super.start(context);	
		
		context.addFrameworkListener(new FrameworkListener() {
			public void frameworkEvent(FrameworkEvent fe) {
				logInfo("************************ Got frameworkEvent: " + fe);
				logInfo("EventType=" + fe.getType() + ", bundle=" + fe.getBundle());
			}
		});
	}
	@Override public void stop(BundleContext context) throws Exception {
		super.stop(context);	
	}
}
