/*
 *  Copyright 2008 Hanson Robotics Inc.
 *  All Rights Reserved.
 */

package org.cogchar.platform.stub;

import java.util.List;

/**
 * @author Stu Baurmann
 */
public interface JobSpaceStub extends ThalamentSpaceStub {
	/**
	 * 
	 * @param jl
	 */
//	public void addJobListener(JobListener jl);
	/**
	 * 
	 * @param jl
	 */
	//public void removeJobListener(JobListener jl);
	/**
	 * 
	 * @param j
	 */
	public void postManualJob(JobStub j);
	/**
	 * 
	 * @param j
	 */
	public void clearJob(JobStub j);
	public void terminateAndClearJob(JobStub j);
	/**
	 * 
	 * @return
	 */
	//  public JobConfig getJobConfig();
	/**
	 * 
	 * @return
	 */
	public List<JobStub> getJobListCopy();

	public <JT extends JobStub> List<JT> getAllJobsMatchingClass(Class<JT> clazz);
	public <JT extends JobStub> void terminateAndClearJobsInClass(Class<JT> jtClass);


}
