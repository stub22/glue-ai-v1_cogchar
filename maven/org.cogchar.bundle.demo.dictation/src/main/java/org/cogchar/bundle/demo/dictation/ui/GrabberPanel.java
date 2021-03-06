/*
 * Copyright 2012 by The Cogchar Project (www.cogchar.org).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cogchar.bundle.demo.dictation.ui;

import java.awt.event.KeyEvent;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultCaret;
import org.jflux.api.core.Listener;
import org.jflux.api.core.Source;

/**
 *
 * @author Matthew Stevenson <www.cogchar.org>
 */
public class GrabberPanel extends javax.swing.JPanel {
    private DefaultDictationGrabber myImpl;
    private Refocuser myRefocuser;
    
    /** Creates new form GrabberPanel */
    public GrabberPanel() {
        initComponents();
        myImpl = new DefaultDictationGrabber(
                new Source<String>() {
                    @Override public String getValue() {
                        return txtConcatInput.getText();
                    }
                }, new Source<String>() {
                    @Override public String getValue() {
                        return txtMatches.getText();
                    }
                });
        initCaptureTextBox();
        myRefocuser = new Refocuser(txtInput);
        myRefocuser.ignoreComponent(chkFocus);
        myImpl.getDictationLogNotifier().addListener(
                new Listener<String>() {
                    @Override public void handleEvent(String input) {
                        logSpeech(input);
                    }
                });
        myImpl.getConcatInputNotifier().addListener(
                new Listener<String>() {
                    @Override public void handleEvent(String input) {
                        txtConcatInput.setText(input);
                    }
                });
    }
    
    public DefaultDictationGrabber getDictGrabImpl(){
        return myImpl;
    }
    
    private void initCaptureTextBox(){
        txtInput.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override public void run() {
                        txtConcatInput.setText(
                                txtConcatInput.getText() + txtInput.getText());
                        txtInput.setText("");
                    }
                });
            }
            @Override public void removeUpdate(DocumentEvent e) {}
            @Override public void changedUpdate(DocumentEvent e) {}
        });
    }
    
    protected void logSpeech(String speech){
        txtMatches.append(speech);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                jScrollPane1.getVerticalScrollBar().setValue(
                        jScrollPane1.getVerticalScrollBar().getMaximum());
            }
        });
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        chkFocus.setEnabled(enabled);
        chkSendOnSilence.setEnabled(enabled);
        txtInput.setEnabled(enabled);
        txtConcatInput.setEnabled(enabled);
        txtMatches.setEnabled(enabled);
        jPanel5.setEnabled(enabled);
        jScrollPane1.setEnabled(enabled);
        jScrollPane2.setEnabled(enabled);
    }
    
    

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        txtInput = new javax.swing.JTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtConcatInput = new javax.swing.JTextArea();
        jPanel5 = new javax.swing.JPanel();
        chkSendOnSilence = new javax.swing.JCheckBox();
        chkFocus = new javax.swing.JCheckBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtMatches = new javax.swing.JTextArea();

        txtInput.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtInput_keyPressed(evt);
            }
        });

        txtConcatInput.setColumns(20);
        txtConcatInput.setEditable(false);
        txtConcatInput.setRows(4);
        txtConcatInput.setTabSize(4);
        jScrollPane2.setViewportView(txtConcatInput);

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder("Capture Options"));

        chkSendOnSilence.setText("Send on Silence"); // NOI18N
        chkSendOnSilence.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkSendOnSilenceActionPerformed(evt);
            }
        });

        chkFocus.setText("Lock Focus"); // NOI18N
        chkFocus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkFocusActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(chkSendOnSilence)
            .addComponent(chkFocus)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(chkSendOnSilence)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(chkFocus))
        );

        txtMatches.setColumns(20);
        txtMatches.setEditable(false);
        txtMatches.setRows(5);
        jScrollPane1.setViewportView(txtMatches);
        DefaultCaret caret = (DefaultCaret) txtMatches.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtInput, javax.swing.GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 223, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(txtInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 155, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

private void txtInput_keyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtInput_keyPressed
        if(evt.getKeyCode() == KeyEvent.VK_ENTER){
            if(myImpl.collectDictation()){
                myImpl.handleDictation();
            }
        }else if(evt.getKeyChar() == KeyEvent.VK_BACK_SPACE){
            String txt = txtConcatInput.getText();
            if(!txt.isEmpty()){
                txtConcatInput.setText(txt.substring(0, txt.length()-1));
                myImpl.collectDictation();
            }
            evt.consume();
        }
}//GEN-LAST:event_txtInput_keyPressed

private void chkSendOnSilenceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkSendOnSilenceActionPerformed
        boolean sel = chkSendOnSilence.isSelected();
        boolean val = myImpl.setAutoSend(sel);
        chkSendOnSilence.setSelected(val);
        if(val && sel){
            chkFocus.setSelected(true);
            myRefocuser.start();
        }
}//GEN-LAST:event_chkSendOnSilenceActionPerformed

private void chkFocusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkFocusActionPerformed
        if(chkFocus.isSelected()){
            myRefocuser.start();
        }else{
            myRefocuser.stop();
        }
}//GEN-LAST:event_chkFocusActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox chkFocus;
    private javax.swing.JCheckBox chkSendOnSilence;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    protected javax.swing.JTextArea txtConcatInput;
    protected javax.swing.JTextField txtInput;
    protected javax.swing.JTextArea txtMatches;
    // End of variables declaration//GEN-END:variables
}
