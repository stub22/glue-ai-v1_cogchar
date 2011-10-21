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

package org.cogchar.render.opengl.bony.demo;

import org.cogchar.render.opengl.bony.sys.VirtCharPanel;
import org.cogchar.render.opengl.bony.sys.BonyContext;
import org.cogchar.render.opengl.bony.sys.BonySystemFuncs;
import org.cogchar.render.opengl.bony.app.BonyStickFigureApp;
import org.cogchar.render.opengl.bony.app.BonyRagdollApp;
import org.cogchar.render.opengl.bony.app.BonyVirtualCharApp;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jme3.system.AppSettings;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class StickFigureTestMain {
	static Logger theLogger = LoggerFactory.getLogger(StickFigureTestMain.class);
	
	static String	sceneFilePath = "leo_hanson_tests/test3/test3.scene";
	static float	sceneLocalScale = 0.5f;
	

	public static int  DEFAULT_CANVAS_WIDTH = 800, DEFAULT_CANVAS_HEIGHT = 600;
	

	public static BonyContext initStickFigureApp(String lwjglRendererName, int canvasWidth, int canvasHeight) { 
		BonyStickFigureApp stickFigureApp = // new BonyStickFigureApp(sceneFilePath, sceneLocalScale);
					new BonyRagdollApp(lwjglRendererName, sceneFilePath, sceneLocalScale);
		BonySystemFuncs.setJMonkeySettings(stickFigureApp, canvasWidth, canvasHeight);
		stickFigureApp.initCharPanelWithCanvas();
		BonyContext bc = stickFigureApp.getBonyContext();
		return bc;
	}
	public static void main(String[] args) {
		final BonyContext bc = initStickFigureApp(AppSettings.LWJGL_OPENGL_ANY, DEFAULT_CANVAS_WIDTH, DEFAULT_CANVAS_HEIGHT);
		// Frame must be packed after panel created, but created 
		// before startJMonkey.  Might add frame to BonyContext...
		VirtCharPanel vcp = bc.getPanel();
		theLogger.info("*********************** BEFORE FRAMING: VirtCharPanel width="  + vcp.getWidth() + ", height=" + vcp.getHeight());
		JFrame jf = vcp.makeEnclosingJFrame();
		bc.setFrame(jf);
		jf.addWindowListener(new WindowAdapter() {
			@Override public void	windowClosing(WindowEvent e) {
				theLogger.info("StickFigureTestMain.JFrame.windowClosing event:  " + e);
				theLogger.info("NOT explicitly calling requestClose() on the app, letting the LWJGL thread detect dispose of the canvas instead");
				BonyVirtualCharApp app = bc.getApp();
				// JMonkey sez this method is "internal use only".
				// Results in an abrubt close, without lwjgl shutdown and thread closure.
				// The only messages then occur AFTER this method returns, saying:
				/*
				 * com.jme3.system.lwjgl.LwjglCanvas$GLCanvas removeNotify
				 * INFO: EDT: Application is stopped. Not restoring canvas.
				 */
				//app.requestClose(false);
				theLogger.info("StickFigureTestMain.JFrame.windowClosing - END");
				/*
				 * Note that line 207 of JME3:  LwjglAbstractDisplay says:
	                if (Display.isCloseRequested())
		                listener.requestClose(false);
				 * and with no explicit call of our own to requestClose, we get:
				 * 
4042 [AWT-EventQueue-0] INFO org.cogchar.render.opengl.bony.StickFigureTestMain - StickFigureTestMain.JFrame.windowClosing - END
Oct 7, 2011 7:44:27 PM com.jme3.system.lwjgl.LwjglCanvas$GLCanvas removeNotify
INFO: EDT: Notifying OGL that canvas is about to become invisible..
Oct 7, 2011 7:44:27 PM com.jme3.system.lwjgl.LwjglCanvas runLoop
INFO: OGL: Received destroy request! Complying..
Oct 7, 2011 7:44:27 PM com.jme3.system.lwjgl.LwjglCanvas pauseCanvas
INFO: OGL: Canvas will become invisible! Destroying ..
Oct 7, 2011 7:44:27 PM com.jme3.system.lwjgl.LwjglCanvas$GLCanvas removeNotify
INFO: EDT: Acknowledged receipt of canvas death
4144 [AWT-EventQueue-0] INFO org.cogchar.render.opengl.bony.StickFigureTestMain - VirtCharPanel.JFrame Window CLOSED event:  java.awt.event.WindowEvent[WINDOW_CLOSED,opposite=null,oldState=0,newState=0] on frame0
VirtCharPanel.JFrame.closed, exiting
				 * 
				 */
			}
			@Override public void	windowClosed(WindowEvent e) {
				theLogger.info("VirtCharPanel.JFrame Window CLOSED event:  " + e);
				System.out.println("VirtCharPanel.JFrame.closed, exiting");
				System.exit(0);
			}
		});
		BonyVirtualCharApp app = bc.getApp();
		app.startJMonkeyCanvas();
		((BonyStickFigureApp) app).setScoringFlag(true);
		theLogger.info("*********************** AFTER FRAMING + STARTING: VirtCharPanel width="  + vcp.getWidth() + ", height=" + vcp.getHeight());
		theLogger.info("*********************** Frame width="  + jf.getWidth() + ", height=" + jf.getHeight());

		// OR, run a JMonkey demo in a standalone OpenGL system window (not a Java/Swing GUI).
	
		// Generally you want to run just ONE of the following main methods:
			
		// Most demos support camera nav using mouse and/or W,A,S,D and arrow keys

		// This is the most impressive relevant JME3 demo - recently updated with facial expressions!
		// jme3test.bullet.TestBoneRagdoll.main(null);    //  Spacebar to make him do a pushup, then shoot him ...


		// jme3test.helloworld.HelloAnimation.main(null);   // Press spacebar to walk
		// jme3test.bullet.TestBrickTower.main(null);       // shoot bricks
		// jme3test.animation.TestMotionPath(null);			// space, u, i, j, p
		// jme3test.model.anim.TestOgreComplexAnim(null);   // not interactive		
		
		// jme3test.TestChooser.main(null);
	}		
}
