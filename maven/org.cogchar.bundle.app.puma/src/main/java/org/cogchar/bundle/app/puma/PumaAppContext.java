/*
 *  Copyright 2011 by The Cogchar Project (www.cogchar.org).
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.cogchar.bundle.app.puma;

import java.io.File;
import javax.swing.JFrame;

import org.robokind.api.motion.Robot;

import org.cogchar.bind.rk.robot.client.RobotAnimClient;

import org.osgi.framework.BundleContext;

import org.cogchar.bind.rk.robot.model.ModelRobot;
import org.cogchar.bind.rk.robot.model.ModelRobotUtils;
import org.cogchar.render.opengl.bony.app.BonyVirtualCharApp;
import org.cogchar.render.opengl.bony.sys.BonyRenderContext;
import org.cogchar.render.opengl.bony.gui.VirtualCharacterPanel;

import org.cogchar.render.opengl.bony.sys.JmonkeyAssetLocation;

import org.cogchar.render.opengl.osgi.RenderBundleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class PumaAppContext {

	static Logger theLogger = LoggerFactory.getLogger(PumaAppContext.class);
	private BundleContext myBundleContext;

	public PumaAppContext(BundleContext bc) {
		myBundleContext = bc;
	}

	public PumaDualCharacter makeDualCharForSwingOSGi(String dualCharURI) throws Throwable {
		startOpenGLCanvas(dualCharURI, true);
		return connectDualRobotChar(dualCharURI);
	}

	private BonyRenderContext fetchBonyRenderContext() {
		return RenderBundleUtils.getBonyRenderContext(myBundleContext);
	}
	// TODO: add URI based lookup for multiple BCs

	public BonyRenderContext getBonyRenderContext(String bonyCharURI) {
		return fetchBonyRenderContext();
	}

	public PumaDualCharacter connectDualRobotChar(String bonyCharURI)
			throws Throwable {
		
		BonyRenderContext bc = getBonyRenderContext(bonyCharURI);
		if (bc == null) {
			throw new Exception ("BonyRenderContext is null");
		}
		PumaDualCharacter pdc = new PumaDualCharacter(bc, myBundleContext);
		pdc.connectBonyDualForURI(bonyCharURI);
		return pdc;
	}

	public void startOpenGLCanvas(String dualCharURI, boolean wrapInJFrameFlag) throws Exception {
		BonyRenderContext bc = getBonyRenderContext(dualCharURI);
		theLogger.info("Got BonyRenderContext: " + bc);

		if (bc != null) {
			if (wrapInJFrameFlag) {
				VirtualCharacterPanel vcp = bc.getPanel();
				theLogger.info("Got VirtCharPanel: " + vcp);
				// Frame must be packed after panel created, but created  before startJMonkey.  
				// If startJMonkey is called first, we often hang in frame.setVisible() as JMonkey tries
				// to do some magic restart deal that doesn't work as of jme3-alpha4-August_2011.
				JFrame jf = vcp.makeEnclosingJFrame("CCRK-PUMA virtual character");
				theLogger.info("Got Enclosing Frame, adding to BonyRenderContext for WindowClose triggering: " + jf);
				// Frame will receive a close event when org.cogchar.bundle.render.opengl is STOPPED
				bc.setFrame(jf);
			}
			BonyVirtualCharApp app = bc.getApp();

			if (app.isCanvasStarted()) {
				theLogger.warn("JMonkey Canvas was already started!");
			} else {

				theLogger.info("Starting JMonkey canvas - hold yer breath! [[[[[[[[[[[[[[[[[[[[[[[[[[");
				app.startJMonkeyCanvas();
				theLogger.info("]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]  Finished starting JMonkey canvas!");
			}
			//((BonyStickFigureApp) app).setScoringFlag(true);			

		} else {
			theLogger.error("BonyRenderContext is NULL, cannot startOpenGLCanvas!");
		}
	}
}
