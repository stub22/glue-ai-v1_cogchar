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
package org.cogchar.api.humanoid;

import org.appdapter.core.name.Ident;
import org.appdapter.help.repo.SolutionMap;
import org.appdapter.help.repo.RepoClient;
import org.appdapter.help.repo.SolutionHelper;
import org.cogchar.blob.emit.QueryTester;

/**
 * This class serves as a place to hold the humanoid config (right now, the stuff on the "Humanoids" spreadsheet tab)
 * for a single character. It can be updated on graph changes, etc.
 *
 * @author Ryan Biggs <rbiggs@skyriversoftware.com>
 */
public class HumanoidConfig {

	public Ident myCharIdent;
	public String nickname = "";
	public String meshPath;
	public String jointConfigPath;
	public Float[] initialPosition = new Float[3];
	public boolean physicsFlag;

	public HumanoidConfig(RepoClient qi, Ident charIdent, Ident graphIdent) {
		SolutionHelper sh = new SolutionHelper();
		myCharIdent = charIdent;
		SolutionMap solutionMap = qi.getQueryResultMap(HumanoidQueryNames.HUMANOID_QUERY, HumanoidQueryNames.ROBOT_URI_VAR_NAME, graphIdent);
		nickname = sh.getStringFromSolution(solutionMap, charIdent, HumanoidQueryNames.ROBOT_ID_VAR_NAME);
		meshPath = sh.getStringFromSolution(solutionMap, charIdent, HumanoidQueryNames.MESH_PATH_VAR_NAME);
		jointConfigPath = sh.getStringFromSolution(solutionMap, charIdent, HumanoidQueryNames.JOINT_CONFIG_PATH_VAR_NAME);
		for (int i = 0; i < initialPosition.length; i++) {
			initialPosition[i] = sh.getFloatFromSolution(solutionMap, charIdent, HumanoidQueryNames.INITIAL_POSITION_VAR_NAMES[i]);
		}
		physicsFlag = sh.getBooleanFromSolution(solutionMap, charIdent, HumanoidQueryNames.PHYSICS_FLAG_VAR_NAME);
	}
}
