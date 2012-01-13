/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cogchar.bind.rk.robot.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import org.robokind.api.common.position.NormalizedDouble;
import org.robokind.api.motion.AbstractRobot;
import org.robokind.api.motion.Robot;
import org.robokind.api.motion.Robot.RobotPositionMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class ModelRobot extends AbstractRobot<ModelJoint> {
	
	static Logger theLogger = LoggerFactory.getLogger(ModelRobot.class);
    private List<ModelBoneRotation> myInitialBoneRotations;
    private boolean myConnectionFlag;
	
	public static interface MoveListener {
		public void notifyBonyRobotMoved(ModelRobot br);
	}
	private	List<MoveListener> myListeners = new ArrayList<MoveListener>();
	
	public void registerMoveListener(MoveListener ml) {
		myListeners.add(ml);
	}
	public void notifyMoveListeners() { 
		for (MoveListener ml : myListeners) {
			ml.notifyBonyRobotMoved(this);
		}
	}

	public ModelRobot(Robot.Id robotId) {
		super(robotId);
	}
    
    protected void setInitialBoneRotations(List<ModelBoneRotation> rotations){
        myInitialBoneRotations = rotations;
    }
    
    public List<ModelBoneRotation> getInitialBoneRotations(){
        return myInitialBoneRotations;
    }

	@Override public boolean connect() {
        myConnectionFlag = true;
		theLogger.info("BonyRobot[" + getRobotId() + "] connecting");
		return true;
	}

	@Override public void disconnect() {
        myConnectionFlag = false;
		theLogger.info("BonyRobot[" + getRobotId() + "] disconnecting");
	}

    private long myLastMove = System.currentTimeMillis();
	@Override public void move(RobotPositionMap positions, long lenMillisec) {
        long now = System.currentTimeMillis();
        long elapsed = now - myLastMove;
		theLogger.info("BonyRobot[" + getRobotId() + "] moving to: " + 
                positions + ", over " + lenMillisec + " milliseconds. "
                + "elapsed: " + elapsed + ", current time: " + System.currentTimeMillis());
        myLastMove = now;
        if(myJointMap == null){
            throw new NullPointerException();
        }
        for(Entry<Robot.JointId, NormalizedDouble> e : positions.entrySet()){
            Robot.JointId id = e.getKey();
            ModelJoint bj = myJointMap.get(id);
            if(bj == null){
				theLogger.warn("ignoring unknown joint id: " + id);
                continue;
            }
            NormalizedDouble pos = e.getValue();
            bj.setGoalPosition(pos);
        }
		notifyMoveListeners();
	}
	public void registerBonyJoint(ModelJoint bj) {
		theLogger.info("BonyRobot[" + getRobotId() + "] registering joint: " + bj);
		addJoint(bj);
	}

    @Override
    public boolean isConnected() {
        return myConnectionFlag;
    }
}
