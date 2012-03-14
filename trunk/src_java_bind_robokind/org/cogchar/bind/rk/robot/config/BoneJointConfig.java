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

import org.robokind.api.common.position.NormalizedDouble;
import org.appdapter.gui.assembly.DynamicCachingComponentAssembler;
import org.appdapter.gui.box.KnownComponentImpl;
import org.appdapter.core.item.Ident;
import org.appdapter.core.item.Item;
import org.appdapter.core.item.ItemFuncs;
import org.appdapter.gui.box.BoxImpl;
import org.appdapter.gui.box.Trigger;
import org.appdapter.core.item.JenaResourceItem;
import org.appdapter.core.item.ModelIdent;
import org.appdapter.gui.box.KnownComponent;
import org.appdapter.gui.box.MutableKnownComponent;
import org.appdapter.bind.rdf.jena.model.AssemblerUtils;


import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;
import com.hp.hpl.jena.rdf.model.Resource;

import java.util.Set;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class BoneJointConfig {

	public Integer						myJointNum;
	public String						myBoneName;
	public Double						myNormalDefaultPos;
	public List<BoneProjectionRange>	myProjectionRanges = new ArrayList<BoneProjectionRange>();

	public BoneJointConfig(Item configItem) {
		myJointNum = ItemFuncs.getInteger(configItem, BoneConfigNames.P_jointNum, null);
		myBoneName = ItemFuncs.getString(configItem, BoneConfigNames.P_boneName, null);
		myNormalDefaultPos = ItemFuncs.getDouble(configItem, BoneConfigNames.P_defaultPosNorm, null);
		Set<Item> bprItems = ItemFuncs.getLinkedItemSet(configItem, BoneConfigNames.P_projectionRange);
		
		for (Item bpri : bprItems) {
			BoneProjectionRange bpr = BoneProjectionRange.makeOne(this, bpri);
			myProjectionRanges.add(bpr);
		}

	}
}
