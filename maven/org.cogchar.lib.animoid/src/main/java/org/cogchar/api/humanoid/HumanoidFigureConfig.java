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
import org.cogchar.api.skeleton.config.BoneQueryNames;
import org.cogchar.blob.emit.RenderConfigEmitter;
import org.appdapter.help.repo.SolutionList;
import org.appdapter.help.repo.QueryInterface;
import org.cogchar.blob.emit.QueryTester;

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
	public float myInitX, myInitY, myInitZ;

	public HumanoidFigureConfig(QueryInterface qi, HumanoidConfig hc, RenderConfigEmitter rce, Ident bonyGraphIdent) {
		myCharIdent = hc.myCharIdent;
		myNickname = hc.nickname;
		myMeshPath = hc.meshPath;
		myDebugSkelMatPath = rce.getMaterialPath();
		myBoneConfig = new HumanoidBoneConfig();
		myInitX = hc.initialPosition[0];
		myInitY = hc.initialPosition[1];
		myInitZ = hc.initialPosition[2];
		myPhysicsFlag = hc.physicsFlag;
		addBoneDescsFromBoneRobotConfig(qi, myCharIdent, bonyGraphIdent, this);
	}

	// A method to add the bone descriptions by querying the bony config resource. Might should live somewhere else,
	// but I haven't figured out where yet. Then again, the bone descs are part of the HumanoidFigureConfig,
	// so why not here?
	private void addBoneDescsFromBoneRobotConfig(QueryInterface qi, Ident charIdent, Ident bonyGraphIdent, HumanoidFigureConfig hfc) {
		String queryString = qi.getQuery(BoneQueryNames.BONE_NAMES_QUERY_TEMPLATE_URI);
		queryString = qi.setQueryVar(queryString, BoneQueryNames.ROBOT_IDENT_QUERY_VAR, charIdent);
		SolutionList solutionList = qi.getTextQueryResultList(queryString, bonyGraphIdent);
		List<String> boneNames = qi.getStringsFromSolutionAsJava(solutionList, BoneQueryNames.BONE_NAME_VAR_NAME);
		for (String boneName : boneNames) {
			myBoneConfig.addBoneDesc(boneName);
		}
	}

	public boolean isComplete() {
		return myMeshPath != null;
	}
}
