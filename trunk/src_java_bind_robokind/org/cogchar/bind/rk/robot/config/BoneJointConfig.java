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
package org.cogchar.bind.rk.robot.config;


import org.appdapter.core.item.Item;
import org.appdapter.core.item.ItemFuncs;


import java.util.Set;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class BoneJointConfig {

	public Integer						myJointNum;
	public String						myJointName;
	public Double						myNormalDefaultPos;
	public List<BoneProjectionRange>	myProjectionRanges = new ArrayList<BoneProjectionRange>();

	public BoneJointConfig(Item configItem) {
		myJointNum = ItemFuncs.getInteger(configItem, BoneConfigNames.P_jointNum, null);
		myJointName = ItemFuncs.getString(configItem, BoneConfigNames.P_jointName, null);
		myNormalDefaultPos = ItemFuncs.getDouble(configItem, BoneConfigNames.P_defaultPosNorm, null);
		Set<Item> bprItems = ItemFuncs.getLinkedItemSet(configItem, BoneConfigNames.P_projectionRange);
		
		for (Item bpri : bprItems) {
			BoneProjectionRange bpr = BoneProjectionRange.makeOne(this, bpri);
			myProjectionRanges.add(bpr);
		}
	}
	public String toString() {
		return "BJC[num=" + myJointNum + ", name=" + myJointName + ", defPos=" + myNormalDefaultPos + ", projs=" + myProjectionRanges + "]";
	}
}
