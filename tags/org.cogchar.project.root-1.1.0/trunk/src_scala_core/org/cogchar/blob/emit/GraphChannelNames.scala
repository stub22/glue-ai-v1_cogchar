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

package org.cogchar.blob.emit

import org.appdapter.api.module.Module.State;
import org.appdapter.core.log.{BasicDebugger};
import org.appdapter.core.name.{Ident, FreeIdent};
import org.appdapter.core.item.{Item};
import org.appdapter.bind.rdf.jena.assembly.KnownComponentImpl;
import org.appdapter.bind.rdf.jena.assembly.DynamicCachingComponentAssembler;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;
import com.hp.hpl.jena.assembler.assemblers.AssemblerBase;
import com.hp.hpl.jena.rdf.model.Resource;

import org.cogchar.name.dir.NamespaceDir;
import org.cogchar.name.channel.ChannelNames;

/**
 * @author Stu B. <www.texpedient.com>
 */

object GraphChannelNames {
	val		NS_ccScn =	NamespaceDir.NS_ccScn 
	val		NS_ccScnInst = NamespaceDir.NS_ccScnInst 

	val		I_graphPrimary =   "primaryGraph";
	val		I_graphSecondary	=	"secondaryGraph";
	
	private def getChannelIdent(localName : String) : Ident = { 
		val absURI = NS_ccScnInst + localName;
		new FreeIdent(absURI, localName);
	}
	
	def getChanTypeID_graphPrimary() = getChannelIdent(I_graphPrimary)
	def getChanTypeID_graphSecondary() = getChannelIdent(I_graphSecondary)

}
