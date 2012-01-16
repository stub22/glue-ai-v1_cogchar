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

/*
 * VerticalToolPanel.java
 *
 * Created on Jan 12, 2012, 5:47:00 PM
 */

package org.cogchar.render.opengl.bony.gui;

import org.cogchar.platform.trigger.DummyBox;
import org.cogchar.platform.trigger.DummyTrigger;
import org.cogchar.render.opengl.bony.app.TwistController;

/**
 *
 * @author Stu B. <www.texpedient.com>
 */
public class VerticalToolPanel extends javax.swing.JPanel implements TwistController {
	private DummyTrigger myPokeTrigger;
	private DummyBox	 myPokeBox;
	
    /** Creates new form VerticalToolPanel */
    public VerticalToolPanel() {
        initComponents();
		setupSpinners();
    }
	private void setupSpinners() {
		spin_twistChan.setModel(new javax.swing.SpinnerNumberModel(0, 0, 3, 1));
        spin_twistDir.setModel(new javax.swing.SpinnerListModel(new String[] {"pitch", "roll", "yaw"}));
        spin_twistModel.setModel(new javax.swing.SpinnerListModel(new String[] {"self", "first child"}));
	}
	@Override public int getTwistChannelNum() {
		return (Integer) spin_twistChan.getValue();
	}
	@Override public String getTwistChannelModifier() {
		return (String) spin_twistModel.getValue();
	}
	@Override public String getTwistDirection() {
		return (String) spin_twistDir.getValue();
	}
	@Override public void setupPokeTrigger(DummyBox db, DummyTrigger dt) {
		myPokeBox = db;
		myPokeTrigger = dt;
	}	
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel5 = new javax.swing.JLabel();
        spin_twistDir = new javax.swing.JSpinner();
        jLabel4 = new javax.swing.JLabel();
        spin_twistModel = new javax.swing.JSpinner();
        jLabel3 = new javax.swing.JLabel();
        spin_twistChan = new javax.swing.JSpinner();
        pokeButton = new javax.swing.JButton();

        setRequestFocusEnabled(false);

        jLabel5.setFont(new java.awt.Font("Tahoma", 0, 8));
        jLabel5.setText("twist dir");

        jLabel4.setFont(new java.awt.Font("Tahoma", 0, 8));
        jLabel4.setText("twist model");

        jLabel3.setFont(new java.awt.Font("Tahoma", 0, 8));
        jLabel3.setText("twist chan");

        pokeButton.setText("poke");
        pokeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pokeButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 60, Short.MAX_VALUE)
                    .addComponent(spin_twistChan, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4)
                    .addComponent(spin_twistModel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5)
                    .addComponent(spin_twistDir, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pokeButton))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(spin_twistChan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(spin_twistModel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel5)
                .addGap(18, 18, 18)
                .addComponent(spin_twistDir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pokeButton)
                .addContainerGap(376, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

	private void pokeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pokeButtonActionPerformed
		if (myPokeTrigger != null) { 
			myPokeTrigger.fire(myPokeBox);
		}
	}//GEN-LAST:event_pokeButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JButton pokeButton;
    private javax.swing.JSpinner spin_twistChan;
    private javax.swing.JSpinner spin_twistDir;
    private javax.swing.JSpinner spin_twistModel;
    // End of variables declaration//GEN-END:variables


}
