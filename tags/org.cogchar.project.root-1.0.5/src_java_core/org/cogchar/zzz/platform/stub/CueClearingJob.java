package org.cogchar.zzz.platform.stub;

/**
 * @author Stu B. <www.texpedient.com>
 * 
 * When started, clears a particular Cue from a particular CueSpace, which both must be 
 * known at construction time.
 */
public class CueClearingJob extends JobStub {
	private	transient CueSpaceStub			myCueSpace;
	private	CueStub					myCue;
	/**
	 * @param cs - space in which Cue c should exist at time of Job start.
	 * @param c - cue which should exist in Space cs at time of Job start.
	 */
	public CueClearingJob(CueSpaceStub cs, CueStub c) {
		myCueSpace = cs;
		myCue = c;
	}
	/**
	 * SetStatus RUNNING, clearCue, SetStatus COMPLETED
	 */
	protected void start() {
		// TODO Auto-generated method stub
		setStatus(JobStub.Status.RUNNING);
		myCueSpace.clearCue(myCue);
		setStatus(JobStub.Status.COMPLETED);
	}


	/*
	 * @return Description of Cue to be cleared.
	 */
	@Override public String getContentSummaryString() {
		return myCue.toString();
	}

}
