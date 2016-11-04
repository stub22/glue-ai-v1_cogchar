/*
 *  Copyright 2015 by The Cogchar Project (www.cogchar.org).
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

package org.cogchar.blob.ghost

import org.appdapter.core.name.{ FreeIdent, Ident }
import com.hp.hpl.jena.rdf.model.{ Model, Statement, Resource, RDFNode, Literal }

trait PrefixExpander {
	protected def getPrefixResolutionModel : Model  = ??? // Typically this was done in the past by using dirModel
	
	def expandPrefix(prefixed: String): String = getPrefixResolutionModel.expandPrefix(prefixed)
	def expandResource(prefixed: String) = getPrefixResolutionModel.createResource(expandPrefix(prefixed))
	def expandProperty(prefixed: String) = getPrefixResolutionModel.createProperty(expandPrefix(prefixed))
	def expandIdent(prefixed: String) = new FreeIdent(expandPrefix(prefixed))
}
