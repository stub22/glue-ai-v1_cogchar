/*
 *  Copyright 2012 by The Cogchar Project (www.cogchar.org).
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

package org.cogchar.bind.rk.aniconv;

import org.appdapter.help.repo.Solution;
import org.appdapter.help.repo.SolutionHelper;

/**
 *
 * @author Ryan Biggs <rbiggs@hansonrobokind.com>
 */


public class MayaChannelMapping {
	
	String channelName;
	String jointName;
	int jointNum;
	//String boneName;
	//String rotationAxisString;
	double min;
	double max;
	
	MayaChannelMapping(Solution channelSolution, SolutionHelper sh, MayaCN mcn) {
		channelName = sh.pullString(channelSolution, mcn.MAYA_CHANNEL_VAR_NAME);
		jointName = sh.pullString(channelSolution, mcn.JOINT_NAME_VAR_NAME);
		//jointNum = sh.pullInteger(channelSolution, mcn.JOINT_NUM_VAR_NAME, 0); // Really what we want
		jointNum = (int)Math.round(sh.pullDouble(channelSolution, mcn.JOINT_NUM_VAR_NAME, 0)); // A nasty workaround until we get the right flavor of pullInteger
		//boneName = sh.pullString(channelSolution, mcn.BONE_NAME_VAR_NAME);
		//rotationAxisString = sh.pullString(channelSolution, mcn.ROTATION_AXIS_VAR_NAME);
		min = sh.pullDouble(channelSolution, mcn.MINIMUM_VALUE_VAR_NAME, 0);
		max = sh.pullDouble(channelSolution, mcn.MAXIMUM_VALUE_VAR_NAME, 0);
	}
	
	public String toString() {
		return "[Mapping for channel: " + channelName + " drives joint:" + jointName + ", number " + jointNum +
				" using channel min/max values of " + min + "/" + max + "]";
	}
	
}
