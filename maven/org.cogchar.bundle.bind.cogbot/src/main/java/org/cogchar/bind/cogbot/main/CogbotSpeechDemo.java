package org.cogchar.bind.cogbot.main;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;


import org.jflux.api.core.Listener;
import org.robokind.api.messaging.DefaultMessageAsyncReceiver;
import org.robokind.api.messaging.DefaultMessageSender;
import org.robokind.api.messaging.MessageAsyncReceiver;
import org.robokind.api.messaging.MessageSender;
import org.robokind.api.messaging.RecordAsyncReceiver;
import org.robokind.api.messaging.RecordSender;
import org.robokind.api.speech.SpeechRequest;
import org.robokind.api.speech.SpeechRequestFactory;
import org.robokind.avrogen.speech.SpeechRequestRecord;
import org.robokind.impl.messaging.JMSAvroRecordAsyncReceiver;
import org.robokind.impl.messaging.JMSAvroRecordSender;
import org.robokind.impl.messaging.JMSBytesMessageSender;
import org.robokind.impl.messaging.utils.ConnectionManager;
import org.robokind.impl.speech.PortableSpeechRequest;

import static org.cogchar.bind.cogbot.osgi.CogbotConfigUtils.*;

/**
 * Hello world!
 *
 */
public class CogbotSpeechDemo {
    private final static Logger theLogger = Logger.getLogger(CogbotSpeechDemo.class.getName());
    private final static String CONF_DEMO_BROKER_IP = "cogbotDemoBrokerIp";
    private final static String CONF_DEMO_BROKER_PORT = "cogbotDemoBrokerPort";
    private final static String CONF_DEMO_BROKER_USERNAME = "cogbotDemoBrokerUsername";
    private final static String CONF_DEMO_BROKER_PASSWORD = "cogbotDemoBrokerPassword";
    private final static String CONF_DEMO_BROKER_CLIENT_NAME = "cogbotDemoBrokerClientName";
    private final static String CONF_DEMO_BROKER_VIRTUAL_HOST = "cogbotDemoBrokerVirtualHost";
    private final static String CONF_TTS_DEST = "cogbotDemoTTSDest";
    private final static String CONF_SPREC_DEST = "cogbotDemoSpRecDest";
    
    private static void setDemoConfigVals(){
        setOrCreateValue(String.class, CONF_DEMO_BROKER_IP, "127.0.0.1");
        setOrCreateValue(String.class, CONF_DEMO_BROKER_PORT, "5672");
        setOrCreateValue(String.class, CONF_DEMO_BROKER_USERNAME, "admin");
        setOrCreateValue(String.class, CONF_DEMO_BROKER_PASSWORD, "admin");
        setOrCreateValue(String.class, CONF_DEMO_BROKER_CLIENT_NAME, "client1");
        setOrCreateValue(String.class, CONF_DEMO_BROKER_VIRTUAL_HOST, "test");
        setOrCreateValue(String.class, CONF_TTS_DEST, "speechRequest");
        setOrCreateValue(String.class, CONF_SPREC_DEST, "speechRecEvent");
    }
    
    public static void main( String[] args ){
        setDemoConfigVals();
        Session session = getDemoSession();
        if(session == null){
            return;
        }
        //setValue(String.class, CONF_COGBOT_IP, "192.168.0.100");
        if(args.length >= 1){
            setValue(String.class, CONF_COGBOT_IP, args[0]);
        }
        CogbotCommunicator cogbot = createCogbotComm();
        if(cogbot == null){
            return;
        }
        GenRespWithConf resp = cogbot.getResponse("hello friend!");
        String respStr = resp.getResponse();
        theLogger.info("++++++++++++++++++++++++++++++++++++++++++++++++++");
        theLogger.info("++++++++++++++++++++++++++++++++++++++++++++++++++");
        theLogger.info("++++++++++++++++++++++++++++++++++++++++++++++++++");
        theLogger.info(respStr);
        theLogger.info("++++++++++++++++++++++++++++++++++++++++++++++++++");
        theLogger.info("++++++++++++++++++++++++++++++++++++++++++++++++++");
        theLogger.info("++++++++++++++++++++++++++++++++++++++++++++++++++");
        Destination sendDest = ConnectionManager.createDestination(
                getValue(String.class, CONF_TTS_DEST));
        Destination recDest = ConnectionManager.createDestination(
                getValue(String.class, CONF_SPREC_DEST));

        MessageSender<SpeechRequest> sender = createSpeechSender(session, sendDest);
        if(sender == null){
            return;
        }
        SpeechHandler handler = new SpeechHandler(cogbot, sender);
        MessageAsyncReceiver<SpeechRequest> receiver = createSpeechReceiver(session, recDest);
        if(receiver == null){
            return;
        }
        receiver.addMessageListener(handler);
        try{
            receiver.start();
        }catch(Exception ex){
            theLogger.log(Level.SEVERE, "Error starting message receiver.", ex);
            return;
        }
    }

    private static Session getDemoSession(){
        String ip = getValue(String.class, CONF_DEMO_BROKER_IP);
        String port = getValue(String.class, CONF_DEMO_BROKER_PORT);
        String addr = "tcp://" + ip + ":" + port;
        Connection con = ConnectionManager.createConnection(
                getValue(String.class, CONF_DEMO_BROKER_USERNAME),
                getValue(String.class, CONF_DEMO_BROKER_PASSWORD),
                getValue(String.class, CONF_DEMO_BROKER_CLIENT_NAME),
                getValue(String.class, CONF_DEMO_BROKER_VIRTUAL_HOST), 
                addr);
        try{
            con.start();
            return con.createSession(false, Session.CLIENT_ACKNOWLEDGE);
        }catch(JMSException ex){
            theLogger.log(Level.SEVERE, "Error starting connection.", ex);
            return null;
        }
    }

    private static CogbotCommunicator createCogbotComm(){
		System.out.println("Creating cogbot. Sending hello");
		CogbotCommunicator cogbot = new CogbotCommunicator(null);
        return cogbot;
    }

    private static MessageSender<SpeechRequest> createSpeechSender(
            Session session, Destination dest){
        DefaultMessageSender<SpeechRequest, SpeechRequestRecord> sender =
                new DefaultMessageSender<SpeechRequest, SpeechRequestRecord>();
        JMSBytesMessageSender bytesSender = new JMSBytesMessageSender();
        bytesSender.setSession(session);
        bytesSender.setDestination(dest);
        RecordSender<SpeechRequestRecord> recSender =
                new JMSAvroRecordSender<SpeechRequestRecord>(bytesSender);
        sender.setAdapter(new PortableSpeechRequest.MessageRecordAdapter());
        sender.setRecordSender(recSender);
        try{
            sender.start();
        }catch(Exception ex){
            theLogger.log(Level.SEVERE, "Error starting message sender.", ex);
            return null;
        }
        return sender;
    }

    private static MessageAsyncReceiver<SpeechRequest> createSpeechReceiver(
            Session session, Destination dest){

        DefaultMessageAsyncReceiver<SpeechRequest, SpeechRequestRecord> receiver =
                new DefaultMessageAsyncReceiver<SpeechRequest, SpeechRequestRecord>();
        MessageConsumer consumer;
        try{
            consumer = session.createConsumer(dest);
        }catch(JMSException ex){
            theLogger.log(Level.SEVERE, "Error starting message receiver.", ex);
            return null;
        }
        RecordAsyncReceiver<SpeechRequestRecord> recReceiver =
                new JMSAvroRecordAsyncReceiver<SpeechRequestRecord>(
                        SpeechRequestRecord.class,
                        SpeechRequestRecord.SCHEMA$,
                        consumer);
        receiver.setRecordReceiver(recReceiver);
        receiver.setAdapter(new PortableSpeechRequest.RecordMessageAdapter());
        return receiver;
    }

    static class SpeechHandler implements Listener<SpeechRequest>{
        private CogbotCommunicator myCogbot;
        private MessageSender<SpeechRequest> mySpeechSender;
        private SpeechRequestFactory myFactory;

        public SpeechHandler(
                CogbotCommunicator cogbot,
                MessageSender<SpeechRequest> speechSender){
            if(cogbot == null || speechSender == null){
                throw new NullPointerException();
            }
            myCogbot = cogbot;
            mySpeechSender = speechSender;
            myFactory = new PortableSpeechRequest.Factory();
        }

        @Override
        public void handleEvent(SpeechRequest event) {
            String input = event.getPhrase();
            GenRespWithConf genResp = myCogbot.getResponse(input);
            String resp = genResp.getResponse();
            SpeechRequest req = myFactory.create("client", "host", resp);
            mySpeechSender.sendMessage(req);
        }
    }
}
