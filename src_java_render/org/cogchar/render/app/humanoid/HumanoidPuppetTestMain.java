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

package org.cogchar.render.app.humanoid;

import org.cogchar.render.app.bony.BonyRenderContext;
import org.cogchar.render.app.bony.BonyVirtualCharApp;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.cogchar.render.gui.bony.VirtualCharacterPanel;
import org.cogchar.blob.emit.RenderConfigEmitter;
import org.cogchar.render.app.bony.BonyGameFeatureAdapter;
import org.cogchar.render.gui.bony.PanelUtils;

/**
 * Example of a Java program that is able to display a bony-humanoid in OpenGL,
 * without using OSGi, or any Robokind robotics interfaces.
 * 
 * To successfully run this test program, you must invoke it with a classpath that includes
 * the resources it wants, e.g. use the BonyDemoMain class in the org.cogchar.demo.render.opengl 
 * project.
 * 
 * @author Stu B. <www.texpedient.com>
 */
public class HumanoidPuppetTestMain {
	static Logger theLogger = LoggerFactory.getLogger(HumanoidPuppetTestMain.class);
	
	public static BonyRenderContext makeBonyRenderContextWithApp(RenderConfigEmitter rce) { 
		BonyVirtualCharApp bvcApp = new HumanoidPuppetApp(rce); 
		VirtualCharacterPanel vcp = PanelUtils.makeVCPanel(rce, "FULL");
		bvcApp.initCharPanelWithCanvas(vcp);
		BonyRenderContext bc = bvcApp.getBonyRenderContext();
		return bc;
	}
	public static void main(String[] args) {
		try {
			startTest();
		} catch (Throwable t) {
			theLogger.error("Caught Exception", t);
		}
	}
	public static void startTest() throws Throwable { 
		RenderConfigEmitter rce = new RenderConfigEmitter();
		final BonyRenderContext bc = makeBonyRenderContextWithApp(rce); 
		// Frame must be packed after panel created, but created 
		// before startJMonkey.  Might add frame to BonyRenderContext...
		VirtualCharacterPanel vcp = bc.getPanel();
		// theLogger.info("*********************** BEFORE FRAMING: VCP width="  + vcp.getWidth() + ", height=" + vcp.getHeight());
		JFrame jf = vcp.makeEnclosingJFrame("Humanoid Puppet Test");
		bc.setFrame(jf);
		jf.addWindowListener(new WindowAdapter() {
			@Override public void	windowClosing(WindowEvent e) {
				theLogger.info("StickFigureTestMain.JFrame.windowClosing event:  {}", e);
				theLogger.info("NOT explicitly calling requestClose() on the app, letting the LWJGL thread detect dispose of the canvas instead");
				BonyVirtualCharApp app = bc.getApp();
				// JMonkey code sez this requestClose() method is "internal use only".
				// Results in an abrupt close, without lwjgl shutdown and thread closure.
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
				theLogger.info("VirtCharPanel.JFrame Window CLOSED event:  {}", e);
				System.out.println("VirtCharPanel.JFrame.closed, exiting");
				System.exit(0);
			}
		});
		BonyVirtualCharApp app = bc.getApp();
		app.startJMonkeyCanvas();
		
		HumanoidRenderContext hrc = (HumanoidRenderContext) app.getBonyRenderContext();
		
		hrc.runPostInitLaunchOnJmeThread();
		
		hrc.initCinematicParameters();
		
		hrc.getGameFeatureAdapter().setScoringFlag(true);
		// theLogger.info("*********************** AFTER FRAMING + STARTING: VirtCharPanel width="  + vcp.getWidth() + ", height=" + vcp.getHeight());
		theLogger.info("*********************** Frame width={}, height={}", jf.getWidth(), jf.getHeight());

		// OR, run a JMonkey demo in a standalone OpenGL system window (not a Java/Swing GUI).
		// Most demos support camera nav using mouse/arrow-keys to rotate, and W,S A,D, Q,Z to move.
		// Generally you want to run just ONE of the following main methods:
			
		// JME3 Sinbad demo - includes grimacing facial expressions!
		// jme3test.bullet.TestBoneRagdoll.main(null);    //  Spacebar to make 'im do a pushup, then shoot 'im agin!

		// jme3test.helloworld.HelloAnimation.main(null);   // Press spacebar to walk
		// jme3test.bullet.TestBrickTower.main(null);       // shoot bricks
		// jme3test.animation.TestMotionPath(null);			// space, u, i, j, p
		// jme3test.model.anim.TestOgreComplexAnim(null);   // not interactive		
		
		// jme3test.TestChooser.main(null);
	}		
}
