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

package org.cogchar.bony;

import javax.swing.JFrame;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class StickFigureTestMain {
	static String	sceneFilePath = "leo_hanson_tests/test3/test3.scene";
	static float	sceneLocalScale = 0.5f;
	static int		canvasWidth = 640, canvasHeight = 480;
	

	public static BonyContext initStickFigureApp(boolean addFrame) { 
		BonyStickFigureApp stickFigureApp = new BonyStickFigureApp(sceneFilePath, sceneLocalScale);
		BonySystem.setJMonkeySettings(stickFigureApp, canvasWidth, canvasHeight);
		stickFigureApp.initCharPanelWithCanvas();
		BonyContext bc = stickFigureApp.getBonyContext();
		if (addFrame) {
			// Frame must be packed after panel created, but created 
			// before startJMonkey.  Might add frame to BonyContext...
			VirtCharPanel vcp = bc.getPanel();
			JFrame jf = vcp.makeEnclosingJFrame();
		}	
		stickFigureApp.startJMonkeyCanvas();
		stickFigureApp.setScoringFlag(true);	
		return bc;
	}
	public static void main(String[] args) {
		BonyContext bc = initStickFigureApp(true);
	
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
