/*
 *  Copyright 2008 Hanson Robotics Inc.
 *  All Rights Reserved.
 */

package org.cogchar.animoid.output;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import org.cogchar.animoid.config.AnimoidConfig;
import org.cogchar.animoid.protocol.Frame;
import org.cogchar.animoid.protocol.JVFrame;
import org.cogchar.animoid.protocol.Joint;
import org.cogchar.animoid.protocol.JointPosition;
import org.cogchar.animoid.protocol.JointPositionAROM;
import org.cogchar.animoid.protocol.JointStateCoordinateType;

/**
 * @author Stu Baurmann, Matt Stevenson
 */
public class VisemeJob extends MotionJob {
	private		int							myCurrentVisemeNumber;
	private		int							myNextVisemeNumber;
	private		Frame<JointPositionAROM>	myCurrentFrame;
	private		Frame<JointPositionAROM>	myNextFrame;
	private		double						myNominalSecPerFrame;
    private     boolean                     isSilent;

	public static boolean extendDuration = false;
	
	public VisemeJob(AnimoidConfig aconf, double secsPerFrame) {
		super(aconf);
		myNominalSecPerFrame = secsPerFrame;
        isSilent = true;
	}
	public void setCurrentVisemeNumber(int cvn) {
		myCurrentVisemeNumber = cvn;
	}
	public void setNextVisemeNumber(int nvn) {
		myNextVisemeNumber = nvn;
	}
	public void setDurationMillisec(long duration) {
		scheduleForIntervalStartingNow(duration);
	}
	public Long getDurationMillisec() {
		Long dur = getSchedDuration();
		if(dur == null){
			dur = 0L;
		}
		return dur;
	}
	@Override
	public String  getTypeString() {
		return "VisemeJob";
	}
	@Override
	public String getContentSummaryString() {
		return "curVisNum=" +  myCurrentVisemeNumber + ", nextVisNum" + myNextVisemeNumber 
					+ ", durationMillisec=" + getDurationMillisec();
	}

	public Frame<JointPositionAROM> getCurrentTargetPosFrame() {
		return myCurrentFrame;
	}

	public void setCurrentTargetPosFrame(Frame<JointPositionAROM> currentFrame) {
		myCurrentFrame = currentFrame;
	}

	public Frame<JointPositionAROM> getNextTargetPosFrame() {
		return myNextFrame;
	}

	public void setNextTargetPosFrame(Frame<JointPositionAROM> nextFrame) {
		myNextFrame = nextFrame;
	}

	@Override
	public Collection<Joint> getCautionJoints() {
		/*if(myCurrentFrame != null){
			return myCurrentFrame.getUsedJointSet();
		}*/
		return new ArrayList<Joint>();
	}

	@Override
	public JVFrame contributeVelFrame(Frame prevPosAbsRomFrame, JVFrame prevVelRomFrame, Set<Joint> cautionJoints) {
		if(this.getStatus() == Status.RUNNING) {
			if (myCurrentFrame == null) {
				return null;
			}
			Double frameRateMsec = myNominalSecPerFrame * 1000;
			if(myCurrentVisemeNumber == 0 && myNextVisemeNumber == 0){
				if(isSilent){
					return null;
				}
				isSilent = true;
				setDurationMillisec(frameRateMsec.longValue());
			}else{
				isSilent = false;
			}
			long durationMsec = getDurationMillisec();
			Double frameCount = durationMsec/frameRateMsec;
			Double currWeight = 0.0;
			Double nextWeight = 1.0;
			if(durationMsec < frameRateMsec){
				currWeight = durationMsec/frameRateMsec;
				currWeight = Math.min(Math.max(currWeight, 0.0), 1.0);
				nextWeight = 1 - currWeight;
			}
			Frame startFrame = new Frame();
			Frame endFrame = new Frame();
			for(JointPositionAROM jp : myCurrentFrame.getAllPositions()){
				Joint j = jp.getJoint();
				//we should start the animation with where we are
				startFrame.addPosition(prevPosAbsRomFrame.getJointPositionForJoint(j));

				// the end frame is the weighted sum of the current and next frame
				JointPosition nextVisemeJP = myNextFrame.getJointPositionForJoint(j);
				JointPosition endJP = JointPosition.weightedSumJointPositions(jp, currWeight, nextVisemeJP, nextWeight);
				endFrame.addPosition(endJP);
			}
			Frame velFrame = Frame.computeDerivativeFrame(
					JointStateCoordinateType.FLOAT_VEL_RANGE_OF_MOTION_PER_SEC,
					startFrame, endFrame, myNominalSecPerFrame);
			if(extendDuration){
				velFrame.multiplyByScalar(1.0/frameCount);
			}
			return JVFrame.makeFrom(velFrame);
		} else {
			return null;
		}
	}
	public void setNominalSecPerFrame(double nominalSecPerFrame) {
		myNominalSecPerFrame = nominalSecPerFrame;
	}
}
