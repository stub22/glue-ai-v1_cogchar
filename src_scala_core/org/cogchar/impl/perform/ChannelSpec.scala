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

package org.cogchar.impl.perform

import  org.cogchar.api.perform.{Media, Channel, Performance, BasicTextChannel, BasicFramedChannel, BasicTextPerformance, BasicFramedPerformance};

import org.appdapter.api.module.Module.State;
import org.appdapter.core.log.{BasicDebugger};
import org.appdapter.core.name.{Ident, FreeIdent};
import org.appdapter.core.item.{Item};
import org.appdapter.core.component.KnownComponentImpl;
import org.appdapter.bind.rdf.jena.assembly.DynamicCachingComponentAssembler;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;
import com.hp.hpl.jena.assembler.assemblers.AssemblerBase;
import com.hp.hpl.jena.rdf.model.Resource;

// The ChannelSpec has not advanced beyond a URI rendevous point, which SceneSpecs can refer to.
// Through 2012 our behavior demos relied on PUMA to inject actual channels with matching URIs into a Theater.

class ChannelSpec extends KnownComponentImpl {
	var		myDetails : String = "EMPTY";
	var		myOsgiFilterString : String = "NONE";
	var		myChanType : Ident = null;
	
	override def getFieldSummary() : String = {
		return super.getFieldSummary() + ", details=" + myDetails + ", osgiFilteringString=" + myOsgiFilterString + 
				", chanType=" + myChanType;
	}	
}
class ChannelSpecBuilder(builderConfRes : Resource) extends DynamicCachingComponentAssembler[ChannelSpec](builderConfRes) {

	override protected def initExtendedFieldsAndLinks(cs: ChannelSpec, configItem : Item, assmblr : Assembler , mode: Mode ) {
		getLogger().warn("ChannelSpecBuilder.initExtendedFieldsAndLinks");
		val reader = getReader();
		cs.myDetails = reader.readConfigValString(configItem.getIdent(), ChannelNames.P_details, configItem, null);
		cs.myOsgiFilterString = reader.readConfigValString(configItem.getIdent(), ChannelNames.P_osgiFilterString, configItem, null);
		val chanTypePropID = reader.getConfigPropertyIdent(configItem, configItem.getIdent(), ChannelNames.P_channelType);
		val linkedChanTypes : java.util.Set[Item] = configItem.getLinkedItemSet(chanTypePropID);
		
		getLogger().info("ChannelSpec has linkedChanTypes: " + linkedChanTypes);
		if (linkedChanTypes.size() == 1) {
			cs.myChanType =  linkedChanTypes.iterator.next.asInstanceOf[Ident]
		}
	}
}