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
package org.cogchar.bundle.demo.convo.ui;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;
import org.cogchar.bundle.demo.convo.CogbotProcessor;
import org.cogchar.bundle.demo.convo.ConversationInputFilter;
import org.cogchar.bundle.demo.convo.ConvoResponse;
import org.cogchar.bundle.demo.convo.ConvoResponseFilter;
import org.cogchar.bundle.demo.convo.ConvoResponseStringAdapter;
import org.cogchar.bundle.demo.convo.PannousProcessor;
import org.cogchar.bundle.demo.convo.SpeechFormatter;
import org.cogchar.bundle.demo.convo.SpeechRecFilter;
import org.cogchar.bundle.demo.convo.SpeechRecStringFilter;
import org.jflux.api.core.node.ConsumerNode;
import org.jflux.api.core.node.DefaultProcessorNode;
import org.jflux.api.core.node.ProcessorNode;
import org.jflux.api.core.node.ProducerNode;
import org.jflux.api.core.node.chain.NodeChain;
import org.jflux.api.core.node.chain.NodeChainBuilder;
import org.jflux.api.core.util.EmptyAdapter;
import org.jflux.api.encode.EncodeRequest;
import org.jflux.api.messaging.rk.services.ServiceCommand;
import org.jflux.impl.messaging.JMSAvroUtils;
import org.jflux.impl.messaging.rk.ServiceCommandRecord;
import org.jflux.impl.messaging.services.PortableServiceCommand;
import org.jflux.impl.transport.jms.MessageHeaderAdapter;
import org.mechio.api.speech.SpeechRequest;
import org.mechio.api.speechrec.SpeechRecEvent;
import org.mechio.api.speechrec.SpeechRecEventList;
import org.mechio.impl.speech.SpeechRequestRecord;
import org.mechio.impl.speechrec.SpeechRecEventListRecord;

/**
 *
 * @author matt
 */


public class ConvoConnector {
    private final static Logger theLogger = Logger.getLogger(ConvoConnectionPanel.class.getName());
    ProducerNode<SpeechRecEventList<SpeechRecEvent>> mySpeechProducer;
    ProcessorNode<String,ConvoResponse> myConvoProc;
    ConsumerNode<ServiceCommand> myTTSCommnadSender;
    ConsumerNode<ServiceCommand> myAnimPromptSender;
    ConsumerNode<SpeechRequest> myResponseSender;
    
    private NodeChain myChain;
    
    private MessagingConnectImpl myTTSConnector;
    private MessagingConnectImpl mySpeechRecConnector;
    private Destination myAnimDest;
    private Destination myTTSCmdDest;
    
    public ConvoConnector(
            MessagingConnectImpl ttsConnector,
            MessagingConnectImpl speechRecConnector,
            Destination animDest, Destination ttsCmdDest){
        myTTSConnector = ttsConnector;
        mySpeechRecConnector = speechRecConnector;
        myAnimDest = animDest;
        myTTSCmdDest = ttsCmdDest;
    }
    
    public ProcessorNode<String,ConvoResponse> getConvoProc(){
        return myConvoProc;
    }
    
    public boolean connectCogbot(String cogbotUrl) {
        CogbotProcessor proc = new CogbotProcessor(cogbotUrl);
        myConvoProc = new DefaultProcessorNode<String, ConvoResponse>(proc);
        proc.setInputListener(myConvoProc.getListener());
        return connect();
    }
    
    public boolean connectPannous() {
        myConvoProc = 
                new DefaultProcessorNode<String, ConvoResponse>(
                        new PannousProcessor("", getPannousTimeout()));
        return connect();
    }
    
    private boolean connect(){
        if(!myTTSConnector.connect()){
            return false;
        }if(!mySpeechRecConnector.connect()){
            myTTSConnector.disconnect();
            return false;
        }
        myChain = connect(
                mySpeechRecConnector.getSession(), 
                mySpeechRecConnector.getDestination(), 
                myTTSConnector.getSession(), 
                myTTSConnector.getDestination(), 
                myTTSCmdDest, myAnimDest);
        if(myChain == null || !myChain.start()){
            disconnect();
            return false;
        }
        return true;
    }
    
//    private void ignore(){
//        try{
//            myTTSCommnadSender = buildServiceCommandNodeChain(ttsSession, 
//                    new AMQQueue("speechCommand; {create: always, node: {type: queue}}"));
//            myAnimPromptSender = buildServiceCommandNodeChain(ttsSession, 
//                    new AMQTopic("animPrompt; {create: always, node: {type: topic}}"));
//        }catch(URISyntaxException ex){}        
//    }
    
    private NodeChain connect(Session recSession, Destination recDest,
            Session ttsSession, Destination ttsDest, Destination ttsCmdDest, 
            Destination animPromptDest) {

        mySpeechProducer = buildSpeechRecChain(recSession, recDest);
        myResponseSender = buildTTSNodeChain(ttsSession, ttsDest);
        myTTSCommnadSender = buildServiceCommandNodeChain(ttsSession, ttsCmdDest);
        myAnimPromptSender = buildServiceCommandNodeChain(ttsSession, animPromptDest);
        if(mySpeechProducer == null || myResponseSender == null 
                || myTTSCommnadSender == null || myAnimPromptSender == null
                || myConvoProc == null){
            return null;
        }
        myTTSCommnadSender.start();
        myAnimPromptSender.start();
        
        return NodeChainBuilder.build(mySpeechProducer)
            .attach(new SpeechRecFilter()) 
            .attach(new SpeechRecStringFilter())
            .attach(new ConversationInputFilter())
            .attach(myConvoProc)
            .attach(new ConvoResponseFilter(
                    myTTSCommnadSender.getListener(), 
                    new PortableServiceCommand.Factory(),
                    myAnimPromptSender.getListener()))
            .attach(new ConvoResponseStringAdapter())
            .attach(new SpeechFormatter("source", "dest"))
            .attach(myResponseSender);
    }
    
    private int getPannousTimeout(){
//        String intStr = txtPannousTimeout.getText();
//        try{
//            return Integer.parseInt(intStr);
//        }catch(NumberFormatException ex){
            return 10000;
//        }
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
    
    private ConsumerNode<ServiceCommand> buildServiceCommandNodeChain(
            Session session, Destination dest){
        try{
            return NodeChainBuilder.build(
                    EncodeRequest.factory(ServiceCommand.class, new JMSAvroUtils.ByteOutputStreamFactory()))
                .getConsumerChain(JMSAvroUtils.buildEventSenderChain(
                    ServiceCommandRecord.class, 
                    ServiceCommandRecord.SCHEMA$, 
                    new EmptyAdapter(), 
                    session, dest, 
                    new MessageHeaderAdapter("application/service-command")));
        }catch(JMSException ex){
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
        mySpeechRecConnector.disconnect();
        myTTSConnector.disconnect();
    }
    
    public void updatetPollInterval(){
//        String str = txtCogbotPollInterval.getText();
//        try{
//            Long interval = Long.parseLong(str);
//            interval = Math.max(interval, 1);
//            myCogbotPollIntervalSetter.handleEvent(interval);
//        }catch(NumberFormatException ex){
//            theLogger.log(Level.WARNING, 
//                    "Invalid Poll Interval, not a number: " + str, ex);
//        }
    }
    
    public void setPollIntervalEnabled(boolean val){
//        txtCogbotPollInterval.setEnabled(val);
    }
}
