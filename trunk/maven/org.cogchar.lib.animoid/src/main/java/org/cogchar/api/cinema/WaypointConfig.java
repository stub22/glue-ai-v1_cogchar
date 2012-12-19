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

import java.util.Arrays;
import org.appdapter.core.item.*;
import org.appdapter.core.name.Ident;
import org.appdapter.help.repo.Solution;
import org.appdapter.help.repo.RepoClient;
import org.appdapter.help.repo.SolutionHelper;
//import com.jme3.math.Vector3f; // Not available here


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

	// This constructor is called from within CinematicTrack to correspond to Turtle configured usages of "named" waypoints within CinematicTracks
	// The need for this results from the flexibility of initial Turtle definition: waypoints could be defined inline or as separate
	// Named entities. (Same for tracks and rotations.) We're moving away from this with the spreadsheet config, and can
	// simplify / clean up things if we decide we're permanently doing away with the inline definitions
	public WaypointConfig(Ident ident) {
		myUri = ident;
	}
	
	// For use by goodies in generating cinematics for MOVE actions. Would like to modify this to use Vector3f eventually
	public WaypointConfig(Ident ident, float[] waypointVector) {
		this(ident);
		myCoordinates = waypointVector;
	}

	// Called from CinematicConfig, corresponds to a "named" waypoint definition
	public WaypointConfig(RepoClient qi, Solution solution) {
		SolutionHelper sh = new SolutionHelper();
		myUri = sh.pullIdent(solution, CinemaCN.WAYPOINT_VAR_NAME);
		for (int index = 0; index < myCoordinates.length; index++) {
			myCoordinates[index] = sh.pullFloat(solution, CinemaCN.POSITION_VAR_NAME[index], Float.NaN);
		}
	}
	
	// You'll see this a lot; probably should be refactored into superclass
	public String getName() {
		return myUri.getLocalName();
	}

	/* Depreciated assembler-based constructor; removed since we're now "naming" waypoints by URI instead of string
	public WaypointConfig(Item configItem) {
		// If this waypoint has no name, it's likely an unnamed waypoint defined in-line with a track definition...
		waypointName = ItemFuncs.getString(configItem, CinemaAN.P_waypointName, CinemaAN.unnamedWaypointName);
		String waypointLocalName = configItem.getIdent().getLocalName();
		// ... or a waypoint with no name may be from a waypoint resource not defined as part of a track
		if (waypointLocalName == null) {
			waypointLocalName = "no dice"; // Keeps expression below from throwing an NPE if waypointLocalName is null, which it is if waypoint is defined within track definition
		}
		if (waypointLocalName.startsWith(CinemaAN.P_namedWaypoint)) {
			waypointName = waypointLocalName;
		}
		for (int index = 0; index < myCoordinates.length; index++) {
			myCoordinates[index] = ItemFuncs.getDouble(configItem, CinemaAN.P_position[index], Double.NaN).floatValue();
		}
	}
	*/
}
