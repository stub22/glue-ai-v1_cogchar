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
public class BoneRobotConfig extends KnownComponentImpl {
	public	String							myRobotName;
	public	List<BoneJointConfig>			myBJCs = new ArrayList<BoneJointConfig>();
	
		
	@Override protected String getFieldSummary() {
		return "robotName=[" + myRobotName + "], joints=[" + myBJCs + "]";
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
		}
		
		public static void clearCache() { 
			clearCacheFor(Builder.class);
		}
	}	
	private static String	UNIT_TEST_RDF_PATH 
		= "/P:/_prj/robo/cogchar_trunk/maven/org.cogchar.bundle.render.resources/src/main/resources/rk_bind_config/motion/bonyRobot_ZenoR50.ttl";	
		
	public static void main(String args[]) {
		System.out.println("starting boneRobotConfig test");
		
		String triplesPath = UNIT_TEST_RDF_PATH;
		// AssemblerUtils.ensureClassLoaderRegisteredWithJenaFM(AssemblyTest.class.getClassLoader());
		logInfo("Loading triples from path: " + triplesPath);
		Set<Object> loadedStuff = AssemblerUtils.buildAllObjectsInRdfFile(triplesPath);
		logInfo("Loaded " + loadedStuff.size() + " objects");
		for (Object o : loadedStuff) {
			logInfo("Loaded: " + o);
		}
		logInfo("=====================================================================");
		
	}
	public static void logInfo(String txt) {
		System.out.println(txt);
	}	
}
