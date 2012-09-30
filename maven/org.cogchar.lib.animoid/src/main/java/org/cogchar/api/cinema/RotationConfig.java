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

import org.appdapter.core.item.*;
import org.appdapter.core.name.Ident;
import org.appdapter.help.repo.Solution;
import org.appdapter.help.repo.RepoClient;
import org.appdapter.help.repo.SolutionHelper;
import org.cogchar.blob.emit.QueryTester;

/**
 *
 * @author Ryan Biggs
 */
public class RotationConfig {

	public String rotationName;
	public float yaw = Float.NaN;
	public float roll = Float.NaN;
	public float pitch = Float.NaN;

	@Override
	public String toString() {
		return "RotationConfig = " + rotationName + ", yaw = " + yaw + ", roll = " + roll + ", pitch = " + pitch;
	}

	// This constructor is called from within CinematicTrack to correspond to Turtle configured usages of "named" rotations within CinematicTracks
	// The need for this results from the flexibility of initial Turtle definition: rotations could be defined inline or as separate
	// Named entities. (Same for tracks and waypoints.) We're moving away from this with the spreadsheet config, and can
	// simplify / clean up things if we decide we're permanently doing away with the inline definitions
	public RotationConfig(Ident ident) {
		rotationName = ident.getLocalName();
	}

	// Called from CinematicConfig, corresponds to a "named" rotation definition
	public RotationConfig(RepoClient qi, Solution solution) {
		SolutionHelper sh = new SolutionHelper();
		Ident myIdent = sh.pullIdent(solution, CinemaCN.ROTATION_VAR_NAME);
		rotationName = myIdent.getLocalName();
		yaw = sh.pullFloat(solution, CinemaCN.YAW_VAR_NAME, Float.NaN);
		pitch = sh.pullFloat(solution, CinemaCN.PITCH_VAR_NAME, Float.NaN);
		roll = sh.pullFloat(solution, CinemaCN.ROLL_VAR_NAME, Float.NaN);
	}

	public RotationConfig(Item configItem) {
		// If this rotation has no name, it's likely an unnamed rotation defined in-line with a track definition...
		rotationName = ItemFuncs.getString(configItem, CinemaAN.P_rotationName, CinemaAN.unnamedRotationName);
		String rotationLocalName = configItem.getIdent().getLocalName();
		// ... or a rotation with no name may be from a rotation resource not defined as part of a track
		if (rotationLocalName == null) {
			rotationLocalName = "no dice"; // Keeps expression below from throwing an NPE if rotationLocalName is null, which it is if rotation is defined within track definition
		}
		if (rotationLocalName.startsWith(CinemaAN.P_namedRotation)) {
			rotationName = rotationLocalName;
		}
		yaw = ItemFuncs.getDouble(configItem, CinemaAN.P_yaw, Double.NaN).floatValue();
		roll = ItemFuncs.getDouble(configItem, CinemaAN.P_roll, Double.NaN).floatValue();
		pitch = ItemFuncs.getDouble(configItem, CinemaAN.P_pitch, Double.NaN).floatValue();

	}
}
