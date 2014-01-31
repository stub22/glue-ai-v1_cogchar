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

import org.apache.qpid.client.AMQSession;
import org.jflux.api.core.Source;
import org.jflux.api.core.Notifier;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;
import org.apache.qpid.client.AMQConnection;
import org.cogchar.bundle.demo.dictation.sound.SoundDetector;
import org.jflux.api.common.rk.utils.TimeUtils;
import org.jflux.api.core.node.ConsumerNode;
import org.jflux.api.core.node.chain.NodeChainBuilder;
import org.jflux.api.core.Adapter;
import org.jflux.api.core.util.DefaultNotifier;
import org.jflux.api.core.util.EmptyAdapter;
import org.jflux.impl.messaging.JMSAvroUtils;
import org.jflux.impl.messaging.rk.utils.ConnectionManager;
import org.jflux.impl.transport.jms.MessageHeaderAdapter;
import org.mechio.api.speechrec.SpeechRecEventList;
import org.mechio.impl.speechrec.SpeechRecEventListRecord;
import org.mechio.impl.speechrec.SpeechRecEventRecord;
import static org.cogchar.bundle.demo.dictation.osgi.DictationConfigUtils.*;

/**
 *
 * @author Matthew Stevenson <www.cogchar.org>
 */
public class DefaultDictationGrabber implements DictationGrabber{
    private final static Logger theLogger = Logger.getLogger(DefaultDictationGrabber.class.getName());
    private static DateFormat theDateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);
    private SoundDetector mySoundDetector;
    private ConsumerNode<String> mySpeechRecSender;
    private Connection myConnection;
    private Session mySession;
    private String myCapturedDictation;
    private Notifier<String> myDictationLogger;
    private Notifier<String> myConcatInputNotifier;
    private Source<String> myConcatInputSource;
    private Source<String> myDictationInputSource;
    
    public DefaultDictationGrabber(
            Source<String> concatInput, Source<String> dictationInput){
        if(concatInput == null || dictationInput == null){
            throw new NullPointerException();
        }
        mySoundDetector = new SoundDetector(this);
        myCapturedDictation = "";
        myConcatInputSource = concatInput;
        myDictationInputSource = dictationInput;
        myDictationLogger = new DefaultNotifier<String>();
        myConcatInputNotifier = new DefaultNotifier<String>();
    }
    
    public Notifier<String> getDictationLogNotifier(){
        return myDictationLogger;
    }
    
    public Notifier<String> getConcatInputNotifier(){
        return myConcatInputNotifier;
    }

    @Override
    public synchronized void handleDictation(){
        collectDictation();
        if(mySpeechRecSender == null){
            myCapturedDictation = "";
            myConcatInputNotifier.notifyListeners("");
            return;
        }else if(myCapturedDictation == null || myCapturedDictation.isEmpty()){
            return;
        }
        mySpeechRecSender.getListener().handleEvent(myCapturedDictation);
        myDictationLogger.notifyListeners(formatSpeech(myCapturedDictation));
        myCapturedDictation = "";
        myConcatInputNotifier.notifyListeners("");
    }
    
    private String formatSpeech(String str){
        str = "[" + theDateFormat.format(new Date()) + "]: " + str;
        String s = myDictationInputSource.getValue();
        if(s != null && !s.isEmpty()){
            str = "\n\n" + str;
        }
        return str;
    }    

    @Override
    public synchronized boolean collectDictation() {
        String input = myConcatInputSource.getValue();
        if(input == null || input.isEmpty() 
                || input.equals(myCapturedDictation)){
            return false;
        }
        myCapturedDictation = input;
        return true;
    }
    
    public boolean connect(){
        if(myConnection != null || mySession != null){
            return false;
        }
        try{
            mySpeechRecSender = buildSpeechSenderNodeChain();
            if(mySpeechRecSender == null){
                return false;
            }
            return mySpeechRecSender.start();
        }catch(JMSException ex){
            theLogger.log(Level.WARNING, "Error building speech sender.", ex);
            return false;
        }
    }
    
    public void disconnect(){
        setAutoSend(false);
        if(mySpeechRecSender != null){
            mySpeechRecSender.stop();
            mySpeechRecSender = null;
        }
        if(mySession != null){
            try{
                mySession.setMessageListener(null);
                mySession.close();
            }catch(JMSException ex){}
            mySession = null;
        }
        if(myConnection != null){
            try{
                for(AMQSession s : ((AMQConnection)myConnection).getSessions().values()){
                    s.close(1000);
                }
                ((AMQConnection)myConnection).close(1000);
            }catch(JMSException ex){}
            myConnection = null;
        }        
    }
    
    public boolean setAutoSend(boolean value){
        if(mySpeechRecSender == null){
            mySoundDetector.stop();
            return false;
        }
        if(value){
            return mySoundDetector.start();
        }else{
            mySoundDetector.stop();
        }
        return value;
    }
    
    public SoundDetector getSoundDetector() {
        return mySoundDetector;
    }
    
    private ConsumerNode<String> buildSpeechSenderNodeChain() throws JMSException{
        String ip = getValue(String.class, CONF_BROKER_IP);
        String port = getValue(String.class, CONF_BROKER_PORT);
        String addr = "tcp://" + ip + ":" + port;
        myConnection = ConnectionManager.createConnection(
                getValue(String.class, CONF_BROKER_USERNAME),
                getValue(String.class, CONF_BROKER_PASSWORD),
                getValue(String.class, CONF_BROKER_CLIENT_NAME),
                getValue(String.class, CONF_BROKER_VIRTUAL_HOST), 
                addr);
        if(myConnection == null){
            return null;
        }
        myConnection.start();
        mySession = 
                myConnection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
        if(mySession == null){
            return null;
        }
        String destStr = getValue(String.class, CONF_DESTINATION);
        Destination dest = ConnectionManager.createDestination(destStr);
        if(dest == null){
            return null;
        }
        ConsumerNode<SpeechRecEventList> ttsNode = NodeChainBuilder.build(
                    JMSAvroUtils.byteStreamRequestFactory(SpeechRecEventList.class))
                .getConsumerChain(JMSAvroUtils.buildEventSenderChain(
                    SpeechRecEventListRecord.class, 
                    SpeechRecEventListRecord.SCHEMA$, 
                    new EmptyAdapter(), 
                    mySession, dest, 
                    new MessageHeaderAdapter("application/speechRecEventList")));
        return NodeChainBuilder.build(
                new SpeechRecEventListFormatter("source", "dest"))
                .getConsumerChain(ttsNode);
    }
    
    public static class SpeechRecEventListFormatter implements 
            Adapter<String, SpeechRecEventList> {
        private String mySourceId;
        private String myDestId;

        public SpeechRecEventListFormatter(String sourceId, String destId){
            if(sourceId == null || destId == null){
                throw new NullPointerException();
            }
            mySourceId = sourceId;
            myDestId = destId;
        }

        @Override
        public SpeechRecEventList adapt(String a) {
            SpeechRecEventRecord event = new SpeechRecEventRecord();
            event.setRecognizerId(mySourceId);
            event.setRecognizedText(a);
            event.setConfidence(1.0);
            event.setTimestampMillisecUTC(TimeUtils.now());

            List<SpeechRecEventRecord> list =
                    new ArrayList<SpeechRecEventRecord>(1);
            list.add(event);
            
            SpeechRecEventListRecord eventList = new SpeechRecEventListRecord();
            eventList.setSpeechRecServiceId(mySourceId);
            eventList.setEventDestinationId(myDestId);
            eventList.setSpeechRecEvents(list);
            eventList.setTimestampMillisecUTC(TimeUtils.now());
            
            return eventList;
        }

    }
}