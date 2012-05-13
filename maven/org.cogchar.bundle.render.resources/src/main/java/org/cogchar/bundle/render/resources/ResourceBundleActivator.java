package org.cogchar.bundle.render.resources;

import org.appdapter.osgi.core.BundleActivatorBase;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.cogchar.render.sys.core.AssetContext;
import org.cogchar.render.sys.core.JmonkeyAssetLocation;
import org.cogchar.render.sys.core.RenderRegistryFuncs;

public class ResourceBundleActivator extends BundleActivatorBase {
	static Logger theLogger = LoggerFactory.getLogger(ResourceBundleActivator.class);

	//public static URL	theBundleRootURL;
	//public static ResourceBundleActivator theInstance;
	@Override protected Logger getLogger() {
		return theLogger;
	}	
	@Override public void start(BundleContext bundleCtx) throws Exception {
		super.start(bundleCtx);
		theLogger.info("******************* Registering assumed resource bundle with default AssetContext");
		//
		AssetContext defAssetCtx = RenderRegistryFuncs.findOrMakeAssetContext(null, null, ResourceBundleActivator.class);
		JmonkeyAssetLocation jmal = new JmonkeyAssetLocation(ResourceBundleActivator.class);
		defAssetCtx.addAssetSource(jmal);		
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
