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

import com.hp.hpl.jena.rdf.model.{ Resource, RDFNode, Literal }

import org.ontoware.rdf2go
import com.hp.hpl.jena

trait CheckoutConn {
  def makeCheckoutHandle(graphID: Ident): ModelCheckoutHandle

}
trait ModelCheckoutHandle {
  def getGraphIdent: Ident
  def refreshCheckout: Unit
  def checkinAsAdd: Unit
  def checkinAsReplace: Unit
  def deleteGraph: Unit
  // We are very explicit about this type, to avoid confusion with jena model!
  def getAsOpenReactorModel: rdf2go.model.Model;
  def getAsJenaModel: jena.rdf.model.Model;
}
