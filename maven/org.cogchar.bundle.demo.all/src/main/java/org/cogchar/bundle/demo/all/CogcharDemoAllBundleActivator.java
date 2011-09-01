package org.cogchar.bundle.demo.all;

import javax.swing.JFrame;
import org.appdapter.osgi.core.BundleActivatorBase;
import org.cogchar.render.opengl.bony.BonyContext;
import org.cogchar.render.opengl.bony.VirtCharPanel;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CogcharDemoAllBundleActivator extends BundleActivatorBase {
	static Logger theLogger = LoggerFactory.getLogger(CogcharDemoAllBundleActivator.class);
	
	@Override protected Logger getLogger() {
		return theLogger;
	}
	@Override public void start(BundleContext bundleCtx) throws Exception {
		super.start(bundleCtx);
		BonyContext bc = getBonyContext(bundleCtx);
		theLogger.info("Got BonyContext: " + bc);
		VirtCharPanel vcp = bc.getPanel();
		JFrame jf = vcp.makeEnclosingJFrame();

	}
	
	private BonyContext getBonyContext(BundleContext bundleCtx) {
		ServiceReference ref = bundleCtx.getServiceReference(BonyContext.class.getName());
		if(ref == null){
			return null;
		}
		return (BonyContext) bundleCtx.getService(ref);
	}
}
