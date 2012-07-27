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
import org.appdapter.core.matdat.SheetRepo;

import org.appdapter.bind.rdf.jena.assembly.AssemblerUtils;
import org.appdapter.core.log.BasicDebugger;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;
import com.hp.hpl.jena.rdf.model.Resource;

import org.cogchar.blob.emit.SolutionList;
import org.cogchar.blob.emit.SolutionMap;
import org.cogchar.blob.emit.QueryEmitter;

import java.util.*;
import org.appdapter.core.item.Ident;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class BoneRobotConfig extends KnownComponentImpl {
	public	String							myRobotName;
	public	List<BoneJointConfig>			myBJCs = new ArrayList<BoneJointConfig>();
	
		
	@Override public String getFieldSummary() {
		return super.getFieldSummary() + ", robotName=" + myRobotName + ", joints=[" + myBJCs + "]";
	}
	
	// A new constructor to build BoneRobotConfig from spreadsheet
	public BoneRobotConfig(Ident bonyConfigIdent) {
		SolutionMap solutionMap = QueryEmitter.getQueryResultMap(BoneQueryNames.ROBOT_NAME_QUERY_URI, BoneQueryNames.ROBOT_URI_VAR_NAME);
		myRobotName = QueryEmitter.getStringFromSolution(solutionMap, bonyConfigIdent, BoneQueryNames.ROBOT_NAME_VAR_NAME);
		String queryString = QueryEmitter.getCompletedQueryFromTemplate(BoneQueryNames.BONE_JOINT_CONFIG_QUERY_TEMPLATE_URI, BoneQueryNames.ROBOT_IDENT_QUERY_VAR, bonyConfigIdent);
		SolutionList solutionList = QueryEmitter.getTextQueryResultList(queryString);
		List<Ident> boneJointConfigIdents = QueryEmitter.getIdentsFromSolutionAsJava(solutionList, BoneQueryNames.BONE_JOINT_CONFIG_INSTANCE_VAR_NAME);
		queryString = QueryEmitter.getQuery(BoneQueryNames.BASE_BONE_JOINT_PROPERTIES_QUERY_TEMPLATE_URI);
		queryString = QueryEmitter.setQueryVar(queryString, BoneQueryNames.ROBOT_IDENT_QUERY_VAR, bonyConfigIdent);
		solutionMap = QueryEmitter.getTextQueryResultMap(queryString, BoneQueryNames.JOINT_URI_VAR_NAME);
		for (Ident jointIdent: boneJointConfigIdents) {
			myBJCs.add(new BoneJointConfig(jointIdent, solutionMap));
		}
	}
	
	// Calling this method before using the constructor above ensures that fresh config is being used, but takes more 
	// time than using the cached repo as occurs by default
	public static void reloadResource() {
		QueryEmitter.reloadSheetRepo(); 
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
					int jointNum2 = ((BoneJointConfig)o2).myJointNum;
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
