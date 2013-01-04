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

import org.appdapter.core.name.Ident;
import org.appdapter.help.repo.RepoClient;
import org.appdapter.help.repo.Solution;
import org.appdapter.help.repo.SolutionHelper;

/**
 *
 * @author Ryan Biggs
 */
public class RotationConfig {

	public Ident myUri;
	public float rotX;
	public float rotY;
	public float rotZ;
	public float rotMag;
	//public Quaternion rotation; // This would likely be better, but can't do that in lib.animoid without adding dependencies -- does this package really belong here?

	@Override
	public String toString() {
		return "RotationConfig = " + myUri.getAbsUriString() + "; rotation axis is ("
				+ rotX + ", " + rotY + ", " + rotZ + "), magnitude " + (rotMag*180f/Math.PI) + " degrees.";
	}
	
	/* Currently unused; may add back something similar depending on how we decide to link this into Goody system / Robosteps
	// For use by goodies in generating cinematics for MOVE actions. Would like to modify this to use Quaternion eventually
	public RotationConfig(Ident ident, float[] eulerAngles) {
		myUri = ident;
		yaw = eulerAngles[0];
		roll = eulerAngles[1];
		pitch = eulerAngles[2];
	}
	*/
	
	public RotationConfig(RepoClient qi, Solution solution) {
		SolutionHelper sh = new SolutionHelper();
		myUri = sh.pullIdent(solution, CinemaCN.ROTATION_VAR_NAME);
		rotX = sh.pullFloat(solution, CinemaCN.ROT_X_VAR_NAME, Float.NaN);
		rotY = sh.pullFloat(solution, CinemaCN.ROT_Y_VAR_NAME, Float.NaN);
		rotZ = sh.pullFloat(solution, CinemaCN.ROT_Z_VAR_NAME, Float.NaN);
		rotMag = (float) (sh.pullFloat(solution, CinemaCN.ROT_MAG_VAR_NAME, Float.NaN) * Math.PI/180f); // Read in degrees, store in radians
	}
	
	// You'll see this a lot; probably should be refactored into superclass
	public String getName() {
		return myUri.getLocalName();
	}
}
