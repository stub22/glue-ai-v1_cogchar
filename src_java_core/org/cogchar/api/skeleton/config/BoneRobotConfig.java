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

import org.appdapter.gui.assembly.DynamicCachingComponentAssembler;
import org.appdapter.gui.box.KnownComponentImpl;
import org.appdapter.core.item.Item;
import org.appdapter.core.item.ItemFuncs;

import org.appdapter.bind.rdf.jena.model.AssemblerUtils;
import org.appdapter.core.log.BasicDebugger;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;
import com.hp.hpl.jena.rdf.model.Resource;

import java.util.*;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class BoneRobotConfig extends KnownComponentImpl {
	public	String							myRobotName;
	public	List<BoneJointConfig>			myBJCs = new ArrayList<BoneJointConfig>();
	
		
	@Override public String getFieldSummary() {
		return super.getFieldSummary() + ", robotName=" + myRobotName + ", joints=[" + myBJCs + "]";
	}
	
	public static class Builder extends DynamicCachingComponentAssembler<BoneRobotConfig> {

		public Builder(Resource builderConfRes) {
			super(builderConfRes);
		}

		@Override protected void initExtendedFieldsAndLinks(BoneRobotConfig mrc, Item configItem, Assembler assmblr, 
						Mode mode) {
			logInfo("BoneRobotConfig.initExtendedFieldsAndLinks()-BEGIN");
			mrc.myRobotName = ItemFuncs.getString(configItem, BoneConfigNames.P_robotName, null);
			Set<Item> jointItems = ItemFuncs.getLinkedItemSet(configItem, BoneConfigNames.P_joint);
			for (Item ji : jointItems) {
				BoneJointConfig bjc = new BoneJointConfig(ji);
				mrc.myBJCs.add(bjc);
			}
			Collections.sort(mrc.myBJCs, new Comparator() {
				public int compare(Object o1, Object o2) {
					int jointNum1 = ((BoneJointConfig)o1).myJointNum;
					int jointNum2 = ((BoneJointConfig)o1).myJointNum;
					return jointNum1 - jointNum2;
				}
			});
		}
		
		public static void clearCache() { 
			clearCacheFor(Builder.class);
		}
	}	
	private static String	UNIT_TEST_RDF_PATH 
		= "../org.cogchar.bundle.render.resources/src/main/resources/rk_bind_config/motion/bonyRobot_ZenoR50.ttl";	
		
	public static void main(String args[]) {
		BasicDebugger bd = new BasicDebugger();
		bd.logInfo("starting boneRobotConfig test");
		
		String triplesPath = UNIT_TEST_RDF_PATH;
		// AssemblerUtils.ensureClassLoaderRegisteredWithJenaFM(AssemblyTest.class.getClassLoader());
		bd.logInfo("Loading triples from path: " + triplesPath);
		Set<Object> loadedStuff = AssemblerUtils.buildAllObjectsInRdfFile(triplesPath);
		bd.logInfo("Loaded " + loadedStuff.size() + " objects");
		for (Object o : loadedStuff) {
			bd.logInfo("Loaded: " + o);
		}
		bd.logInfo("=====================================================================");
		
	}

}
