package org.cogchar.bind.cogbot.unused;

import org.cogchar.bind.cogbot.main.CogbotCommunicator;
import org.cogchar.bind.cogbot.main.GenRespWithConf;
import org.jflux.api.core.Listener;
import org.jflux.api.core.util.EmptyAdapter;
import org.jflux.api.messaging.rk.DefaultMessageAsyncReceiver;
import org.jflux.api.messaging.rk.DefaultMessageSender;
import org.jflux.api.messaging.rk.MessageAsyncReceiver;
import org.jflux.api.messaging.rk.MessageSender;
import org.jflux.api.messaging.rk.RecordAsyncReceiver;
import org.jflux.api.messaging.rk.RecordSender;
import org.jflux.impl.messaging.rk.JMSAvroRecordAsyncReceiver;
import org.jflux.impl.messaging.rk.JMSAvroRecordSender;
import org.jflux.impl.messaging.rk.JMSBytesMessageSender;
import org.jflux.impl.messaging.rk.utils.ConnectionManager;
import org.jflux.impl.messaging.rk.utils.ConnectionUtils;
import org.mechio.api.speech.SpeechRequest;
import org.mechio.api.speech.SpeechRequestFactory;
import org.mechio.impl.speech.PortableSpeechRequest;
import org.mechio.impl.speech.SpeechRequestRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import static org.cogchar.bind.cogbot.main.CogbotConfigUtils.CONF_COGBOT_IP;
import static org.cogchar.bind.cogbot.main.CogbotConfigUtils.getValue;
import static org.cogchar.bind.cogbot.main.CogbotConfigUtils.setOrCreateValue;
import static org.cogchar.bind.cogbot.main.CogbotConfigUtils.setValue;

/**
 * Hello world!
 */
public class CogbotSpeechDemo {
	private static final Logger theLogger = LoggerFactory.getLogger(CogbotSpeechDemo.class);
	private final static String CONF_DEMO_BROKER_IP = "cogbotDemoBrokerIp";
	private final static String CONF_DEMO_BROKER_PORT = "cogbotDemoBrokerPort";
	private final static String CONF_DEMO_BROKER_USERNAME =
			"cogbotDemoBrokerUsername";
	private final static String CONF_DEMO_BROKER_PASSWORD =
			"cogbotDemoBrokerPassword";
	private final static String CONF_DEMO_BROKER_CLIENT_NAME =
			"cogbotDemoBrokerClientName";
	private final static String CONF_DEMO_BROKER_VIRTUAL_HOST =
			"cogbotDemoBrokerVirtualHost";
	private final static String CONF_TTS_DEST = "cogbotDemoTTSDest";
	private final static String CONF_SPREC_DEST = "cogbotDemoSpRecDest";

	private static void setDemoConfigVals() {
		setOrCreateValue(String.class, CONF_DEMO_BROKER_IP, "127.0.0.1");
		setOrCreateValue(String.class, CONF_DEMO_BROKER_PORT, "5672");
		setOrCreateValue(
				String.class, CONF_DEMO_BROKER_USERNAME,
				ConnectionUtils.getUsername());
		setOrCreateValue(
				String.class, CONF_DEMO_BROKER_PASSWORD,
				ConnectionUtils.getPassword());
		setOrCreateValue(String.class, CONF_DEMO_BROKER_CLIENT_NAME, "client1");
		setOrCreateValue(String.class, CONF_DEMO_BROKER_VIRTUAL_HOST, "test");
		setOrCreateValue(String.class, CONF_TTS_DEST, "speechRequest");
		setOrCreateValue(String.class, CONF_SPREC_DEST, "speechRecEvent");
	}

	public static void main(String[] args) {
		setDemoConfigVals();
		Session session = getDemoSession();
		if (session == null) {
			return;
		}
		//setValue(String.class, CONF_COGBOT_IP, "192.168.0.100");
		if (args.length >= 1) {
			setValue(String.class, CONF_COGBOT_IP, args[0]);
		}
		CogbotCommunicator cogbot = createCogbotComm();
		if (cogbot == null) {
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

		MessageSender<SpeechRequest> sender =
				createSpeechSender(session, sendDest);
		if (sender == null) {
			return;
		}
		SpeechHandler handler = new SpeechHandler(cogbot, sender);
		MessageAsyncReceiver<SpeechRequest> receiver =
				createSpeechReceiver(session, recDest);
		if (receiver == null) {
			return;
		}
		receiver.addListener(handler);
		try {
			receiver.start();
		} catch (Exception ex) {
			theLogger.error("Error starting message receiver.", ex);
			return;
		}
	}

	private static Session getDemoSession() {
		String ip = getValue(String.class, CONF_DEMO_BROKER_IP);
		String port = getValue(String.class, CONF_DEMO_BROKER_PORT);
		String addr = "tcp://" + ip + ":" + port;
		Connection con = ConnectionManager.createConnection(
				getValue(String.class, CONF_DEMO_BROKER_USERNAME),
				getValue(String.class, CONF_DEMO_BROKER_PASSWORD),
				getValue(String.class, CONF_DEMO_BROKER_CLIENT_NAME),
				getValue(String.class, CONF_DEMO_BROKER_VIRTUAL_HOST),
				addr);
		try {
			con.start();
			return con.createSession(false, Session.CLIENT_ACKNOWLEDGE);
		} catch (JMSException ex) {
			theLogger.error("Error starting connection.", ex);
			return null;
		}
	}

	private static CogbotCommunicator createCogbotComm() {
		System.out.println("Creating cogbot. Sending hello");
		CogbotCommunicator cogbot = new CogbotCommunicator(null);
		return cogbot;
	}

	private static MessageSender<SpeechRequest> createSpeechSender(
			Session session, Destination dest) {
		DefaultMessageSender<SpeechRequest, SpeechRequestRecord> sender =
				new DefaultMessageSender<>();
		JMSBytesMessageSender bytesSender = new JMSBytesMessageSender();
		bytesSender.setSession(session);
		bytesSender.setDestination(dest);
		RecordSender<SpeechRequestRecord> recSender =
				new JMSAvroRecordSender<>(bytesSender);
		sender.setAdapter(new EmptyAdapter());
		sender.setRecordSender(recSender);
		try {
			sender.start();
		} catch (Exception ex) {
			theLogger.error("Error starting message sender.", ex);
			return null;
		}
		return sender;
	}

	private static MessageAsyncReceiver<SpeechRequest> createSpeechReceiver(
			Session session, Destination dest) {

		DefaultMessageAsyncReceiver<SpeechRequest, SpeechRequestRecord> receiver =
				new DefaultMessageAsyncReceiver<>();
		MessageConsumer consumer;
		try {
			consumer = session.createConsumer(dest);
		} catch (JMSException ex) {
			theLogger.error("Error starting message receiver.", ex);
			return null;
		}
		RecordAsyncReceiver<SpeechRequestRecord> recReceiver =
				new JMSAvroRecordAsyncReceiver<>(
						SpeechRequestRecord.class,
						SpeechRequestRecord.SCHEMA$,
						consumer);
		receiver.setRecordReceiver(recReceiver);
		receiver.setAdapter(new EmptyAdapter());
		return receiver;
	}

	static class SpeechHandler implements Listener<SpeechRequest> {
		private CogbotCommunicator myCogbot;
		private MessageSender<SpeechRequest> mySpeechSender;
		private SpeechRequestFactory myFactory;

		public SpeechHandler(
				CogbotCommunicator cogbot,
				MessageSender<SpeechRequest> speechSender) {
			if (cogbot == null || speechSender == null) {
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
			mySpeechSender.notifyListeners(req);
		}
	}
}
