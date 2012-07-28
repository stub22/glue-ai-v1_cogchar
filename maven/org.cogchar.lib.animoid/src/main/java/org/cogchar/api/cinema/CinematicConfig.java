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
// This is just a guess as to where this should live at this point
package org.cogchar.api.cinema;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;
import com.hp.hpl.jena.rdf.model.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.appdapter.bind.rdf.jena.assembly.DynamicCachingComponentAssembler;
import org.appdapter.bind.rdf.jena.assembly.AssemblerUtils;
import org.appdapter.core.item.Item;
import org.appdapter.core.item.ItemFuncs;
import org.appdapter.core.component.KnownComponentImpl;
import org.appdapter.core.log.BasicDebugger;

/**
 * Used to enclose data from RDF cinematics configuration currently in cinematicConfig.ttl
 *
 * @author Ryan Biggs
 */
public class CinematicConfig extends KnownComponentImpl {

	public List<CinematicInstanceConfig> myCICs = new ArrayList<CinematicInstanceConfig>();
	public List<CinematicTrack> myCTs = new ArrayList<CinematicTrack>();
	public List<WaypointConfig> myWCs = new ArrayList<WaypointConfig>();
	public List<RotationConfig> myRCs = new ArrayList<RotationConfig>();

	public static class Builder extends DynamicCachingComponentAssembler<CinematicConfig> {

		public Builder(Resource builderConfRes) {
			super(builderConfRes);
		}

		@Override
		protected void initExtendedFieldsAndLinks(CinematicConfig mcc, Item configItem, Assembler assmblr,
				Mode mode) {
			logInfo("CinematicConfig.initExtendedFieldsAndLinks()-BEGIN");
			Set<Item> configItems = ItemFuncs.getLinkedItemSet(configItem, CinematicConfigNames.P_cinematic);
			logInfo("Cinematics found: " + configItems.size());
			for (Item ji : configItems) {
				//logInfo("Generating CinematicInstanceConfig"); // TEST ONLY
				CinematicInstanceConfig cic = new CinematicInstanceConfig(ji);
				logInfo("Adding CinematicInstanceConfig in CinematicConfig: " + cic);
				mcc.myCICs.add(cic);
			}
			configItems = ItemFuncs.getLinkedItemSet(configItem, CinematicConfigNames.P_trackList);
			logInfo("Tracks found: " + configItems.size());
			for (Item ji : configItems) {
				//logInfo("Generating CinematicTrack"); // TEST ONLY
				CinematicTrack ct = new CinematicTrack(ji);
				logInfo("Adding named CinematicTrack in CinematicConfig: " + ct);
				mcc.myCTs.add(ct);
			}
			configItems = ItemFuncs.getLinkedItemSet(configItem, CinematicConfigNames.P_waypointList);
			logInfo("Waypoints found: " + configItems.size());
			for (Item ji : configItems) {
				//logInfo("Generating WaypointConfig"); // TEST ONLY
				WaypointConfig wc = new WaypointConfig(ji);
				logInfo("Adding named WaypointConfig in CinematicConfig: " + wc);
				mcc.myWCs.add(wc);
			}
			configItems = ItemFuncs.getLinkedItemSet(configItem, CinematicConfigNames.P_rotationList);
			logInfo("Rotations found: " + configItems.size());
			for (Item ji : configItems) {
				//logInfo("Generating RotationConfig"); // TEST ONLY
				RotationConfig rc = new RotationConfig(ji);
				logInfo("Adding named RotationConfig in CinematicConfig: " + rc);
				mcc.myRCs.add(rc);
			}
		}

		public static void clearCache() {
			clearCacheFor(Builder.class);
		}
	}
	private static String UNIT_TEST_RDF_PATH = "../org.cogchar.bundle.render.resources/src/main/resources/rk_bind_config/motion/cinematicConfig.ttl";

	public static void main(String args[]) {
		BasicDebugger bd = new BasicDebugger();
		bd.logInfo("starting CinematicConfig test");
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
