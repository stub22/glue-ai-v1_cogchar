/*
 *  Copyright 2012 by The Cogchar Project (www.cogchar.org).
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
package org.cogchar.render.gui.bony;

import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.cogchar.blob.emit.RenderConfigEmitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class PanelUtils {
	static Logger theLogger = LoggerFactory.getLogger(PanelUtils.class);	
	
	public static JFrame makeEnclosingJFrame(JPanel panel, String title) {
		if (title == null) { 
			title = panel.getClass().getName();
		}
		JFrame frame = new JFrame(title);
		theLogger.info("Making frame");
		/*
		 * public void setDefaultCloseOperation(int operation)
Sets the operation that will happen by default when the user initiates a "close" on this frame. You must specify one of the following choices:
DO_NOTHING_ON_CLOSE (defined in WindowConstants): Don't do anything; require the program to handle the operation in the windowClosing method of a registered WindowListener object.
HIDE_ON_CLOSE (defined in WindowConstants): Automatically hide the frame after invoking any registered WindowListener objects.
DISPOSE_ON_CLOSE (defined in WindowConstants): Automatically hide and dispose the frame after invoking any registered WindowListener objects.
EXIT_ON_CLOSE (defined in JFrame): Exit the application using the System exit method. Use this only in applications.
The value is set to HIDE_ON_CLOSE by default. Changes to the value of this property cause the firing of a property change event, with property name "defaultCloseOperation".
		 */
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.getContentPane().add(panel, BorderLayout.CENTER);

		theLogger.info("Packing frame - this creates OGL display and starts the LwjglAbstractDisplay thread.");
		frame.pack();
		///*  Sleep here to see that pack() in fact does trigger Lwjgl thread
		try {
			theLogger.info("Sleeping 10 sec so we can see full impact of frame.pack().");
			Thread.sleep(10000);
		} catch (Throwable t) {
			t.printStackTrace();
		}
		theLogger.info("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% 10 sec is up, now setting frame visible");
		frame.setVisible(true);	
		return frame;
	}
	public static VirtualCharacterPanel makeVCPanel (RenderConfigEmitter bce, String panelKind)  {
		VirtualCharacterPanel vcPanel = null;
		try {
			String panelClassName = bce.getVCPanelClassName(panelKind);
			Class panelClass = Class.forName(panelClassName);
			vcPanel = (VirtualCharacterPanel) panelClass.newInstance();
		} catch (Throwable t) {
			theLogger.error("Cannot load panel of kind: " + panelKind, t);
		}
		return vcPanel;
	}
			
			
}
