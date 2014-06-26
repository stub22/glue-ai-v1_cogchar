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

package org.cogchar.impl.chan.fancy

import org.appdapter.bind.rdf.jena.assembly.{DynamicCachingComponentAssembler, ItemAssemblyReader, KnownComponentImpl}
import org.appdapter.core.item.Item
import org.appdapter.core.name.Ident
import org.cogchar.api.channel.Channel
import org.cogchar.name.channel.ChannelNames

import com.hp.hpl.jena.assembler.{Assembler, Mode}
import com.hp.hpl.jena.rdf.model.Resource
/**
 * @author Stu B. <www.texpedient.com>
 */

trait FancyChannel extends Channel {
	//def getChannelSpec() : FancyChannelSpec
}

// The ChannelSpec has not advanced beyond a URI rendevous point, which SceneSpecs can refer to.
// Through 2012 our behavior demos relied on PUMA to inject actual channels with matching URIs into a Theater.

class RealFancyChannelSpec extends KnownComponentImpl {
	var		myOsgiFilterString : String = "NONE";
	var		myChanType : Ident = null;
	
	override def getFieldSummary() : String = {
		return super.getFieldSummary() + ", osgiFilteringString=" + myOsgiFilterString + 
				", chanType=" + myChanType;
	}
	def getOSGiFilterString() = myOsgiFilterString;
	def getChannelTypeID() = myChanType;
	def getChannelID() = getIdent();
	
	
	def completeInit(configItem : Item, reader : ItemAssemblyReader, assmblr : Assembler , mode: Mode) {
		
		myOsgiFilterString = reader.readConfigValString(configItem.getIdent(), ChannelNames.P_osgiFilterString, configItem, null);
		
		val chanTypePropID = reader.getConfigPropertyIdent(configItem, configItem.getIdent(), ChannelNames.P_channelType);
		val linkedChanTypes : java.util.Set[Item] = configItem.getLinkedItemSet(chanTypePropID, Item.LinkDirection.FORWARD);
		
		getLogger().debug("ChannelSpec has linkedChanTypes: {} ",  linkedChanTypes);
		if (linkedChanTypes.size() == 1) {
			myChanType =  linkedChanTypes.iterator.next.asInstanceOf[Ident]
		}		
	}	
}
class RealFancyChannelSpecBuilder(builderConfRes : Resource) extends DynamicCachingComponentAssembler[RealFancyChannelSpec](builderConfRes) {

	override protected def initExtendedFieldsAndLinks(cs: RealFancyChannelSpec, configItem : Item, assmblr : Assembler , mode: Mode ) {
		getLogger().debug("FancyChannelSpecBuilder.initExtendedFieldsAndLinks using {}", configItem);
		val reader = getReader();
		cs.completeInit(configItem, reader, assmblr, mode)
	}
}