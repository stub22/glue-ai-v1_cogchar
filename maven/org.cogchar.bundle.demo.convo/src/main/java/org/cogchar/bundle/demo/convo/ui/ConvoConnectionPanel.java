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

/*
 * ConvoConnectionPanel.java
 *
 * Created on Apr 26, 2012, 3:04:14 PM
 */
package org.cogchar.bundle.demo.convo.ui;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;
import org.cogchar.bundle.demo.convo.*;
import org.jflux.api.core.node.ConsumerNode;
import org.jflux.api.core.node.DefaultProcessorNode;
import org.jflux.api.core.node.ProcessorNode;
import org.jflux.api.core.node.ProducerNode;
import org.jflux.api.core.node.chain.NodeChain;
import org.jflux.api.core.node.chain.NodeChainBuilder;
import org.jflux.impl.messaging.JMSAvroUtils;
import org.jflux.impl.messaging.jms.MessageHeaderAdapter;
import org.robokind.api.speech.SpeechRequest;
import org.robokind.api.speechrec.SpeechRecEvent;
import org.robokind.api.speechrec.SpeechRecEventList;
import org.robokind.avrogen.speech.SpeechRequestRecord;
import org.robokind.avrogen.speechrec.SpeechRecEventListRecord;
import org.robokind.impl.speech.PortableSpeechRequest;
import org.robokind.impl.speechrec.PortableSpeechRecEventList;

/**
 *
 * @author Matthew Stevenson <www.robokind.org>
 */
public class ConvoConnectionPanel extends javax.swing.JPanel {
    private final static Logger theLogger = Logger.getLogger(ConvoConnectionPanel.class.getName());
    ProducerNode<SpeechRecEventList> mySpeechProducer;
    ProcessorNode<String,ConvoResponse> myConvoProc;
    ConsumerNode<SpeechRequest> myResponseSender;
    private NodeChain myChain;
    
    /** Creates new form ConvoConnectionPanel */
    public ConvoConnectionPanel() {
        initComponents();
        pnlRecConnect.setDestination(
                "speechRec.Event; {create:always, node:{type:queue}}");
        pnlTTSConnect.setDestination(
                "speech.Request; {create:always, node:{type:queue}}");
    }
    
    public boolean connect(){
        if(!pnlRecConnect.connect()){
            return false;
        }if(!pnlTTSConnect.connect()){
            pnlRecConnect.disconnect();
            return false;
        }
        myChain = connect(
                pnlRecConnect.getSession(), pnlRecConnect.getDestination(), 
                pnlTTSConnect.getSession(), pnlTTSConnect.getDestination(), 
                txtCogbot.getText());
        if(myChain == null || !myChain.start()){
            disconnect();
            return false;
        }
        txtCogbot.setEnabled(false);
        return true;
    }
    
    public ProcessorNode<String,ConvoResponse> getConvoProc(){
        return myConvoProc;
    }
    
    public NodeChain connect(Session recSession, Destination recDest,
            Session ttsSession, Destination ttsDest, String cogbotUrl) {
        mySpeechProducer = buildSpeechRecChain(recSession, recDest);
        myResponseSender = buildTTSNodeChain(ttsSession, ttsDest);
        myConvoProc = buildConvoChain(cogbotUrl);
        if(mySpeechProducer == null || myResponseSender == null){
            return null;
        }
        
        return NodeChainBuilder.build(mySpeechProducer)
            .attach(SpeechRecEvent.class, new SpeechRecFilter()) 
            .attach(String.class, new SpeechRecStringFilter())
            .attach(String.class, new ConversationInputFilter())
            .attach(myConvoProc)
            .attach(ConvoResponse.class, new ConvoResponseFilter())
            .attach(String.class, new ConvoResponseStringAdapter())
            .attach(SpeechRequest.class, new SpeechFormatter("source", "dest"))
            .attach(myResponseSender);
    }
    
    private ProducerNode<SpeechRecEventList> buildSpeechRecChain(
            Session session, Destination dest){
        try{
            return JMSAvroUtils.buildEventReceiverChain(
                    SpeechRecEventList.class, 
                    SpeechRecEventListRecord.class, 
                    SpeechRecEventListRecord.SCHEMA$, 
                    new PortableSpeechRecEventList.RecordMessageAdapter(), 
                    session, dest);
        }catch(JMSException ex){
            theLogger.log(Level.WARNING,"Error connecting to Speech Rec.",ex);
            return null;
        }
    }
    
    private ConsumerNode<SpeechRequest> buildTTSNodeChain(
            Session session, Destination dest){
        try{
            return JMSAvroUtils.buildEventSenderChain(
                    SpeechRequest.class, 
                    SpeechRequestRecord.class, 
                    SpeechRequestRecord.SCHEMA$, 
                    new PortableSpeechRequest.MessageRecordAdapter(), 
                    session, dest, 
                    new MessageHeaderAdapter("application/speechRequest"));
        }catch(JMSException ex){
            theLogger.log(Level.WARNING,"Error connecting to TTS.",ex);
            return null;
        }
    }
    
    private ProcessorNode<String,ConvoResponse> buildConvoChain(String cogbotUrl){
        return new DefaultProcessorNode(
                String.class, ConvoResponse.class, 
                new CogbotProcessor(cogbotUrl));
    }
    
    public void disconnect(){
        if(myChain != null){
            myChain.stop();
            myChain = null;
        }if(myConvoProc != null){
            myConvoProc = null;
        }
        pnlRecConnect.disconnect();
        pnlTTSConnect.disconnect();
        txtCogbot.setEnabled(true);
    }
    
    public long getPollInterval(){
        String str = txtCogbotPollInterval.getText();
        try{
            Long interval = Long.parseLong(str);
            interval = Math.max(interval, 1);
            return interval;
        }catch(NumberFormatException ex){
            return 1000;
        }
    }
    
    public void setPollIntervalEnabled(boolean val){
        txtCogbotPollInterval.setEnabled(val);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlSpeechRec = new javax.swing.JPanel();
        pnlRecConnect = new org.cogchar.bundle.demo.convo.ui.MessagingConnectPanel();
        pnlCogbotConnection = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        txtCogbot = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        txtCogbotPollInterval = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        pnlTTS = new javax.swing.JPanel();
        pnlTTSConnect = new org.cogchar.bundle.demo.convo.ui.MessagingConnectPanel();

        pnlSpeechRec.setBorder(javax.swing.BorderFactory.createTitledBorder("Speech Rec Connection"));

        javax.swing.GroupLayout pnlSpeechRecLayout = new javax.swing.GroupLayout(pnlSpeechRec);
        pnlSpeechRec.setLayout(pnlSpeechRecLayout);
        pnlSpeechRecLayout.setHorizontalGroup(
            pnlSpeechRecLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlRecConnect, javax.swing.GroupLayout.DEFAULT_SIZE, 536, Short.MAX_VALUE)
        );
        pnlSpeechRecLayout.setVerticalGroup(
            pnlSpeechRecLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlRecConnect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pnlCogbotConnection.setBorder(javax.swing.BorderFactory.createTitledBorder("Cogbot Connection"));

        jLabel1.setText("Cogbot Address");

        txtCogbot.setText("127.0.0.1");

        jLabel2.setText("Poll Interval");

        txtCogbotPollInterval.setText("1000");

        jLabel3.setText("(millisec)");

        javax.swing.GroupLayout pnlCogbotConnectionLayout = new javax.swing.GroupLayout(pnlCogbotConnection);
        pnlCogbotConnection.setLayout(pnlCogbotConnectionLayout);
        pnlCogbotConnectionLayout.setHorizontalGroup(
            pnlCogbotConnectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCogbotConnectionLayout.createSequentialGroup()
                .addGroup(pnlCogbotConnectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel2)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlCogbotConnectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlCogbotConnectionLayout.createSequentialGroup()
                        .addComponent(txtCogbotPollInterval, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel3)
                        .addGap(175, 175, 175))
                    .addComponent(txtCogbot, javax.swing.GroupLayout.DEFAULT_SIZE, 413, Short.MAX_VALUE)))
        );
        pnlCogbotConnectionLayout.setVerticalGroup(
            pnlCogbotConnectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCogbotConnectionLayout.createSequentialGroup()
                .addGroup(pnlCogbotConnectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtCogbot, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlCogbotConnectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtCogbotPollInterval, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)))
        );

        pnlTTS.setBorder(javax.swing.BorderFactory.createTitledBorder("Text to Speech Connection"));

        javax.swing.GroupLayout pnlTTSLayout = new javax.swing.GroupLayout(pnlTTS);
        pnlTTS.setLayout(pnlTTSLayout);
        pnlTTSLayout.setHorizontalGroup(
            pnlTTSLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlTTSConnect, javax.swing.GroupLayout.DEFAULT_SIZE, 536, Short.MAX_VALUE)
        );
        pnlTTSLayout.setVerticalGroup(
            pnlTTSLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlTTSConnect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlSpeechRec, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(pnlTTS, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(pnlCogbotConnection, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(pnlSpeechRec, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlTTS, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlCogbotConnection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel pnlCogbotConnection;
    private org.cogchar.bundle.demo.convo.ui.MessagingConnectPanel pnlRecConnect;
    private javax.swing.JPanel pnlSpeechRec;
    private javax.swing.JPanel pnlTTS;
    private org.cogchar.bundle.demo.convo.ui.MessagingConnectPanel pnlTTSConnect;
    private javax.swing.JTextField txtCogbot;
    private javax.swing.JTextField txtCogbotPollInterval;
    // End of variables declaration//GEN-END:variables
}
