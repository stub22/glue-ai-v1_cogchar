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
package org.cogchar.render.opengl.bony.gui;
import java.awt.Canvas;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.cogchar.render.opengl.bony.app.BodyController;
import org.cogchar.render.opengl.bony.app.VerbalController;

/**
 * @author Stu B. <www.texpedient.com>
 * 
 * Netbeans was refusing to edit things properly, so made
 * this class by hand from a similar generated one.
 * 
 */
public class FancyCharPanel extends javax.swing.JPanel implements VirtualCharacterPanel {

    public FancyCharPanel() {
        initComponents();
    }
	@Override public void setRenderCanvas (Canvas c) {
		myVCP.setRenderCanvas(c);
	}	
	@Override public JFrame makeEnclosingJFrame(String title) {
		return PanelUtils.makeEnclosingJFrame(this, title);
	}	
	@Override public JPanel getJPanel() {
		return this;
	}

	@Override public BodyController getBodyController() {
		return myVTP;
	}
	@Override public VerbalController getVerbalController() {
		return myHTP;
	}
                     
    private void initComponents() {
		
		setPreferredSize(new java.awt.Dimension(880,660));

        myVCP = new org.cogchar.render.opengl.bony.gui.VirtCharPanel();
        myHTP = new org.cogchar.render.opengl.bony.gui.HorizontalToolPanel();
        myVTP = new org.cogchar.render.opengl.bony.gui.VerticalToolPanel();

        setLayout(new java.awt.BorderLayout());

        myVCP.setPreferredSize(new java.awt.Dimension(800, 600));
        add(myVCP, java.awt.BorderLayout.CENTER);

        myHTP.setPreferredSize(new java.awt.Dimension(880, 60));
        add(myHTP, java.awt.BorderLayout.SOUTH);

        myVTP.setPreferredSize(new java.awt.Dimension(80, 600));
        add(myVTP, java.awt.BorderLayout.EAST);
    }                      
                   
    private org.cogchar.render.opengl.bony.gui.HorizontalToolPanel myHTP;
    private org.cogchar.render.opengl.bony.gui.VirtCharPanel myVCP;
    private org.cogchar.render.opengl.bony.gui.VerticalToolPanel myVTP;
               


}
