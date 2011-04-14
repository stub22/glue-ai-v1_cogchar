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
package org.cogchar.animoid.output;



import org.cogchar.animoid.config.AnimoidConfig;
import org.cogchar.platform.stub.JobStub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Stu Baurmann
 */
public abstract class AnimoidJob extends JobStub  {
	private static Logger	theLogger = LoggerFactory.getLogger(AnimoidJob.class.getName());

	private		transient	AnimoidConfig		myAnimoidConfig;
	
	public AnimoidJob(AnimoidConfig aconf) {
		myAnimoidConfig = aconf;
	}
	public AnimoidConfig getAnimoidConfig() {
		return myAnimoidConfig;
	}
	public void enableMotion() {
		theLogger.info("Enabling motion on job: " + this);
		setStatus(Status.RUNNING);
	}
	public void disableMotion() {
		theLogger.info("Disabling motion on job: " + this);
		setStatus(Status.PAUSED);
	}
}
