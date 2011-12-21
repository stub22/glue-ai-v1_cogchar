package org.cogchar.bundle.render.resources;

import org.appdapter.osgi.core.BundleActivatorBase;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceBundleActivator extends BundleActivatorBase {
	static Logger theLogger = LoggerFactory.getLogger(ResourceBundleActivator.class);

	@Override protected Logger getLogger() {
		return theLogger;
	}	

}
