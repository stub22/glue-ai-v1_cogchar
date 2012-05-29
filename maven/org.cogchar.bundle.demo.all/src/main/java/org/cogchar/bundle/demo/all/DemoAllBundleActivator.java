package org.cogchar.bundle.demo.all;


import org.appdapter.osgi.core.BundleActivatorBase;

import org.cogchar.bundle.app.puma.PumaAppContext;
import org.cogchar.bundle.app.puma.PumaBooter;

import org.osgi.framework.BundleContext;

public class DemoAllBundleActivator extends BundleActivatorBase {
	@Override public void start(BundleContext bundleCtx) throws Exception {
		
		super.start(bundleCtx);
		
		PumaBooter	pumaBooter = new PumaBooter();
		PumaBooter.BootResult bootResult = pumaBooter.bootUnderOSGi(bundleCtx);
		/*
		
		String uriPrefix = "http://model.cogchar.org/char/bony/";
		String bonyCharUniqueSuffix = "0x0000FFFF";
		String sysContextURI = "http://model.cogchar.org/char/bony/" + bonyCharUniqueSuffix;
		String debugTxt = "sysContextURI = [" + sysContextURI + "]";
		logInfo("==============================\nStarting " + debugTxt);
		
		
		PumaAppContext pac = new PumaAppContext(bundleCtx, sysContextURI, null);
		try {
			pac.makeDualCharsForSwingOSGi();
		} catch (Throwable t) {
			theLogger.error("Cannot initialize " + debugTxt, t);
		}
		
		theLogger.info("Started" + debugTxt + "\n========================================");
		* 
		*/ 
	}

}
