package org.cogchar.api.platform.cues;

import org.cogchar.platform.stub.CueStub;

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
