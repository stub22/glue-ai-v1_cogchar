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

package org.cogchar.blob.emit
import org.appdapter.core.name.{Ident, FreeIdent}
import org.appdapter.core.store.{Repo}
import org.appdapter.help.repo.{QueryInterface, QueryEmitter} 
import org.appdapter.impl.store.{FancyRepo};
import org.appdapter.core.matdat.{SheetRepo}
import com.hp.hpl.jena.query.{Query, QueryFactory, QueryExecution, QueryExecutionFactory, QuerySolution, QuerySolutionMap, Syntax};
import com.hp.hpl.jena.rdf.model.{Model}


/** Provided solely for testing of queries
 *
 */
object QueryTester {
	def main(args: Array[String]) : Unit = {
		val QUERY_TO_TEST = "ccrt:find_users_99"
		testQuery(QUERY_TO_TEST)
	}
  
	final val SHEET_KEY = "0ArBjkBoH40tndDdsVEVHZXhVRHFETTB5MGhGcWFmeGc" // Main test sheet!
	//final val SHEET_KEY = "0Ajj1Rnx7FCoHdDN2VFdVazMzRGNGY3BMQmk1TXZzUHc" // Biggs test sheet!
	//final val SHEET_KEY = "0AlpQRNQ-L8QUdDNWQXpmSW9iNzROcHktZEJZdTJhY2c" // Workshop v010_004 test sheet
	//final val SHEET_KEY = "0AlpQRNQ-L8QUdGx2RkhDX1VEWklrS256cEVOcy0yb2c" // Workshop v010_005 test sheet
	final val NS_SHEET_NUM = 9
	final val DIR_SHEET_NUM = 8
	final val QUERY_SHEET = "ccrt:qry_sheet_22"
	final val GRAPH_QUERY_VAR = "qGraph"
	var myRepo: FancyRepo = null;
	var myQueryEmitter: QueryEmitter = null;
  
	/** Provided solely for testing of queries
	 *
	 */
	def testQuery(queryToTest: String) : Unit = {
	
		val sr : SheetRepo = loadSheetRepo
		val qText = sr.getQueryText(QUERY_SHEET, queryToTest)
		println("Found query text: " + qText)
		
		val parsedQ = sr.parseQueryText(qText);
		val solnJavaList : java.util.List[QuerySolution] = sr.findAllSolutions(parsedQ, null);
		println("Found solutions: " + solnJavaList)
	}
  
	// Modeled on SheetRepo.loadTestSheetRepo
	def loadSheetRepo : SheetRepo = {
		val dirModel : Model = SheetRepo.readDirectoryModelFromGoog(SHEET_KEY, NS_SHEET_NUM, DIR_SHEET_NUM) 
		val sr = new SheetRepo(dirModel)
		sr.loadSheetModelsIntoMainDataset()
		sr
	}
  	def makeVanillaQueryEmitter() : QueryEmitter = {
		val repo = loadSheetRepo;
		new QueryEmitter(repo, GRAPH_QUERY_VAR, QUERY_SHEET)		
	}
	// A temporary hook-in to allow current clients of QueryEmitter to easily get a "primary" instance until they 
	// start using the managed service version - really this should happen in a registry but this is a short-term fix
	def getInterface : QueryInterface = {
		getEmitter
	}
	// makeVanillaQueryEmitter creates a QueryEmitter, not a QueryInterface per se. Just for the moment, let's store this
	// instead. This will allow PumaBooter.startVanillaQueryInterface to use the same instance of the QueryEmitter for which
	// this.getInterface returns an interface. In turn, that prevents (SLOW) duplicate resource loading and the possibility for 
	// unsynchronized state within PUMA.
	// Really we might rather not expose the emitter, but rather only the interface. That sounds cleaner, but we need to be
	// able to access an instance of the emitter to be able to start a managed service as in PumaBooter.startVanillaQueryInterface.
	// So either we might have to accept exposing the QueryEmitter instance in this way, or move the functionality of 
	// PumaBooter.startVanillaQueryInterface to here or to this class' successor. -Ryan Biggs 16 Sept 2012
	def getEmitter : QueryEmitter = {
		if (myQueryEmitter == null) {
			  myQueryEmitter = makeVanillaQueryEmitter;
		  }
		  myQueryEmitter
	}
  
	def clearQueryInterface : Unit = {
		myQueryEmitter = null;
	}
}
