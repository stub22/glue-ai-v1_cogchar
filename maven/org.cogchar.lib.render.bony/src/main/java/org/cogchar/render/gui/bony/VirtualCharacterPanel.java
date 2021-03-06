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

import java.awt.Canvas;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.cogchar.render.app.bony.BodyController;
import org.cogchar.render.app.bony.VerbalController;
import org.cogchar.render.sys.window.WindowStatusReader;

/**
 * @author Stu B. <www.texpedient.com>
 */
public interface VirtualCharacterPanel extends WindowStatusReader {
	public void setRenderCanvas (Canvas c);
	public JFrame makeEnclosingJFrame(String title, long sleepMsecAfterPack);
	public JPanel getJPanel();
	public BodyController getBodyController();
	public VerbalController getVerbalController();
	// Moved to WindowStatusReader
	// public Dimension getSize(Dimension rv);
}
