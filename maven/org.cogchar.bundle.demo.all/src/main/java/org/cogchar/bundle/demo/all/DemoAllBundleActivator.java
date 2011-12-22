package org.cogchar.bundle.demo.all;

import org.cogchar.bundle.app.puma.PumaDualCharacter;
import java.io.File;
import java.net.URISyntaxException;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.swing.JFrame;
import org.apache.qpid.client.AMQQueue;
import org.appdapter.osgi.core.BundleActivatorBase;
import org.cogchar.bind.rk.robot.client.RobotAnimClient;
import org.cogchar.bind.rk.robot.model.ModelRobot;
import org.cogchar.bind.rk.robot.model.ModelRobotUtils;
import org.cogchar.bundle.app.puma.PumaAppContext;
import org.cogchar.render.opengl.bony.app.BonyVirtualCharApp;
import org.cogchar.render.opengl.bony.sys.BonyRenderContext;
import org.cogchar.render.opengl.bony.sys.VirtCharPanel;
import org.cogchar.render.opengl.osgi.RenderBundleUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.robokind.api.motion.Robot;
import org.robokind.api.motion.utils.RobotFrameSource;
import org.robokind.api.motion.utils.RobotUtils;
import org.robokind.impl.messaging.ConnectionManager;
import org.robokind.impl.motion.messaging.JMSMotionFrameReceiver;
import org.robokind.impl.motion.messaging.MoveFrameListener;
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
