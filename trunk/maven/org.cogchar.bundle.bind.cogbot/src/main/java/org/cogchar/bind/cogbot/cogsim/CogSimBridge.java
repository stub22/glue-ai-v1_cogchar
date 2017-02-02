/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cogchar.bind.cogbot.cogsim;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.cogchar.bind.cogbot.main.CogbotConfigUtils.CONF_COGSIM_ENABLED;
import static org.cogchar.bind.cogbot.main.CogbotConfigUtils.CONF_COGSIM_POLL_ENABLED;
import static org.cogchar.bind.cogbot.main.CogbotConfigUtils.CONF_COGSIM_POLL_INTERVAL;
import static org.cogchar.bind.cogbot.main.CogbotConfigUtils.getValue;

/**
 * @author Stu B.
 */
public class CogSimBridge implements Runnable {

	private static final Logger theLogger = LoggerFactory.getLogger(CogSimBridge.class);
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
					org.slf4j.LoggerFactory.getLogger(CogSimBridge.class).error(ex.getMessage(), ex);
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
				theLogger.error("run() caught exception", t);
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
					theLogger.warn("Failed to fetch cogsim-last-heard", t);
				}
			}
		}
		return lastHeardTxt;
	}
}
