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

package org.cogchar.bundle.demo.convo;

import org.jflux.impl.transport.jms.MessageHeaderAdapter;
import org.jflux.api.encode.EncodeRequest;
import org.mechio.impl.speech.SpeechRequestRecord;
import org.jflux.api.core.Adapter;
import org.jflux.api.core.Listener;
import org.jflux.api.core.Source;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;
import org.jflux.api.core.node.ConsumerNode;
import org.jflux.api.core.node.ProducerNode;
import org.jflux.api.core.node.chain.NodeChain;
import org.jflux.api.core.node.chain.NodeChainBuilder;
import org.jflux.impl.messaging.JMSAvroUtils;
import org.mechio.api.speech.SpeechRequest;
import org.mechio.api.speechrec.SpeechRecEvent;
import org.mechio.api.speechrec.SpeechRecEventList;
import org.mechio.impl.speechrec.SpeechRecEventListRecord;

import static org.cogchar.bundle.demo.convo.osgi.ConvoConfigUtils.*;
/**
 * @author Stu B. <www.texpedient.com>
 */

public class ConvoFacade {
   private final static Logger theLogger = Logger.getLogger(ConvoFacade.class.getName());
    ProducerNode<SpeechRecEventList<SpeechRecEvent>> mySpeechProducer;
    Adapter<String,ConvoResponse> myConvoProc;
    ConsumerNode<SpeechRequest> myResponseSender;
    private NodeChain myChain;
    private Source<String> myCogbotIpSource;
    private Listener<String> myCogbotIpSetter;
    private Source<Long> myCogbotPollIntervalSource;
    private Listener<Long> myCogbotPollIntervalSetter;
    /*
    public ConvoFacade() {

        pnlRecConnect.setDestination(
                getSource(String.class, CONF_SPREC_DESTINATION), 
                getSetter(String.class, CONF_SPREC_DESTINATION));
        pnlRecConnect.setBrokerAddress(
                getSource(String.class, CONF_SPREC_BROKER_IP), 
                getSetter(String.class, CONF_SPREC_BROKER_IP),
                getSource(String.class, CONF_SPREC_BROKER_PORT),
                getSource(String.class, CONF_SPREC_BROKER_USERNAME),
                getSource(String.class, CONF_SPREC_BROKER_PASSWORD),
                getSource(String.class, CONF_SPREC_BROKER_CLIENT_NAME),
                getSource(String.class, CONF_SPREC_BROKER_VIRTUAL_HOST));
        pnlTTSConnect.setDestination(
                getSource(String.class, CONF_TTS_DESTINATION), 
                getSetter(String.class, CONF_TTS_DESTINATION));
        pnlTTSConnect.setBrokerAddress(
                getSource(String.class, CONF_TTS_BROKER_IP), 
                getSetter(String.class, CONF_TTS_BROKER_IP),
                getSource(String.class, CONF_TTS_BROKER_PORT),
                getSource(String.class, CONF_TTS_BROKER_USERNAME),
                getSource(String.class, CONF_TTS_BROKER_PASSWORD),
                getSource(String.class, CONF_TTS_BROKER_CLIENT_NAME),
                getSource(String.class, CONF_TTS_BROKER_VIRTUAL_HOST));
        
        myCogbotIpSource = getSource(String.class, CONF_COGBOT_IP);
        myCogbotIpSetter = getSetter(String.class, CONF_COGBOT_IP);
        myCogbotPollIntervalSource = 
                getSource(Long.class, CONF_COGBOT_POLL_INTERVAL);
        myCogbotPollIntervalSetter = 
                getSetter(Long.class, CONF_COGBOT_POLL_INTERVAL);
        
        txtCogbot.setText(myCogbotIpSource.getValue());
        txtCogbotPollInterval.setText(
                myCogbotPollIntervalSource.getValue().toString());
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
    
    public Adapter<String,ConvoResponse> getConvoProc(){
        return myConvoProc;
    }
    
    public NodeChain connect(Session recSession, Destination recDest,
            Session ttsSession, Destination ttsDest, String cogbotUrl) {
        mySpeechProducer = buildSpeechRecChain(recSession, recDest);
        myResponseSender = buildTTSNodeChain(ttsSession, ttsDest);
        myConvoProc = new CogbotProcessor(cogbotUrl);
        if(mySpeechProducer == null || myResponseSender == null){
            return null;
        }
        
        return NodeChainBuilder.build(mySpeechProducer)
            .attach(new SpeechRecFilter()) 
            .attach(new SpeechRecStringFilter())
            .attach(new ConversationInputFilter())
            .attach(myConvoProc)
            .attach(new ConvoResponseFilter())
            .attach(new ConvoResponseStringAdapter())
            .attach(new SpeechFormatter("source", "dest"))
            .attach(myResponseSender);
    }
    
    private ProducerNode<SpeechRecEventList<SpeechRecEvent>> buildSpeechRecChain(
            Session session, Destination dest){
        try{
            return JMSAvroUtils.buildEventReceiverChain(
                    SpeechRecEventListRecord.class, 
                    SpeechRecEventListRecord.SCHEMA$, 
                    new EmptyAdapter(), 
                    session, dest);
        }catch(JMSException ex){
            theLogger.log(Level.WARNING,"Error connecting to Speech Rec.",ex);
            return null;
        }
    }
    
    private ConsumerNode<SpeechRequest> buildTTSNodeChain(
            Session session, Destination dest){
        try{
            return NodeChainBuilder.build(
                    EncodeRequest.factory(SpeechRequest.class, new JMSAvroUtils.ByteOutputStreamFactory()))
                .getConsumerChain(JMSAvroUtils.buildEventSenderChain(
                    SpeechRequestRecord.class, 
                    SpeechRequestRecord.SCHEMA$, 
                    new EmptyAdapter(), 
                    session, dest, 
                    new MessageHeaderAdapter("application/speechRequest")));
        }catch(Exception ex){
            theLogger.log(Level.WARNING,"Error connecting to TTS.",ex);
            return null;
        }
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
    
    public void updatetPollInterval(){
        String str = txtCogbotPollInterval.getText();
        try{
            Long interval = Long.parseLong(str);
            interval = Math.max(interval, 1);
            myCogbotPollIntervalSetter.handleEvent(interval);
        }catch(NumberFormatException ex){
            theLogger.log(Level.WARNING, 
                    "Invalid Poll Interval, not a number: " + str, ex);
        }
    }
    
    public void setPollIntervalEnabled(boolean val){
        txtCogbotPollInterval.setEnabled(val);
    }

	*/ 
}
