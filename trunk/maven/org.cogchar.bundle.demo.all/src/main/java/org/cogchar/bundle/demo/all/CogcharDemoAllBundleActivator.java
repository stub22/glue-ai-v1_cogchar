package org.cogchar.bundle.demo.all;

import javax.swing.JFrame;
import org.appdapter.osgi.core.BundleActivatorBase;
import org.cogchar.render.opengl.bony.BonyVirtualCharApp;
import org.cogchar.render.opengl.bony.BonyStickFigureApp;
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
		initOpenGLDemoStuff(bundleCtx);
		initRobokindJointPumperDemo(bundleCtx);
	}
	private void initRobokindJointPumperDemo(BundleContext bundleCtx)  throws Exception {
		RobokindJointBindingDemo rjbd = new RobokindJointBindingDemo();
		rjbd.createAndRegisterRobot(bundleCtx);
		rjbd.connectToVirtualChar(bundleCtx);
		
	}
	private void initOpenGLDemoStuff(BundleContext bundleCtx) throws Exception {
		BonyContext bc = getBonyContext(bundleCtx);
		theLogger.info("Got BonyContext: " + bc);
		if (bc != null) {
			ClassLoader tccl = Thread.currentThread().getContextClassLoader();
			try {
				// Must set context classloader so that JMonkey can find goodies
				// on the classpath, currently presumed to be in same class space
				// as the BonyContext class.  (Could generalize this and make
				// it use the loader of a configured bundle).
				ClassLoader bonyLoader = bc.getClass().getClassLoader();
				theLogger.info("Setting thread class loader to bony loader: " + bonyLoader);
				Thread.currentThread().setContextClassLoader(bonyLoader);
				VirtCharPanel vcp = bc.getPanel();
				theLogger.info("Got VirtCharPanel: " + vcp);
				// Frame must be packed after panel created, but created 
				// before startJMonkey.  If startJMonkey is called first,
				// we often hang in frame.setVisible() as JMonkey tries
				// to do some magic restart thing that doesn't work as of
				// jme3-alpha4-August 2011.
				JFrame jf = vcp.makeEnclosingJFrame();
				theLogger.info("Got Enclosing Frame, adding to BonyContext for WindowClose triggering: " + jf);
				// Frame will receive a close event when org.cogchar.bundle.render.opengl is STOPPED
				bc.setFrame(jf);
				BonyVirtualCharApp app = bc.getApp();
				theLogger.info("Starting JMonkey canvas: " + jf);
				app.startJMonkeyCanvas();
				((BonyStickFigureApp) app).setScoringFlag(true);			

			} finally {
				theLogger.info("Restoring old class loader: " + tccl);
				Thread.currentThread().setContextClassLoader(tccl);
			}
				
		} else {
			theLogger.error("BonyContext is NULL");
		}

	}
	
	protected static BonyContext getBonyContext(BundleContext bundleCtx) {
		ServiceReference ref = bundleCtx.getServiceReference(BonyContext.class.getName());
		if(ref == null){
			return null;
		}
		return (BonyContext) bundleCtx.getService(ref);
	}
}
