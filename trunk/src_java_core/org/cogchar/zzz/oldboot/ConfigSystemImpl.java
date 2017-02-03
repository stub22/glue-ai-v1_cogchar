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

package org.cogchar.zzz.oldboot;

import org.cogchar.zzz.ancient.utility.Parameter;
import org.cogchar.zzz.ancient.utility.Parameters;
import org.cogchar.zzz.ancient.utility.ParametersBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * @author Stu B. <www.texpedient.com>
 */
public class ConfigSystemImpl extends SubsystemImpl {
	private static final org.slf4j.Logger theLogger = org.slf4j.LoggerFactory.getLogger(ConfigSystemImpl.class);

	// All parameters - children of <hriParameters>
	private Parameters myRootParams = null;
	// All children of <hriParameters>/<robots>
	private Parameters myMultipleRobotParams = null;
	// All children of <hriParameters>/<robots>/<PARTICULAR_ROBOT_NAME> <-- replace with <robot name="swami">, etc.
	private Parameters myTargetRobotParams;

	private static String ROBOTS_TAG = "robots";

	public boolean loadRootConfigFile(File rootConfgFile) {
		if (rootConfgFile.exists()) {
			myRootParams = ParametersBuilder.parseXMLParameters(rootConfgFile);
			if (myRootParams != null) {
				myMultipleRobotParams = myRootParams.getParam(ROBOTS_TAG).getChildren();
				if (myMultipleRobotParams != null) {
					return true;
				}
			}
		}
		return false;
	}

	public List<String> getAvailableRobotConfigNames() {
		List<String> resultList = null;
		if (myMultipleRobotParams != null) {
			resultList = new ArrayList<String>();
			Iterator robotParamIterator = myMultipleRobotParams.getIterator();
			while (robotParamIterator.hasNext()) {
				Parameter p = (Parameter) robotParamIterator.next();
				String pName = p.getName();
				resultList.add(pName);
			}
		}
		return resultList;
	}

	public boolean setTargetRobot(String targetRobotName) {
		boolean successFlag = false;
		Parameter targetRobotParamHolder = myMultipleRobotParams.getParam(targetRobotName);
		if (targetRobotParamHolder != null) {
			myTargetRobotParams = targetRobotParamHolder.getChildren();
			successFlag = true;
		} else {
			theLogger.debug("Can't find requested robot config: " + targetRobotName);
		}
		return successFlag;
	}

	public Parameters getTargetRobotParameters() {
		return myTargetRobotParams;
	}
}
