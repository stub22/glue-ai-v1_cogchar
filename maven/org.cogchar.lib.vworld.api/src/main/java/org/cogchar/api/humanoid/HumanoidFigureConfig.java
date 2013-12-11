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
 * @author Stu B. <www.texpedient.com>
 */
public class HumanoidFigureConfig {

	public Ident myCharIdent;
	public String myNickname;
	public String myMeshPath;
	public String myDebugSkelMatPath;
	public boolean myPhysicsFlag;
	public HumanoidBoneConfig myBoneConfig;
	public float myInitX, myInitY, myInitZ, myScale;

//	public HumanoidFigureConfig(RepoClient qi, HumanoidConfig hc, RenderConfigEmitter rce, Ident bonyGraphIdent) {
	public HumanoidFigureConfig(RepoClient qi, HumanoidConfig hc, String matPath, Ident bonyGraphIdent) {
		myCharIdent = hc.myCharIdent;
		myNickname = hc.myNickname;
		myMeshPath = hc.myMeshPath;
		myDebugSkelMatPath = matPath;
//		myDebugSkelMatPath = rce.getMaterialPath();
		myBoneConfig = new HumanoidBoneConfig();
		myInitX = hc.myInitialPosition[0];
		myInitY = hc.myInitialPosition[1];
		myInitZ = hc.myInitialPosition[2];
		myScale = hc.myScale;
		myPhysicsFlag = hc.myPhysicsFlag;
		addBoneDescsFromBoneRobotConfig(qi, myCharIdent, bonyGraphIdent, this);
	}

	// A method to add the bone descriptions by querying the bony config resource. Might should live somewhere else,
	// but I haven't figured out where yet. Then again, the bone descs are part of the HumanoidFigureConfig,
	// so why not here?
	private void addBoneDescsFromBoneRobotConfig(RepoClient qi, Ident charIdent, Ident bonyGraphIdent, HumanoidFigureConfig hfc) {
		SolutionHelper sh = new SolutionHelper();
		SolutionList solutionList = qi.queryIndirectForAllSolutions(BoneCN.BONE_NAMES_QUERY_QN, bonyGraphIdent, 
						BoneCN.ROBOT_IDENT_QUERY_VAR, charIdent);
		List<String> boneNames = sh.pullStringsAsJava(solutionList, BoneCN.BONE_NAME_VAR_NAME);
		for (String boneName : boneNames) {
			myBoneConfig.addBoneDesc(boneName);
		}
	}

	public boolean isComplete() {
		return myMeshPath != null;
	}
}
