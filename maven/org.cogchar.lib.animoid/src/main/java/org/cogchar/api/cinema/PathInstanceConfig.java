/*
 *  Copyright 2012 by The Cogchar Project (www.cogchar.org).
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.appdapter.core.name.Ident;
import org.appdapter.help.repo.RepoClient;
import org.appdapter.help.repo.Solution;
import org.appdapter.help.repo.SolutionHelper;
import org.appdapter.help.repo.SolutionList;

/**
 * Holds instances of "Paths" from which we can create Thing MotionPaths/MotionEvents
 * 
 * @author Ryan Biggs <rbiggs@hansonrobokind.com>
 */


public class PathInstanceConfig extends SpatialActionConfig {
	public float duration;
	public String directionType;
	public float[] lookAtDirection = new float[3]; // Vector3f would be ideal but jME dependency not available here. Might move this later and make it so.
	public float tension;
	public boolean cycle;
	public String loopMode;
	public List<WaypointConfig> waypoints = new ArrayList<WaypointConfig>();
	

	@Override
	public String toString() {
		return "PathInstanceConfig[uriFrag = " + myUri.getAbsUriString() + ", duration = " + Float.toString(duration); 
	}
	
	public PathInstanceConfig(RepoClient qi, Solution solution, Ident qGraph) {
		SolutionHelper sh = new SolutionHelper();
		myUri = sh.pullIdent(solution, CinemaCN.PATH_VAR_NAME);
		duration = sh.pullFloat(solution, CinemaCN.DURATION_VAR_NAME, Float.NaN);
		pullAttachedItemAndType(sh, solution);
		directionType = sh.pullIdent(solution, CinemaCN.DIRECTION_TYPE_VAR_NAME).getLocalName();
		for (int index = 0; index < lookAtDirection.length; index++) {
			lookAtDirection[index] = sh.pullFloat(solution, CinemaCN.DIRECTION_VAR_NAME[index], 0f);
		}
		tension = sh.pullFloat(solution, CinemaCN.TENSION_VAR_NAME, 0f);
		cycle = sh.pullBoolean(solution, CinemaCN.CYCLE_VAR_NAME);
		loopMode = sh.pullIdent(solution, CinemaCN.LOOP_MODE_VAR_NAME).getLocalName();
		SolutionList solutionList  = qi.queryIndirectForAllSolutions(CinemaCN.WAYPOINTS_QUERY_TEMPLATE_URI, qGraph, 
					CinemaCN.PATH_INSTANCE_QUERY_VAR_NAME, myUri);
		Map<Integer, Solution> wpMap = pullOrderedStatesFromList(sh, solutionList);
		for (Solution waypointSoln : wpMap.values()) {
			waypoints.add(new WaypointConfig(qi, waypointSoln));
		}
	}
	
	// This could move to SpatialActionConfig if it's needed elsewhere
	final Map<Integer, Solution> pullOrderedStatesFromList(SolutionHelper sh, SolutionList solutionList) {
		Map<Integer, Solution> stateMap = new TreeMap<Integer, Solution>();
		for (Solution wpSolution: solutionList.javaList()) {
			int index = (int)(sh.pullFloat(wpSolution, CinemaCN.SEQUENCE_NUMBER_VAR_NAME, 0f)); // No pullInt in sh!
			stateMap.put(index, wpSolution);
		}
		return stateMap;
	}
	
}
