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
import org.appdapter.core.store.{Repo, InitialBinding }
import org.appdapter.help.repo.{RepoClient, RepoClientImpl, InitialBindingImpl} 
import org.appdapter.impl.store.{FancyRepo};
import org.appdapter.core.matdat.{SheetRepo}
import com.hp.hpl.jena.query.{QuerySolution} // Query, QueryFactory, QueryExecution, QueryExecutionFactory, , QuerySolutionMap, Syntax};
import com.hp.hpl.jena.rdf.model.{Model}


/** Documenting and testing our query-based configuration systems.
 *	// Note that "QName" and QN refers to the prefixed style syntax, which is resolved against
	// the repo's known prefixes.  
 */
object RepoClientTester {

 
	// The first 3 params define the repo:
	
	/**
	The sheet key is used to build a URL for something like a Google-Docs spreadsheet,
	which we read in easily as just CSV (commma-separated values).
	Each tab of the spreadsheet is treated as a separate named graph, which are all
	registered in a directory graph, which can also happen to be in the same spreadsheet.

	// Shared public "CharBootAll" test sheet.  
	// 2012-09-29 : Note that queries were updated to remove "!!", although the latter are now
	// converted to "?" if present, so no query-"templates" should be broken (even in older sheet's).
	// We now use standard SPARQL query text and variable replacement, in all cases.
	*/
	
	final val TEST_REPO_SHEET_KEY = "0ArBjkBoH40tndDdsVEVHZXhVRHFETTB5MGhGcWFmeGc" 
	
	// Here are some other sheets that developers have been using, with this list
	// pointing the way to the next layer of bootstrap integration via PumaContextMediator, et al.

	//final val TEST_REPO_SHEET_KEY = "0Ajj1Rnx7FCoHdDN2VFdVazMzRGNGY3BMQmk1TXZzUHc" // Biggs test sheet!
	//final val TEST_REPO_SHEET_KEY = "0AlpQRNQ-L8QUdDNWQXpmSW9iNzROcHktZEJZdTJhY2c" // Workshop v010_004 test sheet
	//final val TEST_REPO_SHEET_KEY = "0AlpQRNQ-L8QUdGx2RkhDX1VEWklrS256cEVOcy0yb2c" // Workshop v010_005 test sheet
    //final val TEST_REPO_SHEET_KEY = "0AlpQRNQ-L8QUdDg0c1dlT2Q1bEVUMDlDbmVvS0ZwLWc" // v011_02

	// The next two values determine the part of the URL that gives us a particular sheet / graph within
	// the CSV cube, for our two bootstrap pieces of CSV data:
	// 1) Default namespace prefix mappings
	// These may be considered the prefixes of the Directory model, which are then supplied
	// as default prefixes for all queries against the whole repo. 
	final val DFLT_NAMESPACE_SHEET_NUM = 9
	
	// 2) The directory sheet itself, which provides the nucleus of the sheet repo representation.
	final val DFLT_DIRECTORY_SHEET_NUM = 8
	
	// --------------------------------------------------------------
	
	// Alternative params for database-backed repo, with a file-resource initializer.
	// It is easy to make this repo read/write (compared to spreadsheet + file repos)
	
	final val DFLT_SDB_REPO_CONFIG_PATH =  org.appdapter.demo.DemoResources.STORE_CONFIG_PATH; 
	
	// ----------------------------------------------------------------------------------
	
	// Alternative params for a "bunch of file-resources"-backed repo.  This is generally
	// readonly, although it is also possible to read and write live filesystem files.
	
	final val DFLT_FILE_REPO_CONFIG_PATH = "/ok"

	// ----------------------------------------------------------------------------------

	//  OK, that's 3 kinds of repos to test.  
	//  Now, what kind of data do we expect to find  in these repos during our testing?
 
	// The next two params define the repo-client wrapper defaults, giving a default query context
	// to easily fetch from.
	// Either value may be bypassed/overidden using the more general forms of queryIndirect_. 
	/**
	// 1) Default query *source* graph QName used in directory model (Sheet or RDF).
	// We read SPARQL text from this graph, which we use to query *other* graphs.  
	// This graph is typically not used as a regular data graph by  other low-order 
	// query operations, although there is no prohibition or protection from doing so 
	// at this time.   This query source graph may be overridden using the more general
	// forms of queryIndirect_.
	*/
	
	final val DFLT_QRY_SRC_GRAPH_QN = "ccrt:qry_sheet_22"
	
	// 2) default variable name for a single target graph in a SPARQL query.
	// This is used in the convenience forms of queryIndirect that handle many common
	// use cases, wherein the query needs a single graph to operate on that is switched
	// by application logic or user selection.
	
	final val DFLT_TGT_GRAPH_SPARQL_VAR = "qGraph"

	// Last two params can be any query to run, and any graph to run it on as "primary".
	// Queries may also pull in additional graphs, by explicit URI in SPARQL text, or by 
	// binding additional variables to Idents (i.e. URIs) as part of the query invocation.
 
	val lightsQueryQN = "ccrt:find_lights_99" // The QName of a query in the "Queries" model/tab
	val lightsGraphQN = "ccrt:lights_camera_sheet_22" // The QName of a graph = model = tab, as given by directory model.
		
	def main(args: Array[String]) : Unit = {
		// Must enable "compile" scope for Log4J dep in order to compile this code.
	//	org.apache.log4j.BasicConfigurator.configure();
	//	org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.ALL);
		
		// First load up a sheet repo, using 3 params described above.
		// The repo resolves QNames using the namespaces applied to its directory model.
		val dfltTestRepo = loadDefaultTestRepo
		
		// At this point, we can forget that the repo came from a spreadsheet, at least for
		// *reading* purposes.  We could write to the repo in memory, but we'd have to save
		// it in some other format (besides the Google-Spreadhseet).  It wouldn't be hard to
		// do a CSV export, either online or offline, so someone may be picking up that thread 
		// again soon.  
		// 
		// Anyhoo, we can now query our spreadsheet-sourced repo using a given query name (QN), 
		// and the name of a graph to use  as "primary target" (this is the most common form of 
		// simple query).   Result printed should look like:
		// Found solutions for ccrt:find_lights_99 in ccrt:lights_camera_sheet_22 :
		// [( ?light = <urn:ftd:cogchar.org:2012:runtime#light_Demo> ) 
		// ( ?xDir = "-0.1" ) ( ?yDir = "-0.7" ) ( ?zDir = "-1" ) 
		// ( ?lightType = <http://www.cogchar.org/lightscamera/config/instance#DIRECTIONAL> ) 
		// ( ?colorR = "1" ) ( ?colorG = "1" ) ( ?colorB = "1" ) ( ?colorAlpha = "1" ), 
		// ( ?light = <urn:ftd:cogchar.org:2012:runtime#light_Ambient> ) 
		// ( ?lightType = <http://www.cogchar.org/lightscamera/config/instance#AMBIENT> ) 
		// ( ?colorR = "0.8" ) ( ?colorG = "0.8" ) ( ?colorB = "0.8" ) ( ?colorAlpha = "1" )]
		// 
		 
		
		RepoTester.testRepoDirect(dfltTestRepo, DFLT_QRY_SRC_GRAPH_QN, lightsQueryQN, DFLT_TGT_GRAPH_SPARQL_VAR, lightsGraphQN)
		
		// Next, let's set up a RepoClient wrapper to give us some extra features. 
		// (RepoClient constructor params uses opposite order than the variables documented above)
		val dfltTestRC = makeDefaultRepoClient(dfltTestRepo)
		
		// Do the same query again, making use of the default params supplied by the RepoClient wrapper.
		println("Running same query via RepoClient")
		val solList = dfltTestRC.queryIndirectForAllSolutions(lightsQueryQN, lightsGraphQN)
		println("Results, in the form of 'Solution' wrapper objects = " + solList.javaList)
		
		// The dirGraphID can be phony for now, because it is not needed until we try to do something
		// fancy like an indirect query.  We can do direct SPARQL queries against the dataset without
		// having a valid directory graph.  However, we cannot use the "InitialBinding" convenience
		// methods until we have a valid directoryModel to resolve prefixes.  (An alternative could
		// be to treat the "default graph" of our dataset as the directoryModel, at least for NS
		// resolution purposes).  
		// 
		val dirGraphID = new FreeIdent("urn:org.cogchar/dirModelInRepoTestDB", "dirModelInRepoTestDB");
		val dbRepo = RepoTester.loadDatabaseRepo(DFLT_SDB_REPO_CONFIG_PATH, dirGraphID)
		println("Built dbRepo: " + dbRepo);
		
		val lightsGraphID = dfltTestRC.makeIdentForQName(lightsGraphQN);
		val copyURI_Tail = "ranDumbModelURI_02"
		val copyID = new FreeIdent("urn:org.cogchar/" + copyURI_Tail, copyURI_Tail)
		val lightsModelFromSheet = dfltTestRepo.getNamedModel(lightsGraphID);
		println("Fetched lights model: " + lightsModelFromSheet)
		dbRepo.addNamedModel(copyID, lightsModelFromSheet);
		val copiedModel = dbRepo.getNamedModel(copyID)
		println("Copied model: " + copiedModel);
		
		val dfltGraphStatsJL : java.util.List[Repo.GraphStat] = dfltTestRepo.getGraphStats();
		
		println("Got dflt Graph Stats: " + dfltGraphStatsJL)
		import scala.collection.JavaConversions._
		
		val dfltGraphStats : List[Repo.GraphStat] = dfltGraphStatsJL.toList
		dfltGraphStats foreach (gs => {
			println("Doing import for: " + gs)
			val tgtModelID = dbRepo.makeIdentForURI(gs.graphURI)
			val srcModel = dfltTestRepo.getNamedModel(tgtModelID)
			dbRepo.addNamedModel(tgtModelID, srcModel)
		})
		
		
		
		println("DB-Repo dataset: " + dbRepo.getMainQueryDataset())
		
		val resolvedQueryURL = org.appdapter.demo.DemoResources.QUERY_PATH;
		// RepoTester.testRepoDirect(dbRepo, )
	}
	def loadDefaultTestRepo : FancyRepo = RepoTester.loadSheetRepo(TEST_REPO_SHEET_KEY, DFLT_NAMESPACE_SHEET_NUM, DFLT_DIRECTORY_SHEET_NUM)
	def makeDefaultRepoClient (repo : FancyRepo) : RepoClient = makeRepoClient(repo, DFLT_TGT_GRAPH_SPARQL_VAR, DFLT_QRY_SRC_GRAPH_QN)
	
  	def makeRepoClient(fr : FancyRepo, queryTargetVarName:  String, querySheetQN : String) : RepoClient = {
		new RepoClientImpl(fr, queryTargetVarName, querySheetQN)		
	}		

		/*
	def loadRepoSQL(configResPath: String) : DatabaseRepo = {
		
	}
	*/
}
