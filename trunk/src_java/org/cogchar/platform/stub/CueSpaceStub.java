/*
 *  Copyright 2008 Hanson Robotics Inc.
 *  All Rights Reserved.
 */

package org.cogchar.platform.stub;

/**
 *
 * @author Stu Baurmann
 */
public interface CueSpaceStub extends ThalamentSpaceStub {



	/**
	 * 
	 * @return
	 */
	//public JobConfig getJobConfig();
	/**
	 * 
	 * @return
	 */
	//public NowCue getSolitaryNowCue();
	
	/**
	 * 
	 * @param c
	 */
	public void clearCue(CueStub c);

	// Used to selectively broadcast cue updates over JMX
	public void broadcastCueUpdate(CueStub c);
	public void clearAllCues();
	

	public void clearAllNowCues();

	public void clearMatchingNamedCues(String name);

	// public void clearMatchingNamedCues(NamedCue nc);

	
//	public void addCueListener(CueListener cl);

//	public void removeCueListener(CueListener cl);

    public void addCue(CueStub c);

	public CueStub addThoughtCueForName(String thoughtName, double strength);


//	public <NCT extends NamedCue> NCT getNamedCue(Class<NCT> clazz, String cueName) ;
}
