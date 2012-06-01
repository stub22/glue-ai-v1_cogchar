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
 * MessagingConnectPanel.java
 *
 * Created on Apr 26, 2012, 3:25:43 PM
 */
package org.cogchar.bundle.demo.convo.ui;

import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.*;
import org.apache.qpid.client.AMQAnyDestination;
import org.apache.qpid.client.AMQConnectionFactory;
import org.jflux.api.core.Listener;
import org.jflux.api.core.Source;

/**
 *
 * @author Matthew Stevenson <www.robokind.org>
 */
public class MessagingConnectPanel extends javax.swing.JPanel {
    private final static Logger theLogger = Logger.getLogger(MessagingConnectPanel.class.getName());
    private final static String theAMQPFormatString = "amqp://%s:%s@%s/%s?brokerlist='%s'";
    Connection myConnection;
    Session mySession;
    Destination myDestination;
    
    private Source<String> myIpSource;
    private Source<String> myDestSource;
    private Source<String> myPortSource;
    private Source<String> myUsernameSource;
    private Source<String> myPasswordSource;
    private Source<String> myClientNameSource;
    private Source<String> myVirtualHostSource;
    private Listener<String> myIpSetter;
    private Listener<String> myDestSetter;
        
    /** Creates new form MessagingConnectPanel */
    public MessagingConnectPanel() {
        initComponents();
    }
    
    public void setBrokerAddress(
            Source<String> ipSrc, 
            Listener<String> ipSet,
            Source<String> portSource,
            Source<String> usernameSource,
            Source<String> passwordSource,
            Source<String> clientNameSource,
            Source<String> virtualHostSource){
        if(ipSrc == null || ipSet == null || portSource == null
                || usernameSource == null || passwordSource == null
                || clientNameSource == null || virtualHostSource == null){
            throw new NullPointerException();
        }
        myIpSource = ipSrc;
        myIpSetter = ipSet;
        myPortSource = portSource;
        myUsernameSource = usernameSource;
        myPasswordSource = passwordSource;
        myClientNameSource = clientNameSource;
        myVirtualHostSource = virtualHostSource;
        txtBrokerAddress.setText(myIpSource.getValue());
    }
    
    public void setDestination(Source<String> src, Listener<String> set){
        if(src == null || set == null){
            throw new NullPointerException();
        }
        myDestSource = src;
        myDestSetter = set;
        txtDestination.setText(myDestSource.getValue());
    }
    
    public boolean connect(){
        String ip = txtBrokerAddress.getText();
        String dest = txtDestination.getText();
        myIpSetter.handleEvent(ip);
        myDestSetter.handleEvent(dest);
        return connect0();
    }
    
    private boolean connect0(){
        myDestination = buildDestination();
        if(myDestination == null){
            return false;
        }
        myConnection = buildConnection();
        if(myConnection == null){
            return false;
        }
        mySession = buildSession(myConnection);
        if(mySession == null){
            disconnect();
            return false;
        }
        txtBrokerAddress.setEnabled(false);
        txtDestination.setEnabled(false);
        return true;
    }
    
    public void disconnect(){
        if(mySession != null){
            try{
                mySession.close();
            }catch(JMSException ex){}
            mySession = null;
        }
        if(myConnection != null){
            try{
                myConnection.close();
            }catch(JMSException ex){}
            myConnection = null;
        }
        txtBrokerAddress.setEnabled(true);
        txtDestination.setEnabled(true);
    }
    
    private String createAMQPConnectionURL(
            String username, String password, 
            String clientName, String virtualHost, String tcpAddress){
        return String.format(theAMQPFormatString, 
                username, password, clientName, virtualHost, tcpAddress);
    }
    
    private Connection buildConnection(){
        String ip = myIpSource.getValue();
        if(ip == null){
            throw new NullPointerException();
        }
        String port = myPortSource.getValue();
        String addr = "tcp://" + ip + ":" + port;
        String url = createAMQPConnectionURL(
                myUsernameSource.getValue(), 
                myPasswordSource.getValue(), 
                myClientNameSource.getValue(), 
                myVirtualHostSource.getValue(), 
                addr);
        try{ 
            ConnectionFactory fact = new AMQConnectionFactory(url);
            Connection con = fact.createConnection();
            if(con == null){
                return null;
            }
            con.start();
            return con;
        }catch(Exception ex){
            theLogger.log(Level.WARNING, "Error creating Session.", ex);
            return null;
        }
    }
    
    private Session buildSession(Connection con){
        if(con == null){
            throw new NullPointerException();
        }
        try{
            return con.createSession(false, Session.CLIENT_ACKNOWLEDGE);
        }catch(Exception ex){
            theLogger.log(Level.WARNING, "Error creating Session.", ex);
            return null;
        }
    }
    
    private Destination buildDestination(){
        String dest = myDestSource.getValue();
        if(dest == null){
            throw new NullPointerException();
        }
        try{
            return new AMQAnyDestination(dest);
        }catch(URISyntaxException ex){
            theLogger.log(Level.WARNING, "Error creating Destination.", ex);
            return null;
        }
    }
    
    public Session getSession(){
        return mySession;
    }
    
    public Destination getDestination(){
        return myDestination;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblDestination = new javax.swing.JLabel();
        lblBrokerAddress = new javax.swing.JLabel();
        txtBrokerAddress = new javax.swing.JTextField();
        txtDestination = new javax.swing.JTextField();

        lblDestination.setText("Destination"); // NOI18N

        lblBrokerAddress.setText("Broker Address"); // NOI18N

        txtBrokerAddress.setText("127.0.0.1"); // NOI18N

        txtDestination.setText("destinationName; {create: always, node: {type: queue}}"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblBrokerAddress)
                    .addComponent(lblDestination))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtDestination)
                    .addComponent(txtBrokerAddress)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblBrokerAddress)
                    .addComponent(txtBrokerAddress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtDestination, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblDestination)))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblBrokerAddress;
    private javax.swing.JLabel lblDestination;
    private javax.swing.JTextField txtBrokerAddress;
    private javax.swing.JTextField txtDestination;
    // End of variables declaration//GEN-END:variables
}
