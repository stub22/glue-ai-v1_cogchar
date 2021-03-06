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
 * ConversationOutputPanel.java
 *
 * Created on Apr 24, 2012, 6:14:03 PM
 */
package org.cogchar.bundle.demo.convo.ui;

import java.text.DateFormat;
import java.util.Date;
import javax.swing.SwingUtilities;
import org.cogchar.bundle.demo.convo.ConvoResponse;
import org.jflux.api.core.Listener;

/**
 *
 * @author Matthew Stevenson <www.cogchar.org>
 */
public class ConversationLogPanel extends javax.swing.JPanel {
    private static DateFormat theDateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);
    private ConvoResponseListener myResponseListener;
    /** Creates new form ConversationOutputPanel */
    public ConversationLogPanel() {
        initComponents();
        myResponseListener = new ConvoResponseListener();
    }
    
    public Listener<ConvoResponse> getConvoResponseListener(){
        return myResponseListener;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        txtConvoLog = new javax.swing.JTextArea();

        txtConvoLog.setColumns(20);
        txtConvoLog.setEditable(false);
        txtConvoLog.setLineWrap(true);
        txtConvoLog.setRows(5);
        txtConvoLog.setWrapStyleWord(true);
        jScrollPane1.setViewportView(txtConvoLog);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 264, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea txtConvoLog;
    // End of variables declaration//GEN-END:variables

    class ConvoResponseListener implements Listener<ConvoResponse> {

        @Override
        public void handleEvent(ConvoResponse event) {
            if(event == null){
                return;
            }
            String input = event.getInput();
            if(input == null || input.isEmpty()){
                return;
            }
            String out = formatResponse(event);
            String prev = txtConvoLog.getText().trim();
            String log =  (prev == null ||  prev.isEmpty()) 
                    ? out : "\n\n" + out;
            txtConvoLog.append(log);
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    jScrollPane1.getVerticalScrollBar().setValue(
                            jScrollPane1.getVerticalScrollBar().getMaximum());
                }
            });
        }
        
        private String formatResponse(ConvoResponse resp){
            return "[" + theDateFormat.format(new Date()) + "]:\n" 
                    + "input:\n" +resp.getInput() 
                    + "\nresponse:\n" + resp.getResponse();
        }
        
    }
}
