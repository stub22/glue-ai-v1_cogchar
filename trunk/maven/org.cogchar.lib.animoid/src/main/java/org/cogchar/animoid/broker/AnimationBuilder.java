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

package org.cogchar.animoid.broker;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


import org.cogchar.api.animoid.protocol.Animation;
import org.cogchar.api.animoid.protocol.Frame;
import org.cogchar.api.animoid.protocol.Joint;
import org.cogchar.api.animoid.protocol.JointPosition;
import org.cogchar.api.animoid.protocol.JointStateCoordinateType;
import org.cogchar.api.animoid.protocol.Robot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class AnimationBuilder {
	private static Logger	theLogger = LoggerFactory.getLogger(AnimationBuilder.class.getName());

	public static Animation makeConstantAnimation(String name,
			int numFrames, List<JointPosition> jointPositions, double secondsPerFrame) {
		Frame f = makeConstantFrame(jointPositions);
		return makeConstantAnimation(name, numFrames, f, secondsPerFrame);
	}
	public static Animation makeConstantAnimation(String name,
			int numFrames, Frame constFrame, double secondsPerFrame) {
		Animation anim = new Animation(name, secondsPerFrame);
		for(int i=0; i<numFrames; i++){
			anim.appendFrame(constFrame);
		}
		return anim;
	}
	public static Frame makeConstantFrame(List<JointPosition> jointPositions) {
			Frame f = new Frame();
			for(JointPosition jp : jointPositions){
				f.addPosition(jp.copy());
			}
			return f;
	}
	public static JointPosition makeAROM_JointPosition(Joint j, double aromPos) {
		JointPosition jointPos = null;
		jointPos = new JointPosition(j);
		jointPos.setCoordinateFloat(JointStateCoordinateType.FLOAT_ABS_RANGE_OF_MOTION, aromPos);
		return jointPos;
	}

	public static JointPosition makeCenteringJointPosition(Robot r, String jointName) {
		/* now unused */
		Joint j = r.getJointForName(jointName);
		JointPosition centerJP = j.getCenterPosition();
		return centerJP;
	}

	public static Frame makeRobotCenteringFrame(Robot r) {
		Frame frame = new Frame();
		for (Joint j : r.getJoints()) {
			JointPosition centerJP = j.getCenterPosition();
			frame.addPosition(centerJP);
		}
		return frame;
	}
	public static Animation makeRobotCenteringAnimation(Robot r, String animName,
				int numFrames, double secondsPerFrame) {
		Frame robotCenteringFrame = makeRobotCenteringFrame(r);
		Animation anim = makeConstantAnimation(animName, numFrames, robotCenteringFrame, secondsPerFrame);
		return anim;
	}
	public static Animation makeLinearAnimation(String name, Frame start, 
			Frame end, int numFrames){
		Set<Joint> usedJoints = start.getUsedJointSet();
		if(!Frame.verifySameJointsUsed(start, end)){
			theLogger.error("Differing joints in Start Frame and End Frame.  " +
					"Using only common joints");
			usedJoints.retainAll(end.getUsedJointSet());
		}
		List<List<JointPosition>> framePositions = new ArrayList<List<JointPosition>>();
		for(Joint j : usedJoints){
			JointPosition startPos = start.getJointPositionForJoint(j);
			JointPosition endPos = end.getJointPositionForJoint(j);
			framePositions.add(getJointSteps(startPos, endPos, numFrames));
		}
		Animation a = new Animation(name, 1.0);
		for(int i=0; i<numFrames; i++){
			Frame f = new Frame();
			for(int j=0; j<usedJoints.size(); j++){
				f.addPosition(framePositions.get(j).get(i));
			}
			a.appendFrame(f);
		}
		return a;
	}
	public static List<JointPosition> getJointSteps(JointPosition startPos, 
			JointPosition endPos, Integer numSteps)
	{
		JointStateCoordinateType flAbsRoM = JointStateCoordinateType.FLOAT_ABS_RANGE_OF_MOTION;
		List<JointPosition> positions = new ArrayList<JointPosition>();
		startPos = startPos.convertToCooordinateType(flAbsRoM);
		endPos = endPos.convertToCooordinateType(flAbsRoM);
		double start = startPos.getCoordinateFloat(flAbsRoM);
		double end = endPos.getCoordinateFloat(flAbsRoM);
		double range = end - start;
		double step = range/numSteps.doubleValue();
		for(int i=0; i<numSteps; i++){
			double thisStep = getJointStep(step, i, start);
			JointPosition jp = startPos.copy();
			jp.setCoordinateFloat(flAbsRoM, thisStep);
			positions.add(jp);
		}
		return positions;
	}
	// If needed we can add other step functions to build non-linear animations
	public static double getJointStep(double step, int i, double start){
		return start + step*i;
	}
}
