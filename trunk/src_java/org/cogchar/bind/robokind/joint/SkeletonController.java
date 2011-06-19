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
package org.cogchar.bind.robokind.joint;

import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.robokind.motion.ConnectionStatus;
import org.robokind.motion.config.JointConfig;
import org.robokind.motion.serial.SerialControllerConfig;
import org.robokind.motion.serial.SerialJointController;
import org.robokind.utils.config.VersionProperty;
import org.robokind.utils.property.PropertyChangeAction;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class SkeletonController extends SerialJointController<BoneJoint> {

	/**
	 * Controller type version name.
	 */
	public final static String VERSION_NAME = "Cogchar Virtual Skeleton";
	/**
	 * Controller type version number.
	 */
	public final static String VERSION_NUMBER = "0.1";
	/**
	 * Controller type version.
	 */
	public final static VersionProperty VERSION = new VersionProperty(VERSION_NAME, VERSION_NUMBER);
	private final static Logger theLogger = Logger.getLogger(SkeletonController.class.getName());
	private Map<Integer, BoneJoint> myPhysicalMap;

	public SkeletonController(SerialControllerConfig config) {
		super(config);
		myPhysicalMap = new HashMap<Integer, BoneJoint>();
		myChangeMonitor.addAction(JointConfig.PROP_PHYSICAL_ID, new PropertyChangeAction() {

			@Override
			protected void run(PropertyChangeEvent event) {
				changeJointPhysicalId((Integer) event.getOldValue(), (Integer) event.getNewValue());
			}
		});
	}

	@Override protected boolean setJoints() {
		myJoints.clear();
		myJointMap.clear();
		if (myConfig == null) {
			return true;
		}
		Map<Integer, JointConfig> params = myConfig.getJointConfigs();
		if (params == null) {
			return true;
		}
		for (JointConfig param : params.values()) {
			BoneJoint servo = new BoneJoint(param, this);
			int id = servo.getId();
			int pId = servo.getPhysicalId();
			if (myJointMap.containsKey(id) || myPhysicalMap.containsKey(pId)) {
				theLogger.log(Level.WARNING, "Unable to add Joint with duplicate Id - {0}, {1}.",
						new Object[]{id, pId});
				continue;
			}
			myJoints.add(servo);
			myJointMap.put(servo.getId(), servo);
			myPhysicalMap.put(servo.getPhysicalId(), servo);
		}
		return true;
	}

	@Override protected BoneJoint connectJoint(JointConfig jc) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override protected boolean disconnectJoint(int i) {
		throw new UnsupportedOperationException("Not supported yet.");
	}


	@Override public boolean moveJoint(int id) {
		if (ConnectionStatus.CONNECTED != myConnectionStatus) {
			return false;
		}
		if (!containsId(id)) {
			return true;
		}
		
		BoneJoint servo = (BoneJoint) getJoint(id);
		byte physId = (byte) servo.getPhysicalId();
		Integer goal = servo.getAbsoluteGoalPosition();

		if (goal == null) {
			return true;
		} 
		if (!true) {
			theLogger.log(Level.SEVERE, "Cannot move servo {0}, unable to write to serial port {1}", new Object[]{id, myConfig.getPortName()});
			return false;
		}
		return true;
	}

	@Override public boolean moveJoints(Integer... intgrs) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override public boolean moveAllJoints() {
		throw new UnsupportedOperationException("Not supported yet.");

	}
    /**
     * Changes the physical id of the Joint and notifies listeners.
     * @param oldId previous physical id
     * @param newId new physical id
     */
    protected void changeJointPhysicalId(int oldId, int newId){
		BoneJoint joint = myPhysicalMap.remove(oldId);
        myPhysicalMap.put(newId, joint);
        firePropertyChange(PROP_JOINTS, null, myJointMap);
    }
}
