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
import org.appdapter.help.repo.SolutionList;

/**
 * This class serves as a place to hold the humanoid config (right now, the stuff on the "Humanoids" spreadsheet tab)
 * for a single character. It can be updated on graph changes, etc.
 *
 * @author Ryan Biggs <rbiggs@skyriversoftware.com>
 */
public class HumanoidConfig {

	public Ident myCharIdent;
	public String myNickname = "";
	public String myMeshPath;
	public String myJointConfigPath;
	public Float[] myInitialPosition = new Float[3];
	public boolean myPhysicsFlag;

	public HumanoidConfig(RepoClient qi, Ident charIdent, Ident graphIdent) {
		SolutionHelper sh = new SolutionHelper();
		myCharIdent = charIdent;
		
		SolutionList sList = qi.queryIndirectForAllSolutions(HumanoidCN.HUMANOID_QUERY, graphIdent);
		SolutionMap solutionMap  = sList.makeSolutionMap(HumanoidCN.ROBOT_URI_VAR_NAME);
		myNickname = sh.pullString(solutionMap, charIdent, HumanoidCN.ROBOT_ID_VAR_NAME);
		myMeshPath = sh.pullString(solutionMap, charIdent, HumanoidCN.MESH_PATH_VAR_NAME);
		myJointConfigPath = sh.pullString(solutionMap, charIdent, HumanoidCN.JOINT_CONFIG_PATH_VAR_NAME);
		for (int i = 0; i < myInitialPosition.length; i++) {
			myInitialPosition[i] = sh.pullFloat(solutionMap, charIdent, HumanoidCN.INITIAL_POSITION_VAR_NAMES[i]);
		}
		myPhysicsFlag = sh.pullBoolean(solutionMap, charIdent, HumanoidCN.PHYSICS_FLAG_VAR_NAME);
	}
	@Override public String toString() { 
		return "HumanoidConfig[charId=" + myCharIdent + ", nickname=" + myNickname + ", meshPath=" + myMeshPath 
				+ ", jointConfigPath=" + myJointConfigPath + ", initialPos=" + myInitialPosition  
				+ ", physics=" + myPhysicsFlag + "]";
	}
}
