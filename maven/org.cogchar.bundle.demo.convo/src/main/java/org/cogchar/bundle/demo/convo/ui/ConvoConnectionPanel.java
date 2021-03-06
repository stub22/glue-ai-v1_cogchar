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

import org.apache.qpid.client.AMQQueue;
import org.apache.qpid.client.AMQTopic;
import org.cogchar.bundle.demo.convo.CogbotProcessor;
import org.cogchar.bundle.demo.convo.ConversationInputFilter;
import org.cogchar.bundle.demo.convo.ConvoResponse;
import org.cogchar.bundle.demo.convo.ConvoResponseFilter;
import org.cogchar.bundle.demo.convo.ConvoResponseStringAdapter;
import org.cogchar.bundle.demo.convo.PannousProcessor;
import org.cogchar.bundle.demo.convo.SpeechFormatter;
import org.cogchar.bundle.demo.convo.SpeechRecFilter;
import org.cogchar.bundle.demo.convo.SpeechRecStringFilter;
import org.jflux.api.core.Listener;
import org.jflux.api.core.Source;
import org.jflux.api.core.node.ConsumerNode;
import org.jflux.api.core.node.DefaultConsumerNode;
import org.jflux.api.core.node.DefaultProcessorNode;
import org.jflux.api.core.node.DefaultProducerNode;
import org.jflux.api.core.node.ProcessorNode;
import org.jflux.api.core.node.ProducerNode;
import org.jflux.api.core.node.chain.NodeChain;
import org.jflux.api.core.node.chain.NodeChainBuilder;
import org.jflux.api.core.util.EmptyAdapter;
import org.jflux.api.messaging.rk.services.ServiceCommand;
import org.jflux.impl.messaging.rk.JMSAvroMessageAsyncReceiver;
import org.jflux.impl.messaging.rk.JMSAvroMessageSender;
import org.jflux.impl.messaging.rk.ServiceCommandRecord;
import org.jflux.impl.messaging.rk.services.PortableServiceCommand;
import org.mechio.api.speech.SpeechRequest;
import org.mechio.api.speechrec.SpeechRecEvent;
import org.mechio.api.speechrec.SpeechRecEventList;
import org.mechio.impl.speech.SpeechRequestRecord;
import org.mechio.impl.speechrec.SpeechRecEventListRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;

import javax.jms.Destination;
import javax.jms.Session;

import static org.cogchar.bundle.demo.convo.osgi.ConvoConfigUtils.CONF_COGBOT_IP;
import static org.cogchar.bundle.demo.convo.osgi.ConvoConfigUtils.CONF_COGBOT_POLL_INTERVAL;
import static org.cogchar.bundle.demo.convo.osgi.ConvoConfigUtils.CONF_SPREC_BROKER_CLIENT_NAME;
import static org.cogchar.bundle.demo.convo.osgi.ConvoConfigUtils.CONF_SPREC_BROKER_IP;
import static org.cogchar.bundle.demo.convo.osgi.ConvoConfigUtils.CONF_SPREC_BROKER_PASSWORD;
import static org.cogchar.bundle.demo.convo.osgi.ConvoConfigUtils.CONF_SPREC_BROKER_PORT;
import static org.cogchar.bundle.demo.convo.osgi.ConvoConfigUtils.CONF_SPREC_BROKER_USERNAME;
import static org.cogchar.bundle.demo.convo.osgi.ConvoConfigUtils.CONF_SPREC_BROKER_VIRTUAL_HOST;
import static org.cogchar.bundle.demo.convo.osgi.ConvoConfigUtils.CONF_SPREC_DESTINATION;
import static org.cogchar.bundle.demo.convo.osgi.ConvoConfigUtils.CONF_TTS_BROKER_CLIENT_NAME;
import static org.cogchar.bundle.demo.convo.osgi.ConvoConfigUtils.CONF_TTS_BROKER_IP;
import static org.cogchar.bundle.demo.convo.osgi.ConvoConfigUtils.CONF_TTS_BROKER_PASSWORD;
import static org.cogchar.bundle.demo.convo.osgi.ConvoConfigUtils.CONF_TTS_BROKER_PORT;
import static org.cogchar.bundle.demo.convo.osgi.ConvoConfigUtils.CONF_TTS_BROKER_USERNAME;
import static org.cogchar.bundle.demo.convo.osgi.ConvoConfigUtils.CONF_TTS_BROKER_VIRTUAL_HOST;
import static org.cogchar.bundle.demo.convo.osgi.ConvoConfigUtils.CONF_TTS_DESTINATION;
import static org.cogchar.bundle.demo.convo.osgi.ConvoConfigUtils.getSetter;
import static org.cogchar.bundle.demo.convo.osgi.ConvoConfigUtils.getSource;

/**
 * @author Matthew Stevenson <www.cogchar.org>
 */
public class ConvoConnectionPanel extends javax.swing.JPanel {
	private static final Logger theLogger = LoggerFactory.getLogger(ConvoConnectionPanel.class);
	ProducerNode<SpeechRecEventList<SpeechRecEvent>> mySpeechProducer;
	ProcessorNode<String, ConvoResponse> myConvoProc;
	ConsumerNode<ServiceCommand> myTTSCommnadSender;
	ConsumerNode<ServiceCommand> myAnimPromptSender;
	ConsumerNode<SpeechRequest> myResponseSender;

	private NodeChain myChain;
	private Source<String> myCogbotIpSource;
	private Listener<String> myCogbotIpSetter;
	private Source<Long> myCogbotPollIntervalSource;
	private Listener<Long> myCogbotPollIntervalSetter;

	/**
	 * Creates new form ConvoConnectionPanel
	 */
	public ConvoConnectionPanel() {
		initComponents();
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
		pnlPannousConnection.setVisible(false);
	}

	public boolean connect() {
		if (!pnlRecConnect.connect()) {
			return false;
		}
		if (!pnlTTSConnect.connect()) {
			pnlRecConnect.disconnect();
			return false;
		}
		myChain = connect(
				pnlRecConnect.getSession(), pnlRecConnect.getDestination(),
				pnlTTSConnect.getSession(), pnlTTSConnect.getDestination(),
				txtCogbot.getText());
		if (myChain == null || !myChain.start()) {
			disconnect();
			return false;
		}
		txtCogbot.setEnabled(false);
		return true;
	}

	public ProcessorNode<String, ConvoResponse> getConvoProc() {
		return myConvoProc;
	}

	public NodeChain connect(Session recSession, Destination recDest,
							 Session ttsSession, Destination ttsDest, String cogbotUrl) {

		mySpeechProducer = buildSpeechRecChain(recSession, recDest);
		myResponseSender = buildTTSNodeChain(ttsSession, ttsDest);
		try {
			myTTSCommnadSender = buildServiceCommandNodeChain(ttsSession,
					new AMQQueue("speechCommand; {create: always, node: {type: queue}}"));
			myAnimPromptSender = buildServiceCommandNodeChain(ttsSession,
					new AMQTopic("animPrompt; {create: always, node: {type: topic}}"));
		} catch (URISyntaxException ex) {
		}
		if (COGBOT.equals(comboService.getSelectedItem())) {
			CogbotProcessor proc = new CogbotProcessor(cogbotUrl);
			myConvoProc = new DefaultProcessorNode<>(proc);
			proc.setInputListener(myConvoProc.getListener());
		} else if (PANNOUS.equals(comboService.getSelectedItem())) {
			myConvoProc = new DefaultProcessorNode<>(new PannousProcessor("", getPannousTimeout()));
		}
		if (mySpeechProducer == null ||
				myResponseSender == null || myTTSCommnadSender == null) {
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

	private int getPannousTimeout() {
		String intStr = txtPannousTimeout.getText();
		try {
			return Integer.parseInt(intStr);
		} catch (NumberFormatException ex) {
			return 10000;
		}
	}

	private ProducerNode<SpeechRecEventList<SpeechRecEvent>> buildSpeechRecChain(
			Session session, Destination dest) {
		JMSAvroMessageAsyncReceiver<SpeechRecEventList<SpeechRecEvent>, SpeechRecEventListRecord> rec =
				new JMSAvroMessageAsyncReceiver<>(
						session, dest, SpeechRecEventListRecord.class, SpeechRecEventListRecord.SCHEMA$);
		rec.setAdapter(new EmptyAdapter());
		try {
			rec.start();
		} catch (Exception ex) {
			theLogger.error("Unable to start Message Receiver", ex);
			return null;
		}
		return new DefaultProducerNode<>(rec);
//        try{
//            return JMSAvroUtils.buildEventReceiverChain(
//                    SpeechRecEventListRecord.class, 
//                    SpeechRecEventListRecord.SCHEMA$, 
//                    new EmptyAdapter(), 
//                    session, dest);
//        }catch(JMSException ex){
//            theLogger.log(Level.WARNING,"Error connecting to Speech Rec.",ex);
//            return null;
//        }
	}

	private ConsumerNode<SpeechRequest> buildTTSNodeChain(
			Session session, Destination dest) {
		try {
			JMSAvroMessageSender<SpeechRequest, SpeechRequestRecord> sender =
					new JMSAvroMessageSender<>(session, dest);
			sender.setAdapter(new EmptyAdapter());
			sender.setDefaultContentType("application/speechRequest");
			sender.start();
			return new DefaultConsumerNode<>(sender);
//            return NodeChainBuilder.build(
//                    EncodeRequest.factory(SpeechRequest.class, new JMSAvroUtils.ByteOutputStreamFactory()))
//                .getConsumerChain(JMSAvroUtils.buildEventSenderChain(
//                    SpeechRequestRecord.class, 
//                    SpeechRequestRecord.SCHEMA$, 
//                    new EmptyAdapter(), 
//                    session, dest, 
//                    new MessageHeaderAdapter("application/speechRequest")));
		} catch (Exception ex) {
			theLogger.warn("Error connecting to TTS.", ex);
			return null;
		}
	}

	private ConsumerNode<ServiceCommand> buildServiceCommandNodeChain(
			Session session, Destination dest) {
		try {
			JMSAvroMessageSender<ServiceCommand, ServiceCommandRecord> sender =
					new JMSAvroMessageSender<>(session, dest);
			sender.setAdapter(new EmptyAdapter());
			sender.setDefaultContentType("application/service-command");
			sender.start();
			return new DefaultConsumerNode<>(sender);
		} catch (Exception ex) {
			theLogger.warn("Error connecting to TTS.", ex);
			return null;
		}
//        try{
//            return NodeChainBuilder.build(
//                    EncodeRequest.factory(ServiceCommand.class, new JMSAvroUtils.ByteOutputStreamFactory()))
//                .getConsumerChain(JMSAvroUtils.buildEventSenderChain(
//                    ServiceCommandRecord.class, 
//                    ServiceCommandRecord.SCHEMA$, 
//                    new EmptyAdapter(), 
//                    session, dest, 
//                    new MessageHeaderAdapter("application/service-command")));
//        }catch(JMSException ex){
//            theLogger.log(Level.WARNING,"Error connecting to TTS.",ex);
//            return null;
//        }
	}

	public void disconnect() {
		if (myChain != null) {
			myChain.stop();
			myChain = null;
		}
		if (myConvoProc != null) {
			myConvoProc = null;
		}
		pnlRecConnect.disconnect();
		pnlTTSConnect.disconnect();
		txtCogbot.setEnabled(true);
	}

	public void updatetPollInterval() {
		String str = txtCogbotPollInterval.getText();
		try {
			Long interval = Long.parseLong(str);
			interval = Math.max(interval, 1);
			myCogbotPollIntervalSetter.handleEvent(interval);
		} catch (NumberFormatException ex) {
			theLogger.warn("Invalid Poll Interval, not a number: {}", str, ex);
		}
	}

	public void setPollIntervalEnabled(boolean val) {
		txtCogbotPollInterval.setEnabled(val);
	}

	/**
	 * This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	// <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
	private void initComponents() {

		buttonGroup1 = new javax.swing.ButtonGroup();
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
		comboService = new javax.swing.JComboBox();
		jLabel4 = new javax.swing.JLabel();
		pnlPannousConnection = new javax.swing.JPanel();
		jLabel6 = new javax.swing.JLabel();
		txtPannousTimeout = new javax.swing.JTextField();
		jLabel7 = new javax.swing.JLabel();

		pnlSpeechRec.setBorder(javax.swing.BorderFactory.createTitledBorder("Speech Rec Connection"));

		javax.swing.GroupLayout pnlSpeechRecLayout = new javax.swing.GroupLayout(pnlSpeechRec);
		pnlSpeechRec.setLayout(pnlSpeechRecLayout);
		pnlSpeechRecLayout.setHorizontalGroup(
				pnlSpeechRecLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addComponent(pnlRecConnect, javax.swing.GroupLayout.DEFAULT_SIZE, 560, Short.MAX_VALUE)
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
										.addComponent(txtCogbot, javax.swing.GroupLayout.DEFAULT_SIZE, 437, Short.MAX_VALUE)))
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
						.addComponent(pnlTTSConnect, javax.swing.GroupLayout.DEFAULT_SIZE, 560, Short.MAX_VALUE)
		);
		pnlTTSLayout.setVerticalGroup(
				pnlTTSLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addComponent(pnlTTSConnect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
		);

		comboService.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"Cogbot", "Pannous"}));
		comboService.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				comboServiceActionPerformed(evt);
			}
		});

		jLabel4.setText("Answering Service");

		pnlPannousConnection.setBorder(javax.swing.BorderFactory.createTitledBorder("Pannous Connection"));

		jLabel6.setText("Timeout");

		txtPannousTimeout.setText("10000");

		jLabel7.setText("(millisec)");

		javax.swing.GroupLayout pnlPannousConnectionLayout = new javax.swing.GroupLayout(pnlPannousConnection);
		pnlPannousConnection.setLayout(pnlPannousConnectionLayout);
		pnlPannousConnectionLayout.setHorizontalGroup(
				pnlPannousConnectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(pnlPannousConnectionLayout.createSequentialGroup()
								.addGap(52, 52, 52)
								.addComponent(jLabel6)
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(txtPannousTimeout, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(jLabel7)
								.addContainerGap(200, Short.MAX_VALUE))
		);
		pnlPannousConnectionLayout.setVerticalGroup(
				pnlPannousConnectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(pnlPannousConnectionLayout.createSequentialGroup()
								.addGroup(pnlPannousConnectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(txtPannousTimeout, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
										.addComponent(jLabel6)
										.addComponent(jLabel7))
								.addGap(12, 12, 12))
		);

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
		this.setLayout(layout);
		layout.setHorizontalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addComponent(pnlSpeechRec, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(pnlTTS, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addGroup(layout.createSequentialGroup()
								.addComponent(jLabel4)
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(comboService, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addGap(335, 335, 335))
						.addComponent(pnlCogbotConnection, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(pnlPannousConnection, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(layout.createSequentialGroup()
								.addComponent(pnlSpeechRec, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(pnlTTS, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(jLabel4)
										.addComponent(comboService, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(pnlCogbotConnection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
								.addComponent(pnlPannousConnection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addContainerGap())
		);
	}// </editor-fold>//GEN-END:initComponents

	private final static String COGBOT = "Cogbot";
	private final static String PANNOUS = "Pannous";

	private void comboServiceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboServiceActionPerformed
		if (COGBOT.equals(comboService.getSelectedItem())) {
			pnlCogbotConnection.setVisible(true);
			pnlPannousConnection.setVisible(false);
		} else if (PANNOUS.equals(comboService.getSelectedItem())) {
			pnlCogbotConnection.setVisible(false);
			pnlPannousConnection.setVisible(true);
		}
	}//GEN-LAST:event_comboServiceActionPerformed

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.ButtonGroup buttonGroup1;
	private javax.swing.JComboBox comboService;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JLabel jLabel2;
	private javax.swing.JLabel jLabel3;
	private javax.swing.JLabel jLabel4;
	private javax.swing.JLabel jLabel6;
	private javax.swing.JLabel jLabel7;
	private javax.swing.JPanel pnlCogbotConnection;
	private javax.swing.JPanel pnlPannousConnection;
	private org.cogchar.bundle.demo.convo.ui.MessagingConnectPanel pnlRecConnect;
	private javax.swing.JPanel pnlSpeechRec;
	private javax.swing.JPanel pnlTTS;
	private org.cogchar.bundle.demo.convo.ui.MessagingConnectPanel pnlTTSConnect;
	private javax.swing.JTextField txtCogbot;
	private javax.swing.JTextField txtCogbotPollInterval;
	private javax.swing.JTextField txtPannousTimeout;
	// End of variables declaration//GEN-END:variables
}
