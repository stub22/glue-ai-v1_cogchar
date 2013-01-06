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

import java.util.ArrayList;
import java.util.List;
import org.appdapter.core.name.Ident;
import org.appdapter.help.repo.RepoClient;
import org.appdapter.help.repo.Solution;
import org.appdapter.help.repo.SolutionHelper;
import org.appdapter.help.repo.SolutionList;

/**
 * Holds instances of "Thing Animations" from which we can create keyframe-based spatial animations
 *
 * @author Ryan Biggs <rbiggs@hansonrobokind.com>
 */
public class ThingAnimInstanceConfig extends SpatialActionConfig {

	public float duration;
	public List<KeyFrameConfig> myKeyFrameDefinitions = new ArrayList<KeyFrameConfig>();

	@Override
	public String toString() {
		return "ThingAnimInstanceConfig[uriFrag = " + myUri.getAbsUriString() + ", duration = " + Float.toString(duration)
				+ ", # of keyframes: " + myKeyFrameDefinitions.size();
	}

	public ThingAnimInstanceConfig(RepoClient qi, Solution solution, Ident qGraph) {
		SolutionHelper sh = new SolutionHelper();
		myUri = sh.pullIdent(solution, CinemaCN.ANIM_VAR_NAME);
		duration = sh.pullFloat(solution, CinemaCN.DURATION_VAR_NAME, Float.NaN);
		pullAttachedItemAndType(sh, solution);
		SolutionList keyFrameSolutionList = qi.queryIndirectForAllSolutions(CinemaCN.KEYFRAMES_QUERY_TEMPLATE_URI, qGraph,
				CinemaCN.ANIM_INSTANCE_QUERY_VAR_NAME, myUri);
		for (Solution waypointSoln : keyFrameSolutionList.javaList()) {
			float time = sh.pullFloat(waypointSoln, CinemaCN.TIME_VAR_NAME, Float.NaN);
			Ident locationIdent = sh.pullIdent(waypointSoln, CinemaCN.LOCATION_CONFIG_VAR_NAME);
			Ident orientationIdent = sh.pullIdent(waypointSoln, CinemaCN.ORIENTATION_CONFIG_VAR_NAME);
			Ident scaleIdent = sh.pullIdent(waypointSoln, CinemaCN.SCALE_CONFIG_VAR_NAME);
			float scale = sh.pullFloat(waypointSoln, CinemaCN.SCALE_VAR_NAME, Float.NaN);
			myKeyFrameDefinitions.add(new KeyFrameConfig(time, locationIdent, orientationIdent, scaleIdent, scale));
		}
	}

	public class KeyFrameConfig {

		public float myTime;
		public Ident myLocation;
		public Ident myOrientation;
		public Ident myScale;
		public float myScalarScale;

		KeyFrameConfig(float time, Ident locationIdent, Ident orientationIdent, Ident scaleIdent, float scale) {
			myTime = time;
			myLocation = locationIdent;
			myOrientation = orientationIdent;
			myScale = scaleIdent;
			myScalarScale = scale;
		}
		
	}
}
