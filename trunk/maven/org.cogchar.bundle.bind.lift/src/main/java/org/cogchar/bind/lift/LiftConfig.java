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
package org.cogchar.bind.lift;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;
import com.hp.hpl.jena.rdf.model.Resource;
import java.util.*;
import org.appdapter.bind.rdf.jena.assembly.DynamicCachingComponentAssembler;
import org.appdapter.bind.rdf.jena.assembly.AssemblerUtils;
import org.appdapter.core.item.Item;
import org.appdapter.core.item.ItemFuncs;
import org.appdapter.core.component.KnownComponentImpl;
import org.appdapter.core.item.Ident;
import org.appdapter.core.log.BasicDebugger;
import org.cogchar.blob.emit.Solution;
import org.cogchar.blob.emit.SolutionMap;
import org.cogchar.blob.emit.SolutionList;
import org.cogchar.blob.emit.QueryInterface;

/**
 * Used to enclose data from RDF Lift webapp configuration currently in liftConfig.ttl
 *
 * @author Ryan Biggs
 */
public class LiftConfig extends KnownComponentImpl {

	private static final String DEFAULT_TEMPLATE = "12slots";
	public List<ControlConfig> myCCs = new ArrayList<ControlConfig>();
	public String template = DEFAULT_TEMPLATE;
	
	// A new constructor to build CinematicConfig from spreadsheet
	public LiftConfig(QueryInterface qi, Ident configUri) {
		SolutionMap solutionMap = qi.getQueryResultMap(LiftQueryNames.TEMPLATE_QUERY_URI, LiftQueryNames.CONFIG_VAR_NAME);
		String foundTemplate = qi.getStringFromSolution(solutionMap, configUri, LiftQueryNames.TEMPLATE_VAR_NAME);
		if (foundTemplate != null) {
			template = foundTemplate;
		}
		String query = qi.getCompletedQueryFromTemplate(LiftQueryNames.CONTROL_QUERY_TEMPLATE_URI, LiftQueryNames.CONFIG_QUERY_VAR_NAME, configUri);
		SolutionList solutionList = qi.getTextQueryResultList(query);
		for (Solution solution : solutionList.javaList()) {
			myCCs.add(new ControlConfig(qi, solution));
		}
	}

	public static class Builder extends DynamicCachingComponentAssembler<LiftConfig> {

		public Builder(Resource builderConfRes) {
			super(builderConfRes);
		}

		@Override
		protected void initExtendedFieldsAndLinks(LiftConfig mlc, Item configItem, Assembler assmblr,
				Mode mode) {
			logInfo("LiftConfig.initExtendedFieldsAndLinks()-BEGIN");
			mlc.template = ItemFuncs.getString(configItem, LiftConfigNames.P_template, DEFAULT_TEMPLATE);
			Set<Item> controlItems = ItemFuncs.getLinkedItemSet(configItem, LiftConfigNames.P_control);
			for (Item ji : controlItems) {
				ControlConfig cc = new ControlConfig(ji);
				logInfo("Adding ControlConfig in LiftConfig: " + cc);
				mlc.myCCs.add(cc);
			}
		}

		public static void clearCache() {
			clearCacheFor(LiftConfig.Builder.class);
		}
	}
	private static String UNIT_TEST_RDF_PATH = "../org.cogchar.bundle.render.resources/src/main/resources/web/liftConfig.ttl";

	public static void main(String args[]) {
		BasicDebugger bd = new BasicDebugger();
		bd.logInfo("starting LiftConfig test");
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
