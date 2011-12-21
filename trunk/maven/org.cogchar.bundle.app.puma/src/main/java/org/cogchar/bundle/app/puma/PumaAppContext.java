/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cogchar.bundle.app.puma;

import java.io.File;
import javax.swing.JFrame;

import org.robokind.api.motion.Robot;

import org.cogchar.bind.robokind.client.RobotAnimClient;

import org.osgi.framework.BundleContext;

import org.cogchar.bind.robokind.joint.BonyRobot;
import org.cogchar.bind.robokind.joint.BonyRobotUtils;
import org.cogchar.render.opengl.bony.app.BonyVirtualCharApp;
import org.cogchar.render.opengl.bony.sys.BonyContext;
import org.cogchar.render.opengl.bony.sys.VirtCharPanel;


import org.cogchar.render.opengl.osgi.RenderBundleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class PumaAppContext {
	static Logger theLogger = LoggerFactory.getLogger(PumaAppContext.class);	
	
	private BundleContext	myBundleContext;
	
	
	public PumaAppContext(BundleContext bc) {
		myBundleContext = bc;
	}
	public void initDualCharacter(String dualCharURI) throws Throwable { 
		BonyContext bc = fetchBonyContext();
		initOpenGLDemoStuff(bc);
		initRobokindJointPumperDemo(bc, dualCharURI);
	}
	public BonyContext fetchBonyContext() {
		return RenderBundleUtils.getBonyContext(myBundleContext);
	}
	public void initRobokindJointPumperDemo(BonyContext bc, String bonyCharURI)  
					throws Exception {
        File jointBindingConfigFile = bc.getJointConfigFileForChar(bonyCharURI);
		PumaDualCharacter rjbd = new PumaDualCharacter(myBundleContext);
		
        //rjbd.registerDummyRobot();
		rjbd.setupBonyRobotWithBlender(jointBindingConfigFile);
		BonyRobot br = rjbd.getBonyRobot();
		Robot.Id brid = br.getRobotId();
		if (br != null) {
	        rjbd.connectToVirtualChar(bc);
			RobotAnimClient brac = new RobotAnimClient(myBundleContext); 
			try {
		        BonyRobotUtils.createAndRegisterServer(myBundleContext, brid);
			} catch (Throwable t) {
				theLogger.warn("Could not register AMQP network server for robot with ID=" + brid, t);
			}
		}
	}
	public void initOpenGLDemoStuff(BonyContext bc) throws Exception {

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
				//((BonyStickFigureApp) app).setScoringFlag(true);			

			} finally {
				theLogger.info("Restoring old class loader: " + tccl);
				Thread.currentThread().setContextClassLoader(tccl);
			}
				
		} else {
			theLogger.error("BonyContext is NULL");
		}

	}	
}
