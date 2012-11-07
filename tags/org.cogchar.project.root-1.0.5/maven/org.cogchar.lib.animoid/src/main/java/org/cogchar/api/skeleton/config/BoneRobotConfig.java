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

import org.appdapter.bind.rdf.jena.assembly.DynamicCachingComponentAssembler;
import org.appdapter.core.component.KnownComponentImpl;
import org.appdapter.core.item.Item;
import org.appdapter.core.item.ItemFuncs;


import org.appdapter.bind.rdf.jena.assembly.AssemblerUtils;
import org.appdapter.core.log.BasicDebugger;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;
import com.hp.hpl.jena.rdf.model.Resource;

import org.appdapter.help.repo.SolutionList;
import org.appdapter.help.repo.SolutionMap;
import org.appdapter.help.repo.RepoClient;


import java.util.*;
import org.appdapter.core.name.Ident;
import org.appdapter.help.repo.SolutionHelper;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class BoneRobotConfig extends KnownComponentImpl {
	public	String							myRobotName;
	public	List<BoneJointConfig>			myBJCs = new ArrayList<BoneJointConfig>();
	
	@Override public String getFieldSummary() {
		return super.getFieldSummary() + ", robotName=" + myRobotName + ", joints=[" + myBJCs + "]";
	}
	
	public BoneRobotConfig(){} // Needed by o.c.bind.rk.aniconv.ui
	
	// A new constructor to build BoneRobotConfig from spreadsheet
	public BoneRobotConfig(RepoClient rc, Ident bonyConfigIdent, Ident graphIdent, BoneCN bqn) {
		SolutionHelper sh = new SolutionHelper();
		getLogger().info("Building BoneRobotConfig via queries for {} using graph {} ", bonyConfigIdent, graphIdent);
		SolutionList robotSL = rc.queryIndirectForAllSolutions(bqn.ROBOT_NAME_QUERY_URI, graphIdent);
		SolutionMap robotSolMap = robotSL.makeSolutionMap(bqn.ROBOT_URI_VAR_NAME);
		myRobotName = sh.pullString(robotSolMap, bonyConfigIdent, bqn.ROBOT_NAME_VAR_NAME);
		SolutionList bjConfSL = rc.queryIndirectForAllSolutions(bqn.BONE_JOINT_CONFIG_QUERY_QN, graphIdent, 
						bqn.ROBOT_IDENT_QUERY_VAR, bonyConfigIdent);
		List<Ident> boneJointConfigIdents = sh.pullIdentsAsJava(bjConfSL, bqn.BONE_JOINT_CONFIG_INSTANCE_VAR_NAME);
		
		SolutionList bjPropSL = rc.queryIndirectForAllSolutions(bqn.BASE_BONE_JOINT_PROPERTIES_QUERY_QN, graphIdent,
						bqn.ROBOT_IDENT_QUERY_VAR, bonyConfigIdent);
		SolutionMap bjPropSM = bjPropSL.makeSolutionMap(bqn.JOINT_URI_VAR_NAME);
		for (Ident jointIdent: boneJointConfigIdents) {
			myBJCs.add(new BoneJointConfig(rc, jointIdent, bjPropSM, graphIdent));
		}
	}
	
	// Calling this method before using the constructor above ensures that fresh config is being used, but takes more 
	// time than using the cached repo as occurs by default
//	public static void reloadResource() {
//		queryEmitter.reloadSheetRepo(); 
//	}
	
	/** Alternate approach to creating a BoeRobotConfig goes thru Jena's Assembler stuff.
	 * 
	 */
	
	public static class Builder extends DynamicCachingComponentAssembler<BoneRobotConfig> {

		public Builder(Resource builderConfRes) {
			super(builderConfRes);
		}

		@Override protected void initExtendedFieldsAndLinks(BoneRobotConfig mrc, Item configItem, Assembler assmblr, 
						Mode mode) {
			getLogger().debug("BoneRobotConfig.initExtendedFieldsAndLinks()-BEGIN");
			mrc.myRobotName = ItemFuncs.getString(configItem, BoneAN.P_robotName, null);
			Set<Item> jointItems = ItemFuncs.getLinkedItemSet(configItem, BoneAN.P_joint);
			for (Item ji : jointItems) {
				BoneJointConfig bjc = new BoneJointConfig(ji);
				mrc.myBJCs.add(bjc);
			}
			Collections.sort(mrc.myBJCs, new Comparator() {
				public int compare(Object o1, Object o2) {
					int jointNum1 = ((BoneJointConfig)o1).myJointNum;
					int jointNum2 = ((BoneJointConfig)o2).myJointNum;
					return jointNum1 - jointNum2;
				}
			});
		}
		
		public static void clearCacheForThisAssemblerType() { 
			clearCacheForAssemblerSubclass(Builder.class);
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
