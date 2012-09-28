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

	}

}
