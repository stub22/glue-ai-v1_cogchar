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
package org.cogchar.bundle.demo.convo;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;
import org.jflux.api.core.node.ConsumerNode;
import org.jflux.api.core.node.ProducerNode;
import org.jflux.api.core.node.chain.NodeChain;
import org.jflux.api.core.node.chain.NodeChainBuilder;
import org.jflux.api.encode.EncodeRequest;
import org.jflux.impl.messaging.JMSAvroUtils;
import org.jflux.impl.transport.jms.MessageHeaderAdapter;
import org.robokind.api.speech.SpeechRequest;
import org.robokind.api.speechrec.SpeechRecEventList;
import org.robokind.avrogen.speech.SpeechRequestRecord;
import org.robokind.avrogen.speechrec.SpeechRecEventListRecord;
import org.robokind.impl.speech.PortableSpeechRequest;
import org.robokind.impl.speechrec.PortableSpeechRecEventList;

/**
 *
 * @author Matthew Stevenson <www.robokind.org>
 */
public class ConversationChain {
    private final static Logger theLogger = Logger.getLogger(ConversationChain.class.getName());
    
    public NodeChain connect(Session recSession, Destination recDest,
            Session ttsSession, Destination ttsDest, String cogbotUrl) {
        ProducerNode<SpeechRecEventList> receiverNode = 
                buildSpeechRecChain(recSession, recDest);
        ConsumerNode<SpeechRequest> senderNode = 
                buildTTSNodeChain(ttsSession, ttsDest);
        
        return NodeChainBuilder.build(receiverNode)
            .attach(new SpeechRecFilter()) 
            .attach(new SpeechRecStringFilter())
            .attach(new ConversationInputFilter())
            .attach(new CogbotProcessor(cogbotUrl))
            .attach(new ConvoResponseFilter())
            .attach(new ConvoResponseStringAdapter())
            .attach(new SpeechFormatter("source", "dest"))
            .attach(senderNode);
    }
    
    private ProducerNode<SpeechRecEventList> buildSpeechRecChain(
            Session session, Destination dest){
        try{
            return JMSAvroUtils.buildEventReceiverChain(
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
            return NodeChainBuilder.build(
                    EncodeRequest.factory(SpeechRequest.class, new JMSAvroUtils.ByteOutputStreamFactory()))
                .getConsumerChain(JMSAvroUtils.buildEventSenderChain(
                    SpeechRequestRecord.class, 
                    SpeechRequestRecord.SCHEMA$, 
                    new PortableSpeechRequest.MessageRecordAdapter(), 
                    session, dest, 
                    new MessageHeaderAdapter("application/speechRequest")));
        }catch(JMSException ex){
            theLogger.log(Level.WARNING,"Error connecting to TTS.",ex);
            return null;
        }
    }
}
