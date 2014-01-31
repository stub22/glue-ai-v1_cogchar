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
import org.mechio.api.motion.utils.RobotMoverFrameSource;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import org.mechio.api.motion.Robot;
import org.mechio.api.motion.Robot.RobotPositionMap;
import org.mechio.api.motion.blending.FrameSource;
import org.mechio.api.motion.protocol.MotionFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 * Each instance binds to the RK joint-frame protocol for one jointed robot.
 * Holds an ordered list of joint computers, each of which receives one notifySourceComputingCycle() each time
 * we process getMovements().  No protocol among those computers is enforced at this level, we simply presume they
 * will somehow call our "move()" method (inherited from RobotMoverFrameSource) in a sensible way, with the
 * last joint-computer in our collection having the last word.  So, this class provides capability, without policy,
 * and is a good place to begin investigating your own custom low-level (in Cogchar terms) movement planning systems.
 * 
 * Under OSGi (or other JFlux binding), instances of CogcharMotionSource can be found in the registry using
 * findCogcharMotionSources, which can be extended to use robotID as a filter parameter.
 */
public class CogcharMotionSource extends RobotMoverFrameSource {

	static Logger theLogger = LoggerFactory.getLogger(CogcharMotionSource.class);
	private boolean myComputingFlag = false;
	private List<CogcharMotionComputer> myComputers = new ArrayList<CogcharMotionComputer>();

	private Robot.RobotPositionHashMap myGoalPosMap = new Robot.RobotPositionHashMap(), myLastGoalPosMap;
	
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
		super.move(myGoalPosMap, moveLengthMilliSec);
		MotionFrame result = super.getMovements(currentTimeUTC, moveLengthMilliSec);
		// We are imposing a well defined GC burden here, in order to keep a lastGoalPosMap.
		// If we instead just cleared the goalMap in place, we would not force the map itself to be
		// reallocated on each frame.  Either way, we are GC-ing and re-allocing all the positions
		// in the goal map, on each cycle.  So, we have an area for optimization here, once the
		// Cogchar character motion is looking more interesting, and after more of the Symja 
		// possibilities for supplying equivalent/better continuity of state frames are explored.
		myLastGoalPosMap = myGoalPosMap;
		myGoalPosMap = null;
		myComputingFlag = false;
		return result;
	}
	@Override public void move(RobotPositionMap positions, long lenMillisec) {
		if (myGoalPosMap == null) {
			myGoalPosMap = new Robot.RobotPositionHashMap();
		}
		myGoalPosMap.putAll(positions);
	}
	
	public boolean isComputingNow() { 
		return myComputingFlag;
	}

	/**
	 * TODO:  Add robotID filtering parameter.
	 * @param bunCtx
	 * @return 
	 */
	public static List<CogcharMotionSource> findCogcharMotionSources(BundleContext bunCtx, Robot.Id optRobotID) {

		List<CogcharMotionSource> resList = new ArrayList<CogcharMotionSource>();
		// This classname is used to register motion sources in RobotUtils.
		String svcClsName = FrameSource.class.getName();
		String filter = null; 
		if (optRobotID != null) {
			String.format( "(%s=%s)", Robot.PROP_ID, optRobotID.toString());
		}
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
