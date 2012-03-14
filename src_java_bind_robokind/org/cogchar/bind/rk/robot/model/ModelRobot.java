/*
 *  Copyright 2011-2 by The Cogchar Project (www.cogchar.org).
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
package org.cogchar.bind.rk.robot.model;

import java.beans.PropertyChangeListener;
import org.cogchar.bind.rk.robot.config.BoneProjectionPosition;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import org.cogchar.bind.rk.robot.config.BoneJointConfig;
import org.cogchar.bind.rk.robot.config.BoneRobotConfig;
import org.robokind.api.common.position.NormalizedDouble;
import org.robokind.api.motion.AbstractRobot;
import org.robokind.api.motion.Joint;
import org.robokind.api.motion.Joint.Id;
import org.robokind.api.motion.JointProperty;
import org.robokind.api.motion.Robot;
import org.robokind.api.motion.Robot.RobotPositionMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class ModelRobot extends AbstractRobot<ModelJoint> {
	
	static Logger theLogger = LoggerFactory.getLogger(ModelRobot.class);
   // private List<BoneProjectionPosition> myInitialBoneRotations;
    private boolean myConnectionFlag;
	private long myLastMoveStampMillis = System.currentTimeMillis();
	
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
    /*
    protected void setInitialBoneRotations(List<BoneProjectionPosition> rotations){
        myInitialBoneRotations = rotations;
    }
    
    public List<BoneProjectionPosition> getInitialBoneRotations(){
        return myInitialBoneRotations;
    }
	 * 
	 */
	protected String getDescription() { 
		return "ROBOT[" + getRobotId() + "]";
	}
	@Override public boolean connect() {
        myConnectionFlag = true;
		theLogger.info(getDescription() + " connecting");
		return true;
	}

	@Override public void disconnect() {
        myConnectionFlag = false;
		theLogger.info(getDescription() + " disconnecting");
	}


	@Override public void move(RobotPositionMap positions, long lenMillisec) {
        long now = System.currentTimeMillis();
        long elapsed = now - myLastMoveStampMillis;
		//theLogger.info(getDescription() + " moving to: " + 
        //        positions + ", over " + lenMillisec + " milliseconds. "
        //        + "elapsed: " + elapsed + ", current time: " + System.currentTimeMillis());
        myLastMoveStampMillis = now;
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
		theLogger.info(getDescription() + " registering " + bj.getDescription());
		addJoint(bj);
	}

    @Override public boolean isConnected() {
        return myConnectionFlag;
    }
	public void updateConfig(BoneRobotConfig config) {
		for (BoneJointConfig bjc : config.myBJCs) {
			Joint.Id jointJointID = new Joint.Id(bjc.myJointNum); 
			JointId robotJointID = new JointId(getRobotId(), jointJointID);
			ModelJoint mj = getJoint(robotJointID);
			if (mj != null) {
				theLogger.info("Updating robot joint: " + robotJointID);
				mj.updateConfig(bjc);
			}
		}		
	}
}
