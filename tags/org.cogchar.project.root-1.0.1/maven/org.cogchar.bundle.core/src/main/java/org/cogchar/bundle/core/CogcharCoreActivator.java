package org.cogchar.bundle.core;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import org.appdapter.osgi.core.BundleActivatorBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CogcharCoreActivator  extends BundleActivatorBase {

	static Logger theLogger = LoggerFactory.getLogger(CogcharCoreActivator.class);
	
	@Override protected Logger getLogger() {
		return theLogger;
	}
    @Override public void start(BundleContext context) throws Exception {
		// We assume some other bundle has configured SLF4J for us.
		super.start(context);	
	}
	@Override public void stop(BundleContext context) throws Exception {
		super.stop(context);	
	}
}