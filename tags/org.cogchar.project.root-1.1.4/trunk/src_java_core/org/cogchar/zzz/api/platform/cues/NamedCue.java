package org.cogchar.zzz.api.platform.cues;

import org.cogchar.zzz.platform.stub.CueStub;

/*
 *  The name is immutable.
 */

/**
 * 
 * @author Stu B. <www.texpedient.com>
 */
public abstract class NamedCue extends CueStub {
	private final String 	myName;
	
	/**
	 * 
	 * @param n
	 */
	public NamedCue(String n) {
		myName = n;
	}
	/**
	 * 
	 * @return
	 */
	public String getName() {
		return myName;
	}	
	/**
	 * 
	 * @return
	 */
	@Override public String getContentSummaryString() {
		return "ID: " + getThalamentID() + ", Name: " + myName;
	}

}
