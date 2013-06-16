/*
 *  Copyright 2013 by The Cogchar Project (www.cogchar.org).
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

package org.cogchar.impl.channel
import scala.collection.mutable.HashMap;

import org.appdapter.core.log.{BasicDebugger, Loggable};
import org.appdapter.core.name.{Ident, FreeIdent};
import org.appdapter.core.item.{Item};
import org.appdapter.help.repo.{RepoClient}

import org.cogchar.api.channel.{GraphChannel}
import org.appdapter.core.matdat.{RepoFabric, RepoSpec, OnlineSheetRepoSpec, DatabaseRepoSpec, FabricBox, RepoClientTester, DirectDerivedGraph, DerivedGraphSpec}
/**
 * @author Stu B. <www.texpedient.com>
 */

class GraphChannelHub(bootRepoClient : RepoClient) {
	val		myGraphChans = new HashMap[Ident, GraphChannel]
	
	// This is here suggestively rather than necessarily so far.
	val		myMainRepoFabric : RepoFabric = new RepoFabric()
	// Can promote this to EnhancedRepoClient when needed.
	var		myMainRepoClient : RepoClient = bootRepoClient
	
	// dg includes a spec which includes a (multi-)TypedResource, which is used as the channel ID.
	// Ta-Da!

	def makeVanillaDGChannel(ddg : DirectDerivedGraph) : GraphChannel = {
		val chanID = ddg.mySpec.myTargetGraphTR
		val dgChan = new ProvidedGraphChan(chanID, ddg)
		myGraphChans.put(chanID, dgChan)
		dgChan
	}
	
	// Next layer is 
	
	
}
