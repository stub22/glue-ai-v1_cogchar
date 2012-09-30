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
import org.appdapter.help.repo.{RepoClient, RepoClientImpl, InitialBinding} 
import org.appdapter.impl.store.{FancyRepo};
import org.appdapter.core.matdat.{SheetRepo}
import com.hp.hpl.jena.query.{Query, QueryFactory, QueryExecution, QueryExecutionFactory, QuerySolution, QuerySolutionMap, Syntax};
import com.hp.hpl.jena.rdf.model.{Model}


/** Provided solely for testing of queries
 *
 */
object RepoClientTester {
	final val SHEET_KEY = "0ArBjkBoH40tndDdsVEVHZXhVRHFETTB5MGhGcWFmeGc" // Main test sheet!
	//final val SHEET_KEY = "0Ajj1Rnx7FCoHdDN2VFdVazMzRGNGY3BMQmk1TXZzUHc" // Biggs test sheet!
	//final val SHEET_KEY = "0AlpQRNQ-L8QUdDNWQXpmSW9iNzROcHktZEJZdTJhY2c" // Workshop v010_004 test sheet
	//final val SHEET_KEY = "0AlpQRNQ-L8QUdGx2RkhDX1VEWklrS256cEVOcy0yb2c" // Workshop v010_005 test sheet
    //final val SHEET_KEY = "0AlpQRNQ-L8QUdDg0c1dlT2Q1bEVUMDlDbmVvS0ZwLWc" // v011_02
	final val NS_SHEET_NUM = 9
	final val DIR_SHEET_NUM = 8
	final val QUERY_SHEET = "ccrt:qry_sheet_22"
	final val GRAPH_QUERY_VAR = "qGraph"
	// var myRepo: FancyRepo = null;
	// var myQueryEmitter: QueryEmitter = null;

	
	def main(args: Array[String]) : Unit = {
		
		val vqe = makeVanillaQueryEmitter()
		
		val lightsQueryQName = "ccrt:find_lights_99"
		val lightsGraphQName = "ccrt:lights_camera_sheet_22"
		
		testQueryInterface(vqe, lightsQueryQName, lightsGraphQName)
	}
  
	def testQueryInterface(qi : RepoClient, queryQName: String, tgtGraphQName : String) : Unit = {
			
		val repo = qi.getRepo;
		
		// Find the query in this named model (according to Repo directory)
		val querySheetQName = QUERY_SHEET;

		val graphVarName = GRAPH_QUERY_VAR
		
		val qib : InitialBinding = repo.makeInitialBinding
		// Repo reads QNames using its namespaces
		qib.bindQName(graphVarName, tgtGraphQName)
		
		// Run the resulting fully bound query, and print the results.		
		val solnJavaList : java.util.List[QuerySolution] = repo.queryIndirectForAllSolutions(querySheetQName, queryQName, qib.getQSMap);

		println("Found solutions for " + queryQName + " in " + tgtGraphQName + " : " + solnJavaList)
	}
  
	// Modeled on SheetRepo.loadTestSheetRepo
	def loadSheetRepo : SheetRepo = {
		val dirModel : Model = SheetRepo.readDirectoryModelFromGoog(SHEET_KEY, NS_SHEET_NUM, DIR_SHEET_NUM) 
		val sr = new SheetRepo(dirModel)
		sr.loadSheetModelsIntoMainDataset()
		sr
	}
  	def makeVanillaQueryEmitter() : RepoClient = {
		val repo = loadSheetRepo;
		new RepoClientImpl(repo, GRAPH_QUERY_VAR, QUERY_SHEET)		
	}

}
