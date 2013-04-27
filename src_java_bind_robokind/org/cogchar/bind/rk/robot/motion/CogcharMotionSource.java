/*
 *  Copyright 2013 by The Cogchar Project (www.cogchar.org).
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
package org.cogchar.bind.rk.robot.motion;

import java.util.ArrayList;
import java.util.List;
import org.robokind.api.motion.utils.RobotMoverFrameSource;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import org.robokind.api.motion.Robot;
import org.robokind.api.motion.blending.FrameSource;
import org.robokind.api.motion.protocol.MotionFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class CogcharMotionSource extends RobotMoverFrameSource {

	static Logger theLogger = LoggerFactory.getLogger(CogcharMotionSource.class);
	private boolean myComputingFlag = false;
	private List<CogcharMotionComputer> myComputers = new ArrayList<CogcharMotionComputer>();

	public CogcharMotionSource(Robot robot) {
		super(robot);
	}

	public synchronized void addJointComputer(CogcharMotionComputer computer) {
		myComputers.add(computer);
	}

	@Override public synchronized MotionFrame getMovements(long currentTimeUTC, long moveLengthMilliSec) {
		myComputingFlag = true;
		for (CogcharMotionComputer c : myComputers) {
			try {
				c.notifySourceComputingCycle(this, currentTimeUTC, moveLengthMilliSec);
			} catch (Throwable t) {
				theLogger.warn("Problem notifying motion computer", t);
			}
		}
		MotionFrame result = super.getMovements(currentTimeUTC, moveLengthMilliSec);
		myComputingFlag = false;
		return result;
	}
	
	public boolean isComputingNow() { 
		return myComputingFlag;
	}

	public static List<CogcharMotionSource> findCogcharMotionSources(BundleContext bunCtx) {

		List<CogcharMotionSource> resList = new ArrayList<CogcharMotionSource>();
		// This classname is used to register motion sources in RobotUtils.
		String svcClsName = FrameSource.class.getName();
		String filter = null; // String.format( "(%s=%s)", Robot.PROP_ID, robotId.toString());
		try {
			ServiceReference[] refs = bunCtx.getServiceReferences(svcClsName, filter);
			if (refs != null) {
				for (ServiceReference ref : refs) {
					Object svcObj = bunCtx.getService(ref);
					if (svcObj instanceof CogcharMotionSource) {
						resList.add((CogcharMotionSource) svcObj);
					} else {
						bunCtx.ungetService(ref);
					}
				}

			}

		} catch (Throwable t) {
			theLogger.error("Problem finding CoghcarMotionSources", t);
		}
		return resList;
	}
}
