/*
 *  Copyright 2012 by The Cogchar Project (www.cogchar.org).
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.http://www.paroscientific.com/Aerospace.htm

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
// This is just a guess as to where this should live at this point
package org.cogchar.api.cinema;

import org.cogchar.name.cinema.LightsCameraCN;
import java.util.ArrayList;
import java.util.List;
import org.appdapter.bind.rdf.jena.assembly.KnownComponentImpl;
import org.appdapter.core.name.Ident;
import org.appdapter.fancy.rclient.RepoClient;
import org.appdapter.fancy.query.Solution;
import org.appdapter.fancy.query.SolutionList;
import org.appdapter.fancy.query.SolutionHelper;

import org.slf4j.Logger;


/**
 * Used to enclose data from RDF camera and lights configuration currently in charWorldConfig.ttl
 *
 * @author Ryan Biggs
 */
public class LightsCameraConfig extends KnownComponentImpl {
	
	private Logger myLogger = getLoggerForClass(this.getClass());

	public List<CameraConfig> myCCs = new ArrayList<CameraConfig>();
	public List<LightConfig> myLCs = new ArrayList<LightConfig>();
	 // Sets default background color equivalent to ColorRGBA.LightGray if not specified in repo
	public float[] backgroundColor = new float[]{0.8f, 0.8f, 0.8f, 1f};

	// A new constructor to build LightsCameraConfig from spreadsheet
	public LightsCameraConfig(RepoClient queryEmitter, Ident qGraph) {
		SolutionList solutionList = queryEmitter.queryIndirectForAllSolutions(LightsCameraCN.CAMERA_QUERY_URI, qGraph);
		for (Solution cameraSolution : solutionList.javaList()) {
			myCCs.add(new CameraConfig(cameraSolution));
		}
		solutionList = queryEmitter.queryIndirectForAllSolutions(LightsCameraCN.LIGHT_QUERY_URI, qGraph);
		for (Solution lightSolution : solutionList.javaList()) {
			myLCs.add(new LightConfig(queryEmitter, lightSolution));
		}
		try {
			solutionList = queryEmitter.queryIndirectForAllSolutions(LightsCameraCN.BACKGROUND_COLOR_QUERY_URI, qGraph);
			List<Solution> javaSolnList = solutionList.javaList();
			int listSize = javaSolnList.size();
			if (listSize == 0) {
				myLogger.info("No background color specified, using default");
			} else {
				if (listSize > 1) {
					myLogger.warn("Found multiple background color specifications; ignoring all but one!");
				}
				// Seems usually the first item is read from the repo last, so if there are multiple color specifications
				// listed, the last one in the list is most likely to be the first in the spreadsheet
				Solution bgColorSoln = javaSolnList.get(listSize-1);
				SolutionHelper sh = new SolutionHelper();
				for (int i=0; i<backgroundColor.length; i++) {
					backgroundColor[i] = sh.pullFloat(bgColorSoln, LightsCameraCN.COLOR_VAR_NAME[i], backgroundColor[i]);
				}
			}
		} catch (Exception e) { // Could more specifically look for com.hp.hpl.jena.query.QueryParseException, but probably general catching is useful
			myLogger.warn("Problem querying for background color; using default", e);
		}
				
	}
	
	/* Assembler config not currently supported -- see CameraConfig for more info
	public static class Builder extends DynamicCachingComponentAssembler<LightsCameraConfig> {

		public Builder(Resource builderConfRes) {
			super(builderConfRes);
		}

		@Override
		protected void initExtendedFieldsAndLinks(LightsCameraConfig mlcc, Item configItem, Assembler assmblr,
				Mode mode) {
			logInfo("LightsCameraConfig.initExtendedFieldsAndLinks()-BEGIN");
			Set<Item> cameraItems = ItemFuncs.getLinkedItemSet(configItem, LightsCameraConfigNames.P_camera);
			for (Item ji : cameraItems) {
				CameraConfig cc = new CameraConfig(ji);
				logInfo("Adding CameraConfig in LightsCameraConfig: " + cc);
				mlcc.myCCs.add(cc);
			}
			Set<Item> lightItems = ItemFuncs.getLinkedItemSet(configItem, LightsCameraConfigNames.P_light);
			for (Item ji : lightItems) {
				LightConfig lc = new LightConfig(ji);
				logInfo("Adding LightConfig in LightsCameraConfig: " + lc);
				mlcc.myLCs.add(lc);
			}
		}

		public static void clearCache() {
			clearCacheFor(Builder.class);
		}
	}
	private static String UNIT_TEST_RDF_PATH = "../org.cogchar.bundle.render.resources/src/main/resources/rk_bind_config/motion/charWorldConfig.ttl";

	public static void main(String args[]) {
		BasicDebugger bd = new BasicDebugger();
		bd.logInfo("starting LightsCameraConfig test");
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
	*/ 

}
