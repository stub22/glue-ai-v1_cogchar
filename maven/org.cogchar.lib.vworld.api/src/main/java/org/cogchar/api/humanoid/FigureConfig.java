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
 * This class serves as a place to hold the humanoid config (right now, 
 * the stuff on the "Humanoids" spreadsheet tab)
 * for a single character. It can be updated on graph changes, etc.
 *
 * @author Ryan Biggs <rbiggs@skyriversoftware.com>
 */
public class FigureConfig {

	protected Ident myFigureID;
	protected String myNickname = "";
	protected String myMeshPath;
	protected String myJointGroupConfigPath;
	protected Float[] myInitialPosition = new Float[3];
	protected float myScale;
	protected boolean myPhysicsFlag;

	protected FigureConfig(Ident figureID) {
		myFigureID = figureID;
	}
	protected void copyValuesFrom(FigureConfig other) {
		myNickname = other.myNickname;
		myMeshPath = other.myMeshPath;
		myScale = other.myScale;
		myInitialPosition = other.myInitialPosition;		
		myPhysicsFlag = other.myPhysicsFlag;
		myJointGroupConfigPath = other.myJointGroupConfigPath;
	}
	public FigureConfig(RepoClient qi, Ident figureID, Ident graphIdent) {
		SolutionHelper sh = new SolutionHelper();
		myFigureID = figureID;
		
		SolutionList sList = qi.queryIndirectForAllSolutions(FigureCN.HUMANOID_QUERY, graphIdent);
		SolutionMap solutionMap  = sList.makeSolutionMap(FigureCN.ROBOT_URI_VAR_NAME);
		myNickname = sh.pullString(solutionMap, figureID, FigureCN.ROBOT_ID_VAR_NAME);
		myMeshPath = sh.pullString(solutionMap, figureID, FigureCN.MESH_PATH_VAR_NAME);
		myJointGroupConfigPath = sh.pullString(solutionMap, figureID, FigureCN.JOINT_CONFIG_PATH_VAR_NAME);
		for (int i = 0; i < myInitialPosition.length; i++) {
			myInitialPosition[i] = sh.pullFloat(solutionMap, figureID, FigureCN.INITIAL_POSITION_VAR_NAMES[i]);
		}
		myScale = sh.pullFloat(solutionMap, figureID, FigureCN.SCALE_VAR_NAME);
		myPhysicsFlag = sh.pullBoolean(solutionMap, figureID, FigureCN.PHYSICS_FLAG_VAR_NAME);
	}
	@Override public String toString() { 
		return "FigureConfig[figureId=" + myFigureID + ", nickname=" + myNickname + ", meshPath=" + myMeshPath 
				+ ", jointConfigPath=" + myJointGroupConfigPath + ", initialPos=" + myInitialPosition  
				+ ", physics=" + myPhysicsFlag + "]";
	}
	public Ident getFigureID() { 
		return myFigureID;
	}
	public String getNickname() { 
		return myNickname;
	}
	public String getMeshPath() { 
		return myMeshPath;
	}
	public String getJointGroupConfigPath() { 
		return myJointGroupConfigPath;
	}
	public Float getScale() { 
		return myScale;
	}
	public Boolean getPhysicsFlag() { 
		return myPhysicsFlag;
	}
}
