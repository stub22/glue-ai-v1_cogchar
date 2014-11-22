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
import org.appdapter.fancy.gpointer.PointerToTypedGraph
import org.appdapter.fancy.rclient.RepoClient
import org.cogchar.api.thing.ThingActionSpec
import org.cogchar.impl.thing.route.BasicThingActionUpdater

// import org.cogchar.impl.channel.{FancyChannel}

/** These are all graph-reading channel variations contructable using 
*/
class ProvidedGraphChan(chanID : Ident, val myModelProvider : PointerToTypedGraph) extends BasicGraphChan(chanID) with FancyChannel {
	
}
class FancyGraphChan(chanID : Ident, val myRepoClient : RepoClient) extends BasicGraphChan(chanID) with FancyChannel {
	
}
class SingleSourceGraphChan(chanID : Ident, rc : RepoClient, val myMatchGraphID : Ident) extends FancyGraphChan(chanID, rc) {
}

class TypePollingGraphChan(chanID : Ident, rc : RepoClient, matchGraphID : Ident, matchTypeID : Ident) 
		extends SingleSourceGraphChan(chanID, rc, matchGraphID) {
	
}
