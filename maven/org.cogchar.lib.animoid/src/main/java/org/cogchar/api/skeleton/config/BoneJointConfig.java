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
package org.cogchar.api.skeleton.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.appdapter.core.component.KnownComponentImpl;
import org.appdapter.core.item.Item;
import org.appdapter.core.item.ItemFuncs;
import org.appdapter.core.name.Ident;
import org.appdapter.help.repo.*;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class BoneJointConfig extends KnownComponentImpl{
	public String						myURI_Fragment;
	public Integer						myJointNum;
	public String						myJointName;
	public Double						myNormalDefaultPos;
	public List<BoneProjectionRange>	myProjectionRanges = new ArrayList<BoneProjectionRange>();

	// Original assembler constructor
	public BoneJointConfig(Item configItem) {
		myURI_Fragment = configItem.getIdent().getLocalName();
		myJointNum = ItemFuncs.getInteger(configItem, BoneConfigNames.P_jointNum, null);
		myJointName = ItemFuncs.getString(configItem, BoneConfigNames.P_jointName, null);
		myNormalDefaultPos = ItemFuncs.getDouble(configItem, BoneConfigNames.P_defaultPosNorm, null);
		Set<Item> bprItems = ItemFuncs.getLinkedItemSet(configItem, BoneConfigNames.P_projectionRange);
		
		for (Item bpri : bprItems) {
			BoneProjectionRange bpr = BoneProjectionRange.makeOne(this, bpri);
			myProjectionRanges.add(bpr);
		}
	}
	
	// This constructor is used to build BoneJointConfig from queries
	public BoneJointConfig(RepoClient qi, Ident jointIdent, SolutionMap solutionMap, Ident graphIdent) {
		SolutionHelper sh = new SolutionHelper();
		myURI_Fragment = jointIdent.getLocalName();
		myJointNum = sh.getIntegerFromSolution(solutionMap, jointIdent, BoneQueryNames.JOINT_NUM_VAR_NAME);
		myJointName = sh.getStringFromSolution(solutionMap, jointIdent, BoneQueryNames.JOINT_NAME_VAR_NAME);
		myNormalDefaultPos = sh.getDoubleFromSolution(solutionMap, jointIdent, BoneQueryNames.DEFAULT_POS_VAR_NAME);
		// What about bc:invertForSymmetry?
		String queryString = qi.getQuery(BoneQueryNames.BONEPROJECTION_QUERY_TEMPLATE_URI);
		queryString = qi.setQueryVar(queryString, BoneQueryNames.BONE_JOINT_CONFIG_QUERY_VAR, jointIdent);
		SolutionList solutionList = qi.getTextQueryResultList(queryString, graphIdent);
		if (solutionList.javaList().size() == 1) {
			Solution projRangeSoln = solutionList.javaList().get(0);
			String boneName = sh.getStringFromSolution(projRangeSoln, BoneQueryNames.BONE_NAME_VAR_NAME);
			String rotationAxisName = sh.getStringFromSolution(projRangeSoln, BoneQueryNames.ROTATION_AXIS_VAR_NAME);
			Double minAngle = sh.getDoubleFromSolution(projRangeSoln, BoneQueryNames.MIN_ANGLE_VAR_NAME, 0);
			Double maxAngle = sh.getDoubleFromSolution(projRangeSoln, BoneQueryNames.MAX_ANGLE_VAR_NAME, 0);
			BoneRotationAxis rotationAxis = BoneRotationAxis.valueOf(rotationAxisName);
			myProjectionRanges.add(new BoneProjectionRange(this, boneName, rotationAxis, Math.toRadians(minAngle), Math.toRadians(maxAngle)));
			queryString = qi.getQuery(BoneQueryNames.ADDITIONAL_BONES_QUERY_TEMPLATE_URI);
			queryString = qi.setQueryVar(queryString, BoneQueryNames.BONE_JOINT_CONFIG_QUERY_VAR, jointIdent);
			solutionList = qi.getTextQueryResultList(queryString, graphIdent);
			for (Solution solution : solutionList.javaList()) {
				boneName = sh.getStringFromSolution(solution, BoneQueryNames.BONE_NAME_VAR_NAME, "");
				rotationAxisName = sh.getStringFromSolution(solution, BoneQueryNames.ROTATION_AXIS_VAR_NAME, "");
				minAngle = sh.getDoubleFromSolution(solution, BoneQueryNames.MIN_ANGLE_VAR_NAME, 0);
				maxAngle = sh.getDoubleFromSolution(solution, BoneQueryNames.MAX_ANGLE_VAR_NAME, 0);
				rotationAxis = BoneRotationAxis.valueOf(rotationAxisName);
				myProjectionRanges.add(new BoneProjectionRange(this, boneName, rotationAxis, Math.toRadians(minAngle), Math.toRadians(maxAngle)));
			}
		} else {
			logWarning("More than one bone projection range definition in primary bone joint config declaration for " 
					+ myURI_Fragment + " -- not initializing projection ranges for this bone joint config");
		}
		//System.out.println("Created new BoneJointConfig: " + this.toString()); // TEST ONLY
	}
	
	@Override
	public String toString() {
		return "BJC[uriFrag=" + myURI_Fragment + ", num=" + myJointNum + ", name=" + myJointName + ", defPos=" + myNormalDefaultPos + ", projs=" + myProjectionRanges + "]";
	}
}
