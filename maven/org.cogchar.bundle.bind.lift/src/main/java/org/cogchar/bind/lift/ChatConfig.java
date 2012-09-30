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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.appdapter.bind.rdf.jena.assembly.AssemblerUtils;
import org.appdapter.bind.rdf.jena.assembly.DynamicCachingComponentAssembler;
import org.appdapter.core.component.KnownComponentImpl;
import org.appdapter.core.name.Ident;
import org.appdapter.core.item.Item;
import org.appdapter.core.item.ItemFuncs;
import org.appdapter.core.log.BasicDebugger;
import org.appdapter.help.repo.SolutionList;
import org.appdapter.help.repo.RepoClient;

/**
 * Used to enclose data from RDF Lift webapp configuration currently in liftConfig.ttl
 * 31 July 2012: Adding capability to init from spreadsheet / query-based config
 *
 * @author Ryan Biggs
 */
public class ChatConfig extends KnownComponentImpl {

	public List<ChatConfigResource> myCCRs = new ArrayList<ChatConfigResource>();
	
	// A new constructor to build ChatConfig from spreadsheet
	// It's probably unnecessary to retain concept of multple config resources, so for now we just create one with our resource solutions
	// Soon may condense this and ChatConfigResource into single class
	public ChatConfig(RepoClient qi, Ident graphIdent) {
		String query = qi.getCompletedQueryFromTemplate(ChatCN.GENRAL_CONFIG_TEMPLATE_URI, ChatCN.CATEGORY_QUERY_VAR_NAME, ChatCN.CATEGORY_URI);
		SolutionList solutionList = qi.getTextQueryResultList(query, graphIdent);
		myCCRs.add(new ChatConfigResource(qi, solutionList));
	}

	public static class Builder extends DynamicCachingComponentAssembler<ChatConfig> {

		public Builder(Resource builderConfRes) {
			super(builderConfRes);
		}

		@Override
		protected void initExtendedFieldsAndLinks(ChatConfig mcc, Item configItem, Assembler assmblr,
				Mode mode) {
			getLogger().debug("ChatConfig.initExtendedFieldsAndLinks()-BEGIN");
			Set<Item> resourceItems = ItemFuncs.getLinkedItemSet(configItem, ChatConfigNames.P_config);
			for (Item ji : resourceItems) {
				ChatConfigResource ccr = new ChatConfigResource(ji);
				getLogger().debug("Adding ChatConfigResource in ChatConfig: {} ", ccr);
				mcc.myCCRs.add(ccr);
			}
		}

		public static void clearCache() {
			clearCacheFor(ChatConfig.Builder.class);
		}
	}
	private static String UNIT_TEST_RDF_PATH = "metadata/chatbird/cogbotZenoAmazonEC.ttl";

	public static void main(String args[]) {
		BasicDebugger bd = new BasicDebugger();
		bd.logInfo("starting ChatConfig test");
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
