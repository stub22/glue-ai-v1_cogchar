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

package org.cogchar.animoid.protocol;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Stu Baurmann
 */
public class Animation implements Serializable {
	private				String			myName;
	private				Double			myFramePeriodSeconds;
	// Marked transient to reduce the size of Job notifications.
	// Revisit if we start shipping animations again.
	private transient	List<Frame>		myFrameList = new ArrayList<Frame>();
	public Animation(String name, Double framePeriodSeconds) {
		myName = name;
		myFramePeriodSeconds = framePeriodSeconds;
	}
	public String getName() {
		return myName;
	}
	public Double getFramePeriodSeconds() {
		return myFramePeriodSeconds;
	}
	public int getFrameCount() {
		return myFrameList.size();
	}
	public Frame getFrameAt(int idx) {
		return myFrameList.get(idx);
	}
	
	public void appendFrame(Frame f) {
		myFrameList.add(f);
	}
	public void appendEmptyFrames(int quantity) {
		for (int i=0; i < quantity; i++) {
			Frame f = new Frame();
			appendFrame(f);
		}
	}
	/**  Returns all Joints that are used by any frame in this Animation.
	*
	*/ 
	public Set<Joint> getUsedJointSet() {
		HashSet<Joint> usedJoints = new HashSet<Joint>();
		for (Frame f : myFrameList) {
			usedJoints.addAll(f.getUsedJointSet());
		}
		return usedJoints;
	}
	public Frame getPartialFrameInCoordinateSystem(int idx, Set<Joint> jointSet, 
				JointStateCoordinateType queryCT) {
		Frame srcFrame = getFrameAt(idx);
		Frame prevSrcFrame = null;
		if (idx > 0) {
			prevSrcFrame = getFrameAt(idx - 1);
			prevSrcFrame.verifyCoordinateTypeCompatibility(JointStateCoordinateType.FLOAT_ABS_RANGE_OF_MOTION);
		}
		srcFrame.verifyCoordinateTypeCompatibility(JointStateCoordinateType.FLOAT_ABS_RANGE_OF_MOTION);
		Frame resultF = new Frame();
		for (Joint j: jointSet) {
			JointPosition srcJP = srcFrame.getJointPositionForJoint(j);
			if (srcJP == null) {
				throw new RuntimeException("Null src JP for " + j + ", currIdx=" + idx);
			}
			if (queryCT.equals(JointStateCoordinateType.FLOAT_ABS_RANGE_OF_MOTION)) {
				resultF.addPosition(srcJP);
			} else if (queryCT.equals(JointStateCoordinateType.FLOAT_REL_RANGE_OF_MOTION)) {
				// If there is no prev frame, we return an empty frame.
				JointPositionAROM srcJPAR = new JointPositionAROM(srcJP);
				if (prevSrcFrame != null) {
					JointPosition prevSrcJP = prevSrcFrame.getJointPositionForJoint(j);
					if (prevSrcJP == null) {
						throw new RuntimeException("Null previous JP for " + j + ", currIdx=" + idx);
					}
					JointPositionAROM prevSrcJPAR = new JointPositionAROM(prevSrcJP);
					JointPositionRROM relJP = prevSrcJPAR.computeDeltaRelPos(srcJPAR);
					resultF.addPosition(relJP);
				}
			}
		}
		return resultF;
	}
	
}
