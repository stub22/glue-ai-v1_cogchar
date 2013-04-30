/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cogchar.bind.cogbot.cogsim;

import java.util.logging.Level;
import java.util.logging.Logger;

import static org.cogchar.bind.cogbot.main.CogbotConfigUtils.*;

/**
 * @author Stu B.
 */
public class CogSimBridge implements Runnable {

    private static Logger theLogger = Logger.getLogger(CogSimBridge.class.getName());
    private CogSimConf myCogSimConf;
    private DictationReciever myDictationImpl;
    final CogbotAvatar avatar;

    public CogSimBridge(CogbotAvatar av, DictationReciever dimpl, CogSimConf conf) {
        avatar = av;
        myDictationImpl = dimpl;
        myCogSimConf = conf;
        //loadCogSimConf();
    }

    public synchronized boolean isConfigured() {
        return ((myCogSimConf != null) && myCogSimConf.isConfigured());
    }

    public void run() {
        int messageIn = 60;
        while (true) {
            if (!getValue(Boolean.class, CONF_COGSIM_POLL_ENABLED) 
                    || !getValue(Boolean.class, CONF_COGSIM_ENABLED)) {
                try {
                    Thread.sleep(getValue(Long.class, CONF_COGSIM_POLL_INTERVAL));
                } catch (InterruptedException ex) {
                    Logger.getLogger(CogSimBridge.class.getName()).log(Level.SEVERE, null, ex);
                    break;
                }
                continue;
            }
            try {
                if (!myCogSimConf.isWaiting()) {
                    String heard = fetchCogsimHeard(false);
                    if (heard != null) {
                        heard = heard.trim();
                    }
                    if ((heard != null) && (heard.length() > 0)) {
                        theLogger.info("Heard from CogSim: [" + heard + "]");
                        myDictationImpl.receiveNetworkText(heard);
                    } else {
                        if (messageIn < 0) {
                            theLogger.info("Heard NOTHING from CogSim");
                            messageIn = 60;
                        }
                    }
                }
                messageIn--;
                Thread.sleep(getValue(Long.class, CONF_COGSIM_POLL_INTERVAL));
            } catch (Throwable t) {
                theLogger.log(Level.SEVERE, "run() caught exception", t);
            }
        }
    }

    private String fetchCogsimHeard(boolean debugFlag) {
        String lastHeardTxt = null;
        if (myCogSimConf != null) {
            if (!myCogSimConf.isWaiting()) {
                CogSimOp cso = new CogSimOp(avatar, myCogSimConf, null);
                try {
                    lastHeardTxt = cso.fetchLastThingWeHeard(debugFlag);
                } catch (Throwable t) {
                    theLogger.log(Level.WARNING, "Failed to fetch cogsim-last-heard", t);
                }
            }
        }
        return lastHeardTxt;
    }
}
