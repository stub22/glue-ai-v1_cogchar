/*
 * Copyright 2011 The Cogchar Project (www.cogchar.org).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.cogchar.api.cinema;

import org.cogchar.name.cinema.CinemaCN;
import java.util.Arrays;
import org.appdapter.core.name.Ident;
import org.appdapter.fancy.rclient.RepoClient;
import org.appdapter.fancy.query.Solution;
import org.appdapter.fancy.query.SolutionHelper;



/**
 *
 * @author Ryan Biggs
 */
public class WaypointConfig {

	public Ident myUri;
	public float[] myCoordinates = {Float.NaN, Float.NaN, Float.NaN};
	//public Vector3f myCoordinates; // This would likely be better, but can't do that in lib.animoid without adding dependencies -- does this package really belong here?

	@Override
	public String toString() {
		return "WaypointConfig = " + myUri.getAbsUriString() + ", position = " + Arrays.toString(myCoordinates);
	}
	
	/*
	// For use by goodies in generating cinematics for MOVE actions. Would like to modify this to use Vector3f eventually
	public WaypointConfig(Ident ident, float[] waypointVector) {
		this(ident);
		myCoordinates = waypointVector;
	}
	*/

	public WaypointConfig(RepoClient qi, Solution solution) {
		SolutionHelper sh = new SolutionHelper();
		myUri = sh.pullIdent(solution, CinemaCN.WAYPOINT_VAR_NAME);
		for (int index = 0; index < myCoordinates.length; index++) {
			myCoordinates[index] = sh.pullFloat(solution, CinemaCN.POSITION_VAR_NAME[index], Float.NaN);
		}
	}
	
	public WaypointConfig(Ident newIdent, float[] position) {
		myUri = newIdent;
		myCoordinates = position;
	}
	
	// You'll see this a lot; probably should be refactored into superclass
	public String getName() {
		return myUri.getLocalName();
	}
}
