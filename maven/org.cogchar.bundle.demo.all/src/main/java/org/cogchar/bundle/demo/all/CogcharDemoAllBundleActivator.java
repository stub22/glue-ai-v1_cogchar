package org.cogchar.bundle.demo.all;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CogcharDemoAllBundleActivator implements BundleActivator {
	static Logger theLogger = LoggerFactory.getLogger(CogcharDemoAllBundleActivator.class);
    public void start(BundleContext context) throws Exception {
        // TODO add activation code here
		String startupMsg = getClass().getCanonicalName() + ".start(ctx=" + context + ")";
		System.out.println("[System.out]" + startupMsg);
		theLogger.info("[SLF4J]" + startupMsg);
    }

    public void stop(BundleContext context) throws Exception {
		String windupMsg = getClass().getCanonicalName() + ".stop(ctx=" + context + ")";
		theLogger.info("[SLF4J]" + windupMsg);		
		System.out.println("[System.out]" + windupMsg);		
    }

}
