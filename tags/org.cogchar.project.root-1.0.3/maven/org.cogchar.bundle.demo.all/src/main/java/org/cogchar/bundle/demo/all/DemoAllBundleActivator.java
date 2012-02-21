package org.cogchar.bundle.demo.all;


import org.appdapter.osgi.core.BundleActivatorBase;

import org.cogchar.bundle.app.puma.PumaAppContext;

import org.osgi.framework.BundleContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DemoAllBundleActivator extends BundleActivatorBase {
	static Logger theLogger = LoggerFactory.getLogger(DemoAllBundleActivator.class);
	
	@Override protected Logger getLogger() {
		return theLogger;
	}
	@Override public void start(BundleContext bundleCtx) throws Exception {
		String uriPrefix = "http://model.cogchar.org/char/bony/";
		String bonyCharUniqueSuffix = "0x0000FFFF";
		String bonyCharURI = "http://model.cogchar.org/char/bony/" + bonyCharUniqueSuffix;
		String debugTxt = "bonyChar at URI[" + bonyCharURI + "]";
		theLogger.info("==============================\nStarting " + debugTxt);
		super.start(bundleCtx);
		
		PumaAppContext pac = new PumaAppContext(bundleCtx);
		try {
			pac.makeDualCharForSwingOSGi(bonyCharURI);
		} catch (Throwable t) {
			theLogger.error("Cannot initialize " + debugTxt, t);
		}
		
		theLogger.info("Started" + debugTxt + "\n========================================");
	}

}
