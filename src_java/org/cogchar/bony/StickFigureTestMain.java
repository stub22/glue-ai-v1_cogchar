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
	
	public static void main(String[] args) {
		BonyStickFigureApp stickFigureApp = new BonyStickFigureApp(sceneFilePath, sceneLocalScale);
		BonySystem.setJMonkeySettings(stickFigureApp, canvasWidth, canvasHeight);
		stickFigureApp.initCharPanelWithCanvas();
		VirtCharPanel vcp = stickFigureApp.getBonyContext().getPanel();

		stickFigureApp.startJMonkeyCanvas();
		stickFigureApp.setScoringFlag(true);	
		JFrame jf = vcp.makeEnclosingJFrame();		
		// owTst.start(JmeContext.Type.Display);
	}	
}
