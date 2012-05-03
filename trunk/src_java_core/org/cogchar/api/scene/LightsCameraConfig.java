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
package org.cogchar.api.scene;



import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;
import com.hp.hpl.jena.rdf.model.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.appdapter.bind.rdf.jena.assembly.DynamicCachingComponentAssembler;
import org.appdapter.bind.rdf.jena.model.AssemblerUtils;
import org.appdapter.core.item.Item;
import org.appdapter.core.item.ItemFuncs;
import org.appdapter.core.component.KnownComponentImpl;
import org.appdapter.core.log.BasicDebugger;

/**
 * Used to enclose data from RDF camera and lights configuration
 * currently in charWorldConfig.ttl
 * 
 * @author Ryan Biggs
 */
public class LightsCameraConfig extends KnownComponentImpl { 
	public	List<CameraConfig>			  myCCs = new ArrayList<CameraConfig>();
                
        public static class Builder extends DynamicCachingComponentAssembler<LightsCameraConfig> {

		public Builder(Resource builderConfRes) {
			super(builderConfRes);
		}

		@Override protected void initExtendedFieldsAndLinks(LightsCameraConfig mlcc, Item configItem, Assembler assmblr, 
						Mode mode) {
			logInfo("CameraConfig.initExtendedFieldsAndLinks()-BEGIN");
                        Set<Item> cameraItems = ItemFuncs.getLinkedItemSet(configItem, SceneConfigNames.P_camera);
			for (Item ji : cameraItems) {
				CameraConfig cc = new CameraConfig(ji);
                                logInfo("Adding CameraConfig in LightsCameraConfig: " + cc);
				mlcc.myCCs.add(cc);
			}
		}
		
		public static void clearCache() { 
			clearCacheFor(Builder.class);
		}
	}
        
	private static String	UNIT_TEST_RDF_PATH 
		= "../org.cogchar.bundle.render.resources/src/main/resources/rk_bind_config/motion/charWorldConfig.ttl";	
		
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
               
}
