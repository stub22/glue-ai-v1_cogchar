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

import java.util.List;
import org.appdapter.core.name.Ident;
import org.cogchar.name.skeleton.BoneCN;
// import org.cogchar.blob.emit.RenderConfigEmitter;
import org.appdapter.help.repo.SolutionList;
import org.appdapter.help.repo.RepoClient;
import org.appdapter.help.repo.SolutionHelper;

/**
 * This is essentially a wrapper around the HumanoidConfig.
 * TODO:  Let's privatize these variables!
 * @author Stu B. <www.texpedient.com>
 */
public class HumanoidFigureConfig extends FigureConfig {

	private String myDebugSkelMatPath;

	private FigureBoneConfig myFigureBoneConfig;

//	public HumanoidFigureConfig(RepoClient qi, HumanoidConfig hc, RenderConfigEmitter rce, Ident bonyGraphIdent) {
	public HumanoidFigureConfig(RepoClient qi, FigureConfig hc, String matPath, Ident bonyGraphIdent) {
		super(hc.myFigureID);
		copyValuesFrom(hc);
		
		myDebugSkelMatPath = matPath;
//		myDebugSkelMatPath = rce.getMaterialPath();
		myFigureBoneConfig = new FigureBoneConfig();
		

		addBoneDescsFromBoneRobotConfig(qi, myFigureID, bonyGraphIdent, this);
	}

	// A method to add the bone descriptions by querying the bony config resource. Might should live somewhere else,
	// but I haven't figured out where yet. Then again, the bone descs are part of the HumanoidFigureConfig,
	// so why not here?
	private void addBoneDescsFromBoneRobotConfig(RepoClient qi, Ident charIdent, Ident bonyGraphIdent, HumanoidFigureConfig hfc) {
		SolutionHelper sh = new SolutionHelper();
		SolutionList solutionList = qi.queryIndirectForAllSolutions(BoneCN.BONE_NAMES_QUERY_QN, bonyGraphIdent, 
						BoneCN.ROBOT_IDENT_QUERY_VAR, charIdent);
		List<String> boneNames = sh.pullStringsAsJava(solutionList, BoneCN.BONE_NAME_VAR_NAME);
		getLogger().info("Found boneNames {}", boneNames);
		if (boneNames.size() == 0) {
			getLogger().warn("BoneNames result is empty for charID={}", charIdent);
		}
		for (String boneName : boneNames) {
			myFigureBoneConfig.addBoneDesc(boneName);
		}
	}

	public boolean isComplete() {
		return myMeshPath != null;
	}
	public FigureBoneConfig getFigureBoneConfig() {
		return myFigureBoneConfig;
	}
	public Float getInitX() { 
		return myInitialPosition[0];
	}
	public Float getInitY() { 
		return myInitialPosition[1];
	}
	public Float getInitZ() { 
		return myInitialPosition[2];
	}
	public String getDebugSkelMatPath() { 
		return myDebugSkelMatPath;
	}
	
}
