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

package org.cogchar.animoid.calc.estimate;


import org.cogchar.animoid.config.GazeJoint;
import org.cogchar.animoid.config.GazeJointStrategy;
import org.cogchar.animoid.protocol.Frame;
import org.cogchar.animoid.protocol.JointPosition;
import org.cogchar.animoid.world.WorldJointStateSnap;

public class GazeJointStateSnap extends WorldJointStateSnap {

	private GazeJointStrategy	myGazeJointStrategy;

	public GazeJointStateSnap(GazeJointStrategy gjs, JointPosition posJP,
				JointPosition velJP) {
		super(gjs.getGazeJoint(), posJP, velJP);
		myGazeJointStrategy = gjs;
	}
	public GazeJointStateSnap(GazeJointStrategy gjs, Frame posFrame, Frame velFrame) {
		super(gjs.getGazeJoint(), posFrame, velFrame);
		myGazeJointStrategy = gjs;
	}

	public GazeJoint getGazeJoint() {
		return (GazeJoint) getWorldJoint();
	}

	public GazeJointStrategy getGazeJointStrategy() {
		return myGazeJointStrategy;
	}

}
