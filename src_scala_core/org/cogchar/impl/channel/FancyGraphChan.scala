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

import org.cogchar.api.channel.{BasicGraphChan}
import org.appdapter.core.name.{Ident, FreeIdent}
import org.appdapter.core.store.{Repo, InitialBinding }
import org.appdapter.help.repo.{RepoClient} 
import com.hp.hpl.jena.query.{QuerySolution} // Query, QueryFactory, QueryExecution, QueryExecutionFactory, , QuerySolutionMap, Syntax};
import com.hp.hpl.jena.rdf.model.{Model}
import org.cogchar.api.thing.{ThingActionSpec}
import org.cogchar.impl.thing.basic.{BasicThingActionUpdater}
/**
 * @author Stu B. <www.texpedient.com>
 */

import org.appdapter.core.matdat.{SheetRepo, BoundModelProvider, RepoFabric, RepoSpec, OnlineSheetRepoSpec, DatabaseRepoSpec, FabricBox, DirectDerivedGraph, DerivedGraphSpec, _}

class ProvidedGraphChan(chanID : Ident, val myModelProvider : BoundModelProvider) extends BasicGraphChan(chanID) with FancyChannel {
	
}
class FancyGraphChan(chanID : Ident, val myRepoClient : RepoClient) extends BasicGraphChan(chanID) with FancyChannel {
	
}
class SingleSourceGraphChan(chanID : Ident, rc : RepoClient, val myMatchGraphID : Ident) extends FancyGraphChan(chanID, rc) {
}

class TypePollingGraphChan(chanID : Ident, rc : RepoClient, matchGraphID : Ident, matchTypeID : Ident) 
		extends SingleSourceGraphChan(chanID, rc, matchGraphID) {
	
}
// chanID is used as the agent
class ThingActionGraphChan(chanID : Ident, rc : RepoClient, matchGraphID : Ident, cutoffTStamp : Long) 
		extends SingleSourceGraphChan(chanID, rc, matchGraphID) {
			
	
	def seeThingActions() : java.util.List[ThingActionSpec] = {
		val tau = new BasicThingActionUpdater();
		val viewingAgentID = chanID;
		val tasList : java.util.List[ThingActionSpec] = tau.viewActionsAndMark(rc, matchGraphID, cutoffTStamp, viewingAgentID);
		tasList;
	}
	
	
}

import org.appdapter.core.name.{Ident, FreeIdent};
import org.appdapter.core.item.{Item};
import org.appdapter.bind.rdf.jena.assembly.ItemAssemblyReader;
import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;
import com.hp.hpl.jena.assembler.assemblers.AssemblerBase;
import com.hp.hpl.jena.rdf.model.Resource;


class ThingActionChanSpec  extends FancyChannelSpec {
	var		myDetails : String = "EMPTY";
	override def getFieldSummary() : String = {
		return super.getFieldSummary() + ", details=" + myDetails;
	}
	override def completeInit(configItem : Item, reader : ItemAssemblyReader, assmblr : Assembler , mode: Mode) {
		super.completeInit(configItem, reader, assmblr, mode)
		// myDetails = reader.readConfigValString(configItem.getIdent(), PerfChannelNames.P_details, configItem, null);
	}

}