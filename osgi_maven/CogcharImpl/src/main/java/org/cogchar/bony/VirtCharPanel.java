/*
 *  Copyright 2011 by The Friendularity Project (www.friendularity.org).
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

package org.cogchar.bony;

import java.awt.Canvas;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class VirtCharPanel extends javax.swing.JPanel {

    /** Creates new form VirtCharPanel */
    public VirtCharPanel() {
        initComponents();
    }

	public void setRenderCanvas (Canvas c) {
		renderPanel.add(c);
	}
	public int getTestChannelNum() {
		return (Integer) jspin_animTestChannelNum.getValue();
	}
	public String getTestChannelModifier() {
		return (String) jspin_animTestChannelMod.getValue();
	}
	public String getTestDirection() {
		return (String) jspin_animTestDirectionNum.getValue();
	}

	public void setDumpText(String dt) {
		jtxta_dump.setText(dt);
	}
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        controlPanel = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jspin_animTestChannelNum = new javax.swing.JSpinner();
        jspin_animTestDirectionNum = new javax.swing.JSpinner();
        jScrollPane1 = new javax.swing.JScrollPane();
        jtxta_dump = new javax.swing.JTextArea();
        jspin_animTestChannelMod = new javax.swing.JSpinner();
        renderPanel = new javax.swing.JPanel();

        setPreferredSize(new java.awt.Dimension(640, 640));
        setLayout(new java.awt.BorderLayout());

        controlPanel.setBackground(new java.awt.Color(255, 204, 204));
        controlPanel.setPreferredSize(new java.awt.Dimension(640, 150));

        jButton1.setText("jButton1");

        jspin_animTestChannelNum.setModel(new javax.swing.SpinnerNumberModel(0, 0, 3, 1));

        jspin_animTestDirectionNum.setModel(new javax.swing.SpinnerListModel(new String[] {"pitch", "roll", "yaw"}));

        jtxta_dump.setColumns(20);
        jtxta_dump.setRows(5);
        jScrollPane1.setViewportView(jtxta_dump);

        jspin_animTestChannelMod.setModel(new javax.swing.SpinnerListModel(new String[] {"self", "first child"}));

        javax.swing.GroupLayout controlPanelLayout = new javax.swing.GroupLayout(controlPanel);
        controlPanel.setLayout(controlPanelLayout);
        controlPanelLayout.setHorizontalGroup(
            controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(controlPanelLayout.createSequentialGroup()
                .addGroup(controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(controlPanelLayout.createSequentialGroup()
                        .addGap(73, 73, 73)
                        .addComponent(jspin_animTestDirectionNum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(36, 36, 36)
                        .addComponent(jspin_animTestChannelNum, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(41, 41, 41)
                        .addComponent(jspin_animTestChannelMod, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 130, Short.MAX_VALUE)
                        .addComponent(jButton1))
                    .addGroup(controlPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 509, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        controlPanelLayout.setVerticalGroup(
            controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(controlPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jspin_animTestDirectionNum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jspin_animTestChannelNum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jspin_animTestChannelMod, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE))
        );

        add(controlPanel, java.awt.BorderLayout.NORTH);

        renderPanel.setBackground(new java.awt.Color(0, 51, 0));
        renderPanel.setPreferredSize(new java.awt.Dimension(640, 480));

        javax.swing.GroupLayout renderPanelLayout = new javax.swing.GroupLayout(renderPanel);
        renderPanel.setLayout(renderPanelLayout);
        renderPanelLayout.setHorizontalGroup(
            renderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 537, Short.MAX_VALUE)
        );
        renderPanelLayout.setVerticalGroup(
            renderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 173, Short.MAX_VALUE)
        );

        add(renderPanel, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel controlPanel;
    private javax.swing.JButton jButton1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSpinner jspin_animTestChannelMod;
    private javax.swing.JSpinner jspin_animTestChannelNum;
    private javax.swing.JSpinner jspin_animTestDirectionNum;
    private javax.swing.JTextArea jtxta_dump;
    private javax.swing.JPanel renderPanel;
    // End of variables declaration//GEN-END:variables

}
