/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cogchar.api.integroid.cue;

import org.cogchar.platform.util.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class FriendCue extends PersonCue {
	private static final Logger theLogger = LoggerFactory.getLogger(FriendCue.class);
	private String myPermFriendCueID;
	private Long myPermPersonConfirmStamp;

	public FriendCue() {
		super();
	}

	public String getPermPersonID() {
		return myPermFriendCueID;
	}

	@Override
	public void setOrConfirmPermPersonID(String permID, Long confirmedObsStamp) {
		if (myPermFriendCueID != null) {
			if (myPermFriendCueID.equals(permID)) {
				theLogger.debug("No need to update permID to existing value:" + permID + ", but still might update timestamp");
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
			theLogger.debug("No update needed, timestamps are equal");
		}
	}

	@Override
	public Long getPermPersonConfirmStamp() {
		return myPermPersonConfirmStamp;
	}

	@Override
	public Double getPermPersonConfirmAgeSec() {
		if (myPermPersonConfirmStamp != null) {
			return TimeUtils.getStampAgeSec(myPermPersonConfirmStamp);
		} else {
			return null;
		}
	}

	@Override
	public String getContentSummaryString() {
		return "permFriendCueID=" + myPermFriendCueID + ", " + super.getContentSummaryString();
	}

}
