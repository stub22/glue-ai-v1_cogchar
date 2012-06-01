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
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultCaret;
import static org.cogchar.bundle.demo.dictation.osgi.DictationConfigUtils.*;

/**
 *
 * @author Matthew Stevenson <www.robokind.org>
 */
public class DictationPanel extends JPanel {
    private final static Logger theLogger = Logger.getLogger(DictationPanel.class.getName());
    private final static int CONVO_TAB = 0;
    private final static int CONNECTION_TAB = 1;
    
    private DefaultDictationGrabber myImpl;
    private Refocuser myRefocuser;

    /** Creates new form DictationPanel */
    public DictationPanel() {		
        initComponents();
        myImpl = new DefaultDictationGrabber(this);
        initCaptureTextBox();
        myRefocuser = new Refocuser(txtInput);
        myRefocuser.ignoreComponent(chkFocus);
        jTabbedPane1.setSelectedIndex(CONNECTION_TAB);
        txtBrokerAddress.setText(getValue(String.class, CONF_BROKER_IP));
        txtSpeechRecDest.setText(getValue(String.class, CONF_DESTINATION));
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

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtMatches = new javax.swing.JTextArea();
        txtInput = new javax.swing.JTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtConcatInput = new javax.swing.JTextArea();
        jPanel5 = new javax.swing.JPanel();
        chkSendOnSilence = new javax.swing.JCheckBox();
        chkFocus = new javax.swing.JCheckBox();
        jPanel4 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        txtBrokerAddress = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        txtSpeechRecDest = new javax.swing.JTextField();
        tglConnect = new javax.swing.JToggleButton();

        setName("Form"); // NOI18N

        jTabbedPane1.setName("jTabbedPane1"); // NOI18N

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/cogchar/bundle/demo/dictation/ui/Bundle"); // NOI18N
        jPanel1.setName(bundle.getString("DictationPanel.jPanel1.name")); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        txtMatches.setColumns(20);
        txtMatches.setEditable(false);
        txtMatches.setRows(5);
        txtMatches.setName("txtMatches"); // NOI18N
        jScrollPane1.setViewportView(txtMatches);
        DefaultCaret caret = (DefaultCaret) txtMatches.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        txtInput.setName("txtInput"); // NOI18N
        txtInput.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtInput_keyPressed(evt);
            }
        });

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        txtConcatInput.setColumns(20);
        txtConcatInput.setEditable(false);
        txtConcatInput.setRows(4);
        txtConcatInput.setTabSize(4);
        txtConcatInput.setName("txtConcatInput"); // NOI18N
        jScrollPane2.setViewportView(txtConcatInput);

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder("Capture Options"));
        jPanel5.setName("jPanel5"); // NOI18N

        chkSendOnSilence.setText("Send on Silence"); // NOI18N
        chkSendOnSilence.setName("chkSendOnSilence"); // NOI18N
        chkSendOnSilence.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkSendOnSilenceActionPerformed(evt);
            }
        });

        chkFocus.setText("Lock Focus"); // NOI18N
        chkFocus.setName("chkFocus"); // NOI18N
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

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtInput)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 438, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(txtInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1)
                .addContainerGap())
        );

        jTabbedPane1.addTab(bundle.getString("DictationPanel.jPanel1.TabConstraints.tabTitle"), jPanel1); // NOI18N

        jPanel4.setName("jPanel4"); // NOI18N

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("DictationPanel.jPanel2.border.title"))); // NOI18N
        jPanel2.setName("jPanel2"); // NOI18N

        jLabel1.setText("Broker Address"); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        txtBrokerAddress.setText("127.0.0.1"); // NOI18N
        txtBrokerAddress.setName("txtBrokerAddress"); // NOI18N

        jLabel2.setText("Event Queue"); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        txtSpeechRecDest.setText("speechRec.Event; {create: always, node: {type: queue}}"); // NOI18N
        txtSpeechRecDest.setName("txtSpeechRecDest"); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtSpeechRecDest, javax.swing.GroupLayout.DEFAULT_SIZE, 460, Short.MAX_VALUE)
                    .addComponent(txtBrokerAddress)))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtBrokerAddress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtSpeechRecDest, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(136, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Connection", jPanel4);

        tglConnect.setText("Connect"); // NOI18N
        tglConnect.setMaximumSize(new java.awt.Dimension(104, 30));
        tglConnect.setMinimumSize(new java.awt.Dimension(104, 30));
        tglConnect.setName("tglConnect"); // NOI18N
        tglConnect.setPreferredSize(new java.awt.Dimension(104, 30));
        tglConnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tglConnectActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(tglConnect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jTabbedPane1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tglConnect, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
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

    private void tglConnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tglConnectActionPerformed
        if(tglConnect.isSelected()){
            String addr = txtBrokerAddress.getText();
            String dest = txtSpeechRecDest.getText();
            setValue(String.class, CONF_BROKER_IP, addr);
            setValue(String.class, CONF_DESTINATION, dest);
            if(myImpl.connect()){
                txtBrokerAddress.setEnabled(false);
                txtSpeechRecDest.setEnabled(false);
                jTabbedPane1.setSelectedIndex(CONVO_TAB);
            }else{
                myImpl.disconnect();
                tglConnect.setSelected(false);
                jTabbedPane1.setSelectedIndex(CONNECTION_TAB);
                txtBrokerAddress.setEnabled(true);
                txtSpeechRecDest.setEnabled(true);
            }
        }else{
            myImpl.disconnect();
            jTabbedPane1.setSelectedIndex(CONNECTION_TAB);
            txtBrokerAddress.setEnabled(true);
            txtSpeechRecDest.setEnabled(true);
        }
    }//GEN-LAST:event_tglConnectActionPerformed

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
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JToggleButton tglConnect;
    private javax.swing.JTextField txtBrokerAddress;
    protected javax.swing.JTextArea txtConcatInput;
    protected javax.swing.JTextField txtInput;
    protected javax.swing.JTextArea txtMatches;
    private javax.swing.JTextField txtSpeechRecDest;
    // End of variables declaration//GEN-END:variables
}
