package org.cogchar.bind.cogbot.main;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;


import org.robokind.api.common.utils.Listener;
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

/**
 * Hello world!
 *
 */
public class CogbotSpeechDemo {
    private final static Logger theLogger = Logger.getLogger(CogbotSpeechDemo.class.getName());
    
    public static void main( String[] args ){
        Session session = getSession("127.0.0.1");
        if(session == null){
            return;
        }
        String url = "192.168.0.100";
        if(args.length >= 1){
            url = args[0];
        }
        CogbotCommunicator cogbot = createCogbotComm(url);
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
        Destination sendDest = ConnectionManager.createDestination("speechRequest");
        Destination recDest = ConnectionManager.createDestination("speechrecEvent");
        
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
    
    private static Session getSession(String ip){
        Connection con = 
                ConnectionManager.createConnection(
                "admin", "admin", "client1", "test", "tcp://" + ip + ":5672");
        try{
            con.start();
            return con.createSession(false, Session.CLIENT_ACKNOWLEDGE);
        }catch(JMSException ex){
            theLogger.log(Level.SEVERE, "Error starting connection.", ex);
            return null;
        }
    }
    
    private static CogbotCommunicator createCogbotComm(String url){
        Properties config = new Properties();
		String testUser = "Test user";
		String id = testUser.replace(" ", "");
		try {
			config.load(new FileReader("./resources/config.properties"));
		} catch (FileNotFoundException e) {
			System.out.println("No config file found using defaults.");
			config.setProperty("reset_phrase", "reload aiml");
			String urlStr = url;
			config.setProperty("elbot_url_local", urlStr);
			config.setProperty("elbot_url_remote", urlStr);
			config.setProperty("id", id);
			config.setProperty("id_key", id);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Creating cogbot. Sending hello");
		CogbotCommunicator cogbot = new CogbotCommunicator(url);
		cogbot.setBotProperty("username", testUser);
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
        
        public void handleEvent(SpeechRequest event) {
            String input = event.getPhrase();
            GenRespWithConf genResp = myCogbot.getResponse(input);
            String resp = genResp.getResponse();
            SpeechRequest req = myFactory.create("client", "host", resp);
            mySpeechSender.sendMessage(req);
        }
    }
}
