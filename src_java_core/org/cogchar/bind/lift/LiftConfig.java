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
import org.appdapter.core.log.BasicDebugger;

/**
 * Used to enclose data from RDF Lift webapp configuration currently in liftConfig.ttl
 *
 * @author Ryan Biggs
 */
public class LiftConfig extends KnownComponentImpl {

	public List<ControlConfig> myCCs = new ArrayList<ControlConfig>();
	//Map<Integer, ControlConfig> myCCsMap = new HashMap<Integer, ControlConfig>();

	public static class Builder extends DynamicCachingComponentAssembler<LiftConfig> {

		public Builder(Resource builderConfRes) {
			super(builderConfRes);
		}

		@Override
		protected void initExtendedFieldsAndLinks(LiftConfig mlc, Item configItem, Assembler assmblr,
				Mode mode) {
			logInfo("LiftConfig.initExtendedFieldsAndLinks()-BEGIN");
			Set<Item> controlItems = ItemFuncs.getLinkedItemSet(configItem, LiftConfigNames.P_control);
			for (Item ji : controlItems) {
				ControlConfig cc = new ControlConfig(ji);
				logInfo("Adding ControlConfig in LiftConfig: " + cc);
				mlc.myCCs.add(cc);
				//mlc.myCCsMap.put(cc.id, cc);
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
