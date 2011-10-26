/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cogchar.bind.robokind.joint;

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
public class BonyRobot extends AbstractRobot<BonyJoint> {
	static Logger theLogger = LoggerFactory.getLogger(BonyRobot.class);
    private boolean myConnectionFlag;
	
	public static interface MoveListener {
		public void notifyBonyRobotMoved(BonyRobot br);
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

    
	public BonyRobot(Robot.Id robotId) {
		super(robotId);
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

	@Override public void move(RobotPositionMap positions, long lenMillisec) {
		theLogger.info("BonyRobot[" + getRobotId() + "] moving to: " + 
                positions + ", over " + lenMillisec + " milliseconds");
        if(myJointMap == null){
            throw new NullPointerException();
        }
        for(Entry<Robot.JointId, NormalizedDouble> e : positions.entrySet()){
            Robot.JointId id = e.getKey();
            BonyJoint bj = myJointMap.get(id);
            if(bj == null){
				theLogger.warn("ignoring unknown joint id: " + id);
                continue;
            }
            NormalizedDouble pos = e.getValue();
            bj.setGoalPosition(pos);
        }
		notifyMoveListeners();
	}
	public void registerBonyJoint(BonyJoint bj) {
		theLogger.info("BonyRobot[" + getRobotId() + "] registering joint: " + bj);
		addJoint(bj);
	}

    @Override
    public boolean isConnected() {
        return myConnectionFlag;
    }
}
