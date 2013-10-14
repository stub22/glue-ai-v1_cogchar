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

package org.cogchar.api.cinema;

import org.cogchar.name.cinema.CinemaCN;
import java.util.HashMap;
import java.util.Map;
import org.appdapter.core.name.Ident;
import org.appdapter.help.repo.RepoClient;
import org.appdapter.help.repo.Solution;
import org.appdapter.help.repo.SolutionList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class holds "waypoints" of locations, rotations, and possibly other parameters for "thing" animation key frames, etc.
 * 
 * @author Ryan Biggs <rbiggs@hansonrobokind.com>
 */

public class AnimWaypointsConfig {
	
	private static Logger theLogger = LoggerFactory.getLogger(AnimWaypointsConfig.class);
	
	public Map<Ident, WaypointConfig> myWCs = new HashMap<Ident, WaypointConfig>();
	public Map<Ident, RotationConfig> myRCs = new HashMap<Ident, RotationConfig>();
	public Map<Ident, VectorScaleConfig> myVSCs = new HashMap<Ident, VectorScaleConfig>();
	
	private static AnimWaypointsConfig mainConfig; // A possibly temporary place to store the "main" waypoint/orientation list
	
	public static void setMainConfig(AnimWaypointsConfig config) {
		mainConfig = config;
		theLogger.info("Main AnimWaypointsConfig set: " + mainConfig);
	}
	public static AnimWaypointsConfig getMainConfig() {
		return mainConfig;
	}
	
	@Override
	public String toString() {
		return "[AnimWaypointsConfig: " + myWCs.size() + " positions, " + myRCs.size() + " rotations, " 
				+ myVSCs.size() + " vector scalings]"; 
	}
	
	public AnimWaypointsConfig(RepoClient qi, Ident qGraph) {
		SolutionList waypointSolList = qi.queryIndirectForAllSolutions(CinemaCN.WAYPOINT_QUERY_URI, qGraph);
		for (Solution waypointSol : waypointSolList.javaList()) {
			WaypointConfig newConfig = new WaypointConfig(qi, waypointSol);
			myWCs.put(newConfig.myUri, newConfig);
		}
		waypointSolList = qi.queryIndirectForAllSolutions(CinemaCN.ROTATION_QUERY_URI, qGraph);
		for (Solution waypointSol : waypointSolList.javaList()) {
			RotationConfig newConfig = new RotationConfig(qi, waypointSol);
			myRCs.put(newConfig.myUri, newConfig);
		}
		waypointSolList = qi.queryIndirectForAllSolutions(CinemaCN.VECTOR_SCALINGS_QUERY_URI, qGraph);
		for (Solution waypointSol : waypointSolList.javaList()) {
			VectorScaleConfig newConfig = new VectorScaleConfig(qi, waypointSol);
			myVSCs.put(newConfig.myUri, newConfig);
		}
	}
}
