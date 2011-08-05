/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cogchar.integroid.cue;

import java.util.logging.Logger;
import org.cogchar.platform.util.TimeUtils;

/**
 *
 * @author Stu B. <www.texpedient.com>
 */
public class FriendCue extends PersonCue {
	private static Logger	theLogger = Logger.getLogger(FriendCue.class.getName());
	private String		myPermFriendCueID;
	private Long		myPermPersonConfirmStamp;

	public FriendCue() {
		super();
	}
	public String getPermPersonID() {
		return myPermFriendCueID;
	}

	@Override public void setOrConfirmPermPersonID(String permID, Long confirmedObsStamp) {
		if (myPermFriendCueID != null) {
			if (myPermFriendCueID.equals(permID)) {
				theLogger.fine("No need to update permID to existing value:" + permID + ", but still might update timestamp");
			} else {
				throw new RuntimeException("Illegal attempt to update permPersonID from " + myPermFriendCueID + " to " + permID);
			}
		} else {
			myPermFriendCueID = permID;
		}
		if ((myPermPersonConfirmStamp == null) || (confirmedObsStamp > myPermPersonConfirmStamp)) {
			myPermPersonConfirmStamp = confirmedObsStamp;
			this.markUpdatedNow();
		} else {
			theLogger.fine("No update needed, timestamps are equal");
		}
	}

	@Override public Long getPermPersonConfirmStamp() {
		return myPermPersonConfirmStamp;
	}

	@Override public Double getPermPersonConfirmAgeSec() {
		if (myPermPersonConfirmStamp != null) {
			return TimeUtils.getStampAgeSec(myPermPersonConfirmStamp);
		} else {
			return null;
		}
	}
	@Override public String getContentSummaryString() {
		return "permFriendCueID=" + myPermFriendCueID + ", " + super.getContentSummaryString();
	}

}
