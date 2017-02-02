/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cogchar.bind.cogbot.scripting;

import org.cogchar.bind.cogbot.cogsim.CogbotAvatar;

/**
 * @author Administrator
 */
public class MessageClient {

	// static public CogbotJMXClient cogbotJMXClient;
	CogbotAvatar avatar;

	MessageClient(CogbotAvatar av) {
		avatar = av;
		try {
			//cogbotJMXClient = new CogbotJMXClient(CogbotJMXClient.serviceURL);
		} catch (Throwable ex) {
			org.slf4j.LoggerFactory.getLogger(MessageClient.class).error(ex.getMessage(), ex);
		}
	}
}
