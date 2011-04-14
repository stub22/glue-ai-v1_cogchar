/*
 *  Copyright 2011 by The Cogchar Project (www.cogchar.org).
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
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
