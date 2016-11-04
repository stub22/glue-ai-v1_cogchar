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
import org.appdapter.core.name.Ident;
import org.appdapter.fancy.rclient.RepoClient;
import org.appdapter.fancy.query.Solution;
import org.appdapter.fancy.query.SolutionList;



/**
 * Holds a list of "Thing Animations" from which we can create keyframe-based spatial animations
 * 
 * @author Ryan Biggs <rbiggs@hansonrobokind.com>
 */


public class ThingAnimConfig extends SpatialActionSetConfig {

	public ThingAnimConfig(RepoClient qi, Ident qGraph) {
		SolutionList animSolnList = qi.queryIndirectForAllSolutions(CinemaCN.THING_ANIM_QUERY_URI, qGraph);
		for (Solution animSoln : animSolnList.javaList()) {
			mySACs.add(new ThingAnimInstanceConfig(qi, animSoln, qGraph));
		}
	}
	
}
