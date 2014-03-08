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

/*
 * VirtCharPanel.java
 *
 * Created on Apr 4, 2011, 1:29:28 PM
 */

package org.cogchar.render.gui.bony;

import java.awt.BorderLayout;
import java.awt.Canvas;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.cogchar.render.app.bony.BodyController;
import org.cogchar.render.app.bony.VerbalController;
import org.cogchar.render.gui.bony.PanelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class VirtCharPanel extends javax.swing.JPanel implements VirtualCharacterPanel {
	static Logger theLogger = LoggerFactory.getLogger(VirtCharPanel.class);
    /** Creates new form VirtCharPanel */
    public VirtCharPanel() {
        initComponents();
    }

	@Override public void setRenderCanvas (Canvas c) {
		renderPanel.setPreferredSize(c.getPreferredSize());
		renderPanel.add(c, BorderLayout.CENTER);
	}
	
	@Override public JFrame makeEnclosingJFrame(String title) {
		return PanelUtils.makeEnclosingJFrame(this, title);
	}
	@Override public JPanel getJPanel() {
		return this;
	}

	@Override public BodyController getBodyController() {
		return null;
	}
	@Override public VerbalController getVerbalController() {
		return null;
	}

	
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        renderPanel = new javax.swing.JPanel();

        setPreferredSize(new java.awt.Dimension(800, 600));
        setLayout(new java.awt.BorderLayout());

        renderPanel.setBackground(new java.awt.Color(0, 51, 0));
        renderPanel.setPreferredSize(new java.awt.Dimension(800, 600));
        renderPanel.setLayout(new java.awt.BorderLayout());
        add(renderPanel, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel renderPanel;
    // End of variables declaration//GEN-END:variables




}