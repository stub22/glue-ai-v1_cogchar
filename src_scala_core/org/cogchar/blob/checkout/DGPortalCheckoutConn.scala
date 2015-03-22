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

package org.cogchar.blob.checkout

import org.appdapter.core.name.{ Ident, FreeIdent }
import org.appdapter.core.log.BasicDebugger;

import org.appdapter.fancy.gportal.{ GraphPortal, GraphSupplier, GraphQuerier, QueryParseHelper, SuppliedGraphStat, GraphPortalFuncs, DelegatingPortal }
import org.appdapter.bind.rdf.jena.query.JenaArqQueryFuncs_TxAware.Oper

import org.ontoware.rdf2go
import com.hp.hpl.jena

import jena.rdf.model.{ Model, ModelFactory, Resource, RDFNode, Literal }



class DGPortalCheckoutConn(private val myDGPortal: DelegatingPortal) extends CheckoutConn {

	def getJenaModel_Copy(graphId: Ident): Model = {
		val graphAbsUri = graphId.getAbsUriString
		val op = new Oper[Model]  {
			override def perform : Model = {
				val fetchedModel : Model = myDGPortal.getSupplier.getNamedGraph_Readonly(graphAbsUri)
				val aCopy: Model = ModelFactory.createDefaultModel
				aCopy.add(fetchedModel)
				aCopy
			}
		}
		val copiedResult = myDGPortal.execReadTransCompatible(op, null)
		copiedResult
	}
  

	def putJenaModel(graphId: Ident, srcModel: Model) : Unit = {
		val graphAbsUri = graphId.getAbsUriString
		val op = new Oper[String]  {
			override def perform : String = {
				myDGPortal.getAbsorber.replaceNamedModel(graphAbsUri, srcModel)
				"happy result from PUT"
			}
		}
		val result = myDGPortal.execWriteTransCompatible(op, "sad result from PUT")
	}
	def postJenaModel(graphId: Ident, srcModel: Model) {
		val graphAbsUri = graphId.getAbsUriString
		val op = new Oper[String]  {
			override def perform : String = {
				myDGPortal.getAbsorber.addStatementsToNamedModel(graphAbsUri, srcModel)
				"happy result from POST"
			}
		}
		val result = myDGPortal.execWriteTransCompatible(op, "sad result from POST")
	}
	def deleteJenaGraph(graphId: Ident) {
		val graphAbsUri = graphId.getAbsUriString
		throw new RuntimeException("Not Implemented (Yet?).  Often, one *could* just PUT an empty graph, eh?")
		//	myDsetAcc.deleteModel(graphAbsUri)	
	}
	// This always makes a separate checkout.
	// TODO:  Consider keeping track of existing checkouts, sharing them among callers
	override def makeCheckoutHandle(graphURI: Ident): ModelCheckoutHandle = {
		new DGPModelCheckoutHandle(graphURI, this)
	}
}

