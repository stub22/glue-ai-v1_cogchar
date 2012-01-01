package org.cogchar.bundle.render.resources;

import java.net.URL;
import org.appdapter.osgi.core.BundleActivatorBase;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceBundleActivator extends BundleActivatorBase {
	static Logger theLogger = LoggerFactory.getLogger(ResourceBundleActivator.class);

	//public static URL	theBundleRootURL;
	//public static ResourceBundleActivator theInstance;
	@Override protected Logger getLogger() {
		return theLogger;
	}	
	@Override public void start(BundleContext bundleCtx) throws Exception {
		super.start(bundleCtx);
		/*
		Bundle bun = bundleCtx.getBundle();
		theLogger.info("Got bundle: " + bun);
		theLogger.info("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Bundle location: " + bun.getLocation());
		theLogger.info("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Bundle symbolic name: " + bun.getSymbolicName());
		URL bundleRootURL = bun.getEntry("/");
		theLogger.info("Root URL: " + bundleRootURL);
		theBundleRootURL = bundleRootURL;
		theInstance = this;
		 * 
		 */
	}

}
