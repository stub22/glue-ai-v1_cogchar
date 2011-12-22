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
import org.cogchar.render.opengl.bony.sys.BonyRenderContext;
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
import org.cogchar.blob.emit.BonyConfigEmitter;
import org.cogchar.render.opengl.bony.app.DemoApp;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class HumanoidPuppetTestMain {
	static Logger theLogger = LoggerFactory.getLogger(HumanoidPuppetTestMain.class);
	
	//static String	sceneFilePath = "leo_hanson_tests/test3/test3.scene";
	// static float	sceneLocalScale = 0.5f;
	
	public static BonyRenderContext makeBonyRenderContextWithApp(BonyConfigEmitter bce) { //   String lwjglRendererName, int canvasWidth, int canvasHeight) { 
		BonyVirtualCharApp bvcApp = // new BonyStickFigureApp(sceneFilePath, sceneLocalScale);
					// new BonyRagdollApp(lwjglRendererName, canvasWidth, canvasHeight, sceneFilePath, sceneLocalScale);
					new HumanoidPuppetApp(bce); // lwjglRendererName, HumanoidPuppetApp.PATH_HUMANOID_MESH, canvasWidth, canvasHeight);
		bvcApp.initCharPanelWithCanvas();
		BonyRenderContext bc = bvcApp.getBonyRenderContext();
		return bc;
	}
	public static void main(String[] args) {
		// String lwjglRendererName = DemoApp.DEFAULT_RENDERER_NAME;
		// System.out.println("+&+&+&++&+&+&+&+&+&+ Using: " + lwjglRendererName);
		BonyConfigEmitter bce = new BonyConfigEmitter();
		final BonyRenderContext bc = makeBonyRenderContextWithApp(bce); // lwjglRendererName, DemoApp.DEFAULT_CANVAS_WIDTH, DemoApp.DEFAULT_CANVAS_HEIGHT);
		// Frame must be packed after panel created, but created 
		// before startJMonkey.  Might add frame to BonyRenderContext...
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
4042 [AWT-EventQueue-0] INFO org.cogchar.render.opengl.bony.HumanoidPuppetTestMain - HumanoidPuppetTestMain.JFrame.windowClosing - END
Oct 7, 2011 7:44:27 PM com.jme3.system.lwjgl.LwjglCanvas$GLCanvas removeNotify
INFO: EDT: Notifying OGL that canvas is about to become invisible..
Oct 7, 2011 7:44:27 PM com.jme3.system.lwjgl.LwjglCanvas runLoop
INFO: OGL: Received destroy request! Complying..
Oct 7, 2011 7:44:27 PM com.jme3.system.lwjgl.LwjglCanvas pauseCanvas
INFO: OGL: Canvas will become invisible! Destroying ..
Oct 7, 2011 7:44:27 PM com.jme3.system.lwjgl.LwjglCanvas$GLCanvas removeNotify
INFO: EDT: Acknowledged receipt of canvas death
4144 [AWT-EventQueue-0] INFO org.cogchar.render.opengl.bony.HumanoidPuppetTestMain - VirtCharPanel.JFrame Window CLOSED event:  java.awt.event.WindowEvent[WINDOW_CLOSED,opposite=null,oldState=0,newState=0] on frame0
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
