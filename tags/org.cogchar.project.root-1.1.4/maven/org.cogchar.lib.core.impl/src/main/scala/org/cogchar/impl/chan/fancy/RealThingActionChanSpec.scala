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
// import org.appdapter.core.repo.PointerToGraph
import org.appdapter.fancy.rclient.RepoClient
// import org.cogchar.api.thing.ThingActionSpec
/**
 * @author Stu B. <www.texpedient.com>
 */


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