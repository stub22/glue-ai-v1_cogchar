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

import org.appdapter.core.name.Ident;
import org.appdapter.core.item.Item;
import org.appdapter.core.item.ItemFuncs;
import org.appdapter.core.matdat.SheetRepo;

import org.appdapter.help.repo.Solution;
import org.appdapter.help.repo.SolutionList;
import org.appdapter.help.repo.SolutionMap;
import org.appdapter.help.repo.QueryInterface;

import org.cogchar.blob.emit.QueryTester;




import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class BoneJointConfig {
	public String						myURI_Fragment;
	public Integer						myJointNum;
	public String						myJointName;
	public Double						myNormalDefaultPos;
	public List<BoneProjectionRange>	myProjectionRanges = new ArrayList<BoneProjectionRange>();

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
	public BoneJointConfig(QueryInterface queryEmitter, Ident jointIdent, SolutionMap solutionMap, Ident graphIdent) {
		myURI_Fragment = jointIdent.getLocalName();
		myJointNum = queryEmitter.getIntegerFromSolution(solutionMap, jointIdent, BoneQueryNames.JOINT_NUM_VAR_NAME);
		myJointName = queryEmitter.getStringFromSolution(solutionMap, jointIdent, BoneQueryNames.JOINT_NAME_VAR_NAME);
		myNormalDefaultPos = queryEmitter.getDoubleFromSolution(solutionMap, jointIdent, BoneQueryNames.DEFAULT_POS_VAR_NAME);
		// What about bc:invertForSymmetry?
		String queryString = queryEmitter.getQuery(BoneQueryNames.BONEPROJECTION_QUERY_TEMPLATE_URI);
		queryString = queryEmitter.setQueryVar(queryString, BoneQueryNames.BONE_JOINT_CONFIG_QUERY_VAR, jointIdent);
		solutionMap = queryEmitter.getTextQueryResultMapByStringKey(queryString, BoneQueryNames.BONE_NAME_VAR_NAME, graphIdent);
		Iterator rangeIterator = solutionMap.getJavaIterator();
		while (rangeIterator.hasNext()) {
			String boneName = (String)rangeIterator.next();
			String rotationAxisName = queryEmitter.getStringFromSolution(solutionMap, boneName, BoneQueryNames.ROTATION_AXIS_VAR_NAME);
			Double minAngle = queryEmitter.getDoubleFromSolution(solutionMap, boneName, BoneQueryNames.MIN_ANGLE_VAR_NAME);
			Double maxAngle = queryEmitter.getDoubleFromSolution(solutionMap, boneName, BoneQueryNames.MAX_ANGLE_VAR_NAME);
			BoneRotationAxis rotationAxis = BoneRotationAxis.valueOf(rotationAxisName);
			myProjectionRanges.add(new BoneProjectionRange(this, boneName, rotationAxis, Math.toRadians(minAngle), Math.toRadians(maxAngle)));
			queryString = queryEmitter.getQuery(BoneQueryNames.ADDITIONAL_BONES_QUERY_TEMPLATE_URI);
			queryString = queryEmitter.setQueryVar(queryString, BoneQueryNames.BONE_JOINT_CONFIG_QUERY_VAR, jointIdent);
			SolutionList solutionList = queryEmitter.getTextQueryResultList(queryString, graphIdent);
			for (Solution solution : solutionList.javaList()) {
				boneName = queryEmitter.getStringFromSolution(solution, BoneQueryNames.BONE_NAME_VAR_NAME, "");
				rotationAxisName = queryEmitter.getStringFromSolution(solution, BoneQueryNames.ROTATION_AXIS_VAR_NAME, "");
				minAngle = queryEmitter.getDoubleFromSolution(solution, BoneQueryNames.MIN_ANGLE_VAR_NAME, 0);
				maxAngle = queryEmitter.getDoubleFromSolution(solution, BoneQueryNames.MAX_ANGLE_VAR_NAME, 0);
				rotationAxis = BoneRotationAxis.valueOf(rotationAxisName);
				myProjectionRanges.add(new BoneProjectionRange(this, boneName, rotationAxis, Math.toRadians(minAngle), Math.toRadians(maxAngle)));
			}
		}
		//System.out.println("Created new BoneJointConfig: " + this.toString()); // TEST ONLY
	}
	
	public String toString() {
		return "BJC[uriFrag=" + myURI_Fragment + ", num=" + myJointNum + ", name=" + myJointName + ", defPos=" + myNormalDefaultPos + ", projs=" + myProjectionRanges + "]";
	}
}
