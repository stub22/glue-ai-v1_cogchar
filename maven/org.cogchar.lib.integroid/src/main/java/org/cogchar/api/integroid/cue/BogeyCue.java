/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cogchar.api.integroid.cue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Stu B. <www.texpedient.com>
 */
public class BogeyCue extends PersonCue {
	private static Logger	theLogger = LoggerFactory.getLogger(BogeyCue.class.getName());
	private String myBogeyID;

	public BogeyCue(String bogeyID) {
		super();
		myBogeyID = bogeyID;
		theLogger.info("Made BogeyCue with bogeyID=" + myBogeyID + " and sessionPersonID=" + fetchSessionCueID());
	}
	@Override public String getContentSummaryString() {
		return "bogeyID=" + myBogeyID + ", " + super.getContentSummaryString();
	}
}
