package org.cogchar.bundle.render.resources;

import org.appdapter.osgi.core.BundleActivatorBase;
import org.osgi.framework.BundleContext;

import org.cogchar.render.sys.core.AssetContext;
import org.cogchar.render.sys.core.JmonkeyAssetLocation;
import org.cogchar.render.sys.core.RenderRegistryFuncs;

public class ResourceBundleActivator extends BundleActivatorBase {


	@Override public void start(BundleContext bundleCtx) throws Exception {
		super.start(bundleCtx);
		logInfo("******************* Registering assumed resource bundle with default AssetContext");
		
		AssetContext defAssetCtx = RenderRegistryFuncs.findOrMakeAssetContext(null, null, ResourceBundleActivator.class);
		JmonkeyAssetLocation jmal = new JmonkeyAssetLocation(ResourceBundleActivator.class);
		defAssetCtx.addAssetSource(jmal);		

	}

}
