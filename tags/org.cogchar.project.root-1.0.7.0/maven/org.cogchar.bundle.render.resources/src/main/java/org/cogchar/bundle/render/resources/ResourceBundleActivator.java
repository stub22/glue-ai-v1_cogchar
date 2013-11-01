package org.cogchar.bundle.render.resources;

import org.appdapter.osgi.core.BundleActivatorBase;
import org.osgi.framework.BundleContext;

import org.cogchar.render.sys.asset.AssetContext;
import org.cogchar.render.sys.asset.JmonkeyAssetLocation;
import org.cogchar.render.sys.registry.RenderRegistryFuncs;

public class ResourceBundleActivator extends BundleActivatorBase {


	@Override public void start(BundleContext bundleCtx) throws Exception {
		super.start(bundleCtx);
		logInfo("******************* Registering org.cogchar.bundle.render.resources with default AssetContext");
		
		AssetContext defAssetCtx = RenderRegistryFuncs.findOrMakeAssetContext(null, null, ResourceBundleActivator.class);
		JmonkeyAssetLocation jmal = new JmonkeyAssetLocation(ResourceBundleActivator.class);
		defAssetCtx.addAssetSource(jmal);		
		logInfo("******************* Finished registering org.cogchar.bundle.render.resources with default AssetContext");
	}

}
