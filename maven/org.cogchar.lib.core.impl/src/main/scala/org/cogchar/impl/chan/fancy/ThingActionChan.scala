/*
 *  Copyright 2014 by The Cogchar Project (www.cogchar.org).
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

package org.cogchar.impl.chan.fancy 
import org.cogchar.api.channel.{BasicGraphChan}
import org.appdapter.core.name.{Ident, FreeIdent}
import org.appdapter.core.item.{Item}
import org.appdapter.bind.rdf.jena.assembly.ItemAssemblyReader
import com.hp.hpl.jena.assembler.Assembler
import com.hp.hpl.jena.assembler.Mode
import com.hp.hpl.jena.assembler.assemblers.AssemblerBase
import com.hp.hpl.jena.rdf.model.Resource
import org.appdapter.bind.rdf.jena.assembly.KnownComponentImpl
import org.appdapter.bind.rdf.jena.assembly.DynamicCachingComponentAssembler
import org.cogchar.name.dir.{NamespaceDir}
import org.appdapter.core.repo.BoundModelProvider
import org.appdapter.help.repo.RepoClient
import org.cogchar.api.thing.ThingActionSpec
import org.cogchar.impl.thing.basic.BasicThingActionUpdater

//	import org.cogchar.impl.channel.{FancyChannel}

/**
 * @author Stu B. <www.texpedient.com>
 */

// chanID is used as the agent
class ThingActionGraphChan(chanID : Ident, rc : RepoClient, matchGraphID : Ident, cutoffTStamp : Long) 
extends SingleSourceGraphChan(chanID, rc, matchGraphID) {
			
	
	def seeThingActions() : java.util.List[ThingActionSpec] = {
		val tau = new BasicThingActionUpdater();
		val viewingAgentID = chanID;
		/*
		 // This synchronous query call  causes occasional concurrency exceptions -- WHEN it overlaps in time with a SPARQL-Update 
		 // to the same graph.  The exceptions occur during the *view* part (which comes before the "mark" part, which 
		 // if we make it there requires an even more costly write lock) in the iteration over our view-query results.  
		 // As of 2014-06-25 we see these exceptions in practice because we are receiving  HTTP SPARQL-Update requests
		 // from the user's webapp, concurrently with our behavior engine wanting to do this synchronous query against the 
		 // graphStore.   
		 //
		 // Here we are *not* using the async callback pathways (BasicThingActionRouter/Consumer/Updater/etc.).  
		 // Those are properly threaded already, using callbacks from
		 // the graphStore that must complete before the HTTP-Request-processor can send a response and unlock.  So,
		 // that piece of the implementation is working solidly (as far as we know so far), despite being quaint, and,
		 // of course, "temporary"!
		 // 
		 // It is the next line of code calling viewActionsAndMark_Safe
		 // that fails due to concurrency exception.  But, this next line of code is not wrong.  It is good code!
		 // The filtered-query pattern here represents an important part of the way we use our graphStores now and in
		 // the future. 
		 // */ 
		
		val tasList : java.util.List[ThingActionSpec] = tau.viewActionsAndMark_Safe(rc, matchGraphID, cutoffTStamp, viewingAgentID);
		
		/*		 // The actual concurrency problem we have at the moment occurs because of concurrent read/write to just one
		 // crucial model.  You guessed it, "the" runtime-TA model!  But i a more complex scenario there can be any
		 // number of such models, and thus any number of values for matchGraphID on this line of code.
		
		 // Starting with the management of this model, we need to deploy a more robust async+concurrent
		 // infrastructure, using our strategic technologies: JMS/QPid/AMQP, TDB, ARQ.
		 // We want the way we solve our present concurrency problem to fit with that vision and preferably step towards
		 // it rather than move laterally.
		 */

		tasList;
	}
	
	
}


class RealThingActionChanSpec  extends KnownComponentImpl {
	var		myDetails : String = "EMPTY";
	var   mySourceModel : Ident = null;
  
	override def getFieldSummary() : String = {
		return super.getFieldSummary() + ", details=" + myDetails + ", sourec graph=" + mySourceModel;
	}
	def completeInit(configItem : Item, reader : ItemAssemblyReader, assmblr : Assembler , mode: Mode) {
		val sourceModelPropID = reader.getConfigPropertyIdent(configItem, configItem.getIdent(), NamespaceDir.NS_CCRT_RT + "sourceModel");
		val linkedSourceModels : java.util.Set[Item] = configItem.getLinkedItemSet(sourceModelPropID, Item.LinkDirection.FORWARD);
		
		getLogger().debug("ThingActionChanSpec has linkedSourceModels: {} ",  linkedSourceModels);
		if (linkedSourceModels.size() == 1) {
			mySourceModel =  linkedSourceModels.iterator.next.asInstanceOf[Ident]
		}		
		myDetails = reader.readConfigValString(configItem.getIdent(), NamespaceDir.NS_CCRT_RT + "details", configItem, null);		
	}	

}

class RealThingActionChanSpecBuilder(builderConfRes : Resource) extends DynamicCachingComponentAssembler[RealThingActionChanSpec](builderConfRes) {

	override protected def initExtendedFieldsAndLinks(cs: RealThingActionChanSpec, configItem : Item, assmblr : Assembler , mode: Mode ) {
		getLogger().debug("ThingActionChanSpecBuilder.initExtendedFieldsAndLinks using {}", configItem);
		val reader = getReader();
		cs.completeInit(configItem, reader, assmblr, mode)
	}
}


/*     [java] 98060  ERROR [Service Manager Thread - 16] (AssemblerUtils.java:93) buildAllRootsInModel - Cannot assemble item http://www.cogchar.org/schema/scene/instance#Flow-TA-Test-TAChan-C1
 [java] com.hp.hpl.jena.assembler.exceptions.AssemblerException: caught: null
 [java]   doing:
 [java]     root: http://www.cogchar.org/schema/scene/instance#Flow-TA-Test-TAChan-C1 with type: urn:ftd:cogchar.org:2012:runtime#BuildableTAChanSpec assembler class: class org.cogchar.impl.channel.ThingActionChanSpecBuilder
 [java] 
 [java] 	at com.hp.hpl.jena.assembler.assemblers.AssemblerGroup$PlainAssemblerGroup.openBySpecificType(AssemblerGroup.java:138)
 [java] 	at com.hp.hpl.jena.assembler.assemblers.AssemblerGroup$PlainAssemblerGroup.open(AssemblerGroup.java:117)
 [java] 	at com.hp.hpl.jena.assembler.assemblers.AssemblerGroup$ExpandingAssemblerGroup.open(AssemblerGroup.java:81)
 [java] 	at org.appdapter.bind.rdf.jena.assembly.AssemblerUtils.buildAllRootsInModel(AssemblerUtils.java:90)
 [java] 	at org.appdapter.bind.rdf.jena.assembly.AssemblerUtils.buildAllRootsInModel(AssemblerUtils.java:100)
 [java] 	at org.appdapter.core.store.BasicRepoImpl.assembleRootsFromNamedModel(BasicRepoImpl.java:318)
 [java] 	at org.appdapter.help.repo.RepoClientImpl.assembleRootsFromNamedModel(RepoClientImpl.scala:111)
 [java] 	at org.cogchar.outer.behav.demo.TAGraphChanWiringDemo.loadTAChanSpecs(TAGraphChanWiringDemo.java:61)
 [java] 	at org.cogchar.outer.behav.demo.TAGraphChanWiringDemo.loadAndRegisterSpecs(TAGraphChanWiringDemo.java:45)
 [java] 	at com.rkbots.demo.behavior.master.BehaviorMasterLifecycle.create(BehaviorMasterLifecycle.java:47)
 [java] 	at com.rkbots.demo.behavior.master.BehaviorMasterLifecycle.create(BehaviorMasterLifecycle.java:22)
 */