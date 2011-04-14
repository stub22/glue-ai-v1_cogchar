package org.cogchar.animoid.output;

import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import org.cogchar.animoid.calc.estimate.GazeJointStateSnap;
import org.cogchar.animoid.config.AnimoidConfig;
import org.cogchar.animoid.config.GazeJointStrategy;
import org.cogchar.animoid.gaze.GazeStrategyCue;
import org.cogchar.animoid.protocol.Frame;
import org.cogchar.animoid.protocol.JVFrame;
import org.cogchar.animoid.protocol.Joint;
import org.cogchar.animoid.protocol.JointVelocityAROMPS;

/*
 *  Copyright 2008 Hanson Robotics Inc.
 *  All Rights Reserved.
 */

/**
 * @author Stu Baurmann
 * 
 * Typically a RestoringForceJob will also attempt to achieve/keep eyes centered in head.
 */
public class RestoringForceJob extends MotionJob {
	private static Logger	theLogger = Logger.getLogger(RestoringForceJob.class.getName());
	
	public	JVFrame							myLastVelFrame;
	private	GazeStrategyCue					myHoldAndRecenterStrategy;

	public RestoringForceJob(AnimoidConfig aconf) { 
		super(aconf);
	}
	public void setHoldAndRecenterStrategy(GazeStrategyCue harStrat) {
		theLogger.info("Setting holdAndRecenterStrategy to: " + harStrat);
		myHoldAndRecenterStrategy = harStrat;
	}
	@Override public JVFrame contributeVelFrame(
			Frame prevPosAbsRomFrame,
			JVFrame prevVelRomFrame,
			Set<Joint> cautionJoints) {

		JVFrame velFrame = new JVFrame();

        if(this.getStatus() == Status.RUNNING) {
			if (myHoldAndRecenterStrategy != null) {
				List<GazeJointStrategy> gjsList = myHoldAndRecenterStrategy.getJointLinks();
				for (GazeJointStrategy gjs : gjsList) {
					GazeJointStateSnap stat = new GazeJointStateSnap(gjs, prevPosAbsRomFrame, prevVelRomFrame);
					double worldPosDeg = stat.getWorldPosDeg();
					double worldPosMagDeg = Math.abs(worldPosDeg);
					Double recenterSlackDeg = gjs.recenterSlackDeg;
					if (recenterSlackDeg != null) {
						if (worldPosMagDeg < recenterSlackDeg) {
							// theLogger.info("Recentering joint " + stat.getShortDescriptiveName()
							//			+ " worldPosDeg=" + worldPosDeg + " is within slack: "
							//			+ recenterSlackDeg);
							continue;
						}
					}
					Double recenterMaxVelDPS = gjs.recenterMaxVelDPS;
					double secondsPerFrame = getTimeKeeper().getNominalSecPerFrame();
					double oneFrameJumpVelDPS = -1.0 * worldPosDeg  / secondsPerFrame;
					double oneFrameJVMagDPS = Math.abs(oneFrameJumpVelDPS);
					double velDPS = oneFrameJumpVelDPS;
					if ((recenterMaxVelDPS != null) && (oneFrameJVMagDPS > recenterMaxVelDPS)) {
						velDPS = Math.signum(oneFrameJumpVelDPS) * recenterMaxVelDPS;
					}
					double velROM = velDPS / stat.getTotalRomDegrees();
					JointVelocityAROMPS jvel = new JointVelocityAROMPS(stat.getJoint(), velROM);
					velFrame.addPosition(jvel);
				}
            }
        }
		myLastVelFrame = velFrame;
		return velFrame;
	}

    public void setRunning(){
        setStatus(Status.RUNNING);
    }
    public void setPaused(){
        setStatus(Status.PAUSED);
    }
    public void setCompleted(){
        setStatus(Status.COMPLETED);
    }
	@Override public String getContentSummaryString() {
		return "lastVelFrame=" + myLastVelFrame;
	}
}
