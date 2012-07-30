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
import org.cogchar.blob.emit.Solution;
import org.cogchar.blob.emit.QueryEmitter;

/**
 *
 * @author Ryan Biggs
 */
public class WaypointConfig {

	public String waypointName;
	public float[] waypointCoordinates = {Float.NaN, Float.NaN, Float.NaN};

	@Override
	public String toString() {
		return "WaypointConfig = " + waypointName + ", position = " + Arrays.toString(waypointCoordinates);
	}

	// This constructor is called from within CinematicTrack to correspond to Turtle configured usages of "named" waypoints within CinematicTracks
	// The need for this results from the flexibility of initial Turtle definition: waypoints could be defined inline or as separate
	// Named entities. (Same for tracks and rotations.) We're moving away from this with the spreadsheet config, and can
	// simplify / clean up things if we decide we're permanently doing away with the inline definitions
	public WaypointConfig(Ident ident) {
		waypointName = ident.getLocalName();
	}

	// Called from CinematicConfig, corresponds to a "named" waypoint definition
	public WaypointConfig(Solution solution) {
		Ident myIdent = QueryEmitter.getIdentFromSolution(solution, CinematicQueryNames.WAYPOINT_VAR_NAME);
		waypointName = myIdent.getLocalName();
		for (int index = 0; index < waypointCoordinates.length; index++) {
			waypointCoordinates[index] = QueryEmitter.getFloatFromSolution(solution, CinematicQueryNames.POSITION_VAR_NAME[index], Float.NaN);
		}
	}

	public WaypointConfig(Item configItem) {
		// If this waypoint has no name, it's likely an unnamed waypoint defined in-line with a track definition...
		waypointName = ItemFuncs.getString(configItem, CinematicConfigNames.P_waypointName, CinematicConfigNames.unnamedWaypointName);
		String waypointLocalName = configItem.getIdent().getLocalName();
		// ... or a waypoint with no name may be from a waypoint resource not defined as part of a track
		if (waypointLocalName == null) {
			waypointLocalName = "no dice"; // Keeps expression below from throwing an NPE if waypointLocalName is null, which it is if waypoint is defined within track definition
		}
		if (waypointLocalName.startsWith(CinematicConfigNames.P_namedWaypoint)) {
			waypointName = waypointLocalName;
		}
		for (int index = 0; index < waypointCoordinates.length; index++) {
			waypointCoordinates[index] = ItemFuncs.getDouble(configItem, CinematicConfigNames.P_position[index], Double.NaN).floatValue();
		}
	}
}
