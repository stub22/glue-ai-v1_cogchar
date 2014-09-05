/*
 * Copyright 2013 The Cogchar Project (www.cogchar.org).
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
import org.appdapter.core.name.Ident;
import org.appdapter.fancy.rclient.RepoClient;
import org.appdapter.fancy.query.Solution;
import org.appdapter.fancy.query.SolutionHelper;

/**
 *
 * @author Ryan Biggs <rbiggs@hansonrobokind.com>
 */


public class VectorScaleConfig {
	
	public Ident myUri;
	public float scaleX;
	public float scaleY;
	public float scaleZ;
	
	@Override
	public String toString() {
		return "VectorScaleConfig = " + myUri.getAbsUriString() + 
				"; Scale Vector is (" + scaleX + ", " + scaleY + ", " + scaleZ + ")";
	}
	
	public VectorScaleConfig(RepoClient qi, Solution solution) {
		SolutionHelper sh = new SolutionHelper();
		myUri = sh.pullIdent(solution, CinemaCN.VECTOR_SCALE_VAR_NAME);
		scaleX = sh.pullFloat(solution, CinemaCN.SCALE_X_VAR_NAME, Float.NaN);
		scaleY = sh.pullFloat(solution, CinemaCN.SCALE_Y_VAR_NAME, Float.NaN);
		scaleZ = sh.pullFloat(solution, CinemaCN.SCALE_Z_VAR_NAME, Float.NaN);
	}
	
	public float[] getScaleVector() {
		return new float[]{scaleX, scaleY, scaleZ};
	}
}
