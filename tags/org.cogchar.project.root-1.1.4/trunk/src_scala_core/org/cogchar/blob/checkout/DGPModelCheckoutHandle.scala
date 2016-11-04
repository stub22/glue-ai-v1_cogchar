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
import com.hp.hpl.jena.query.{ Dataset, DatasetFactory, Query, QueryExecution, QuerySolution }
import com.hp.hpl.jena.rdf.model.{ Resource, RDFNode, Literal }


import com.hp.hpl.jena.query.{ DatasetAccessor, Dataset, DatasetAccessorFactory }
import com.hp.hpl.jena.rdf.model.{ Model, ModelFactory }

import org.appdapter.bind.rdf.jena.query.JenaArqQueryFuncs_TxAware.Oper

import org.ontoware.rdf2go
import com.hp.hpl.jena

/******************************************************
 * TODO:  This comment below was copied from the prototype impl in FriendU-Checkout.scala, 
 * which used DatasetAccessor directly.  Needs a refresh now that we are using GPortal.
 *-------------------------------------------------------------------------
 * Uses a private in-memory model to hold contents of the checkout.
 * Chose this design because it appears that DatasetAccessor for a *local* dataset returns a modifiable
 * model, which does not fit the idea of "checkout".   So, we go ahead and make sure there is always a
 * local copy, i.e. the checkout, and build up conceptually from there (even tho it means we might make
 * an unnecessary extra copy in the case that the conn is actually remote, which already implies copying).
 *******************************************************/
case class DGPModelCheckoutHandle(private val myGraphId: Ident, private val myConn: DGPortalCheckoutConn)

			extends BasicDebugger with ModelCheckoutHandle {

	val myLocalModel: Model = ModelFactory.createDefaultModel
	// We are very explicit about this type, to avoid confusion with jena model!
	lazy val myReactorModel: rdf2go.model.Model = new rdf2go.impl.jena.ModelImplJena(myLocalModel)

	override def getGraphIdent = myGraphId

	override def refreshCheckout {
		val fromConn: Model = myConn.getJenaModel_Copy(myGraphId)
		myLocalModel.removeAll()
		if (fromConn != null) {
			getLogger().info("Adding {} triples from retrieved model {} to local checkout copy", fromConn.size, myGraphId)
			myLocalModel.add(fromConn)
		} else {
			getLogger().info("No model found at {}, starting from empty local checkout model", myGraphId)
		}
	}
	override def checkinAsAdd: Unit = {
		getLogger().info("Posting {} triples from local checkout copy to {} - as ADD [XACT WRITE]", myLocalModel.size, myGraphId)
		myConn.postJenaModel(myGraphId, myLocalModel)
		getLogger.info("Finished posting ADD")
	}
	override def checkinAsReplace: Unit = {
		getLogger().info("Posting {} triples from local checkout copy to {} - as REPLACE [XACT WRITE]", myLocalModel.size, myGraphId)
		myConn.putJenaModel(myGraphId, myLocalModel)
		getLogger.info("Finished posting REPLACE")
	}
	override def deleteGraph: Unit = {
		getLogger().info("Deleting remote jenaGraph at {}, *without* clearing local model [OOPS NOT IMPL]", myGraphId)
		myConn.deleteJenaGraph(myGraphId)
		getLogger.info("Finished DELETE {}", myGraphId)
	}
	override def getAsOpenReactorModel: rdf2go.model.Model = {
		if (!myReactorModel.isOpen) {
			myReactorModel.open
		}
		myReactorModel
	}

	override def getAsJenaModel = myLocalModel

}