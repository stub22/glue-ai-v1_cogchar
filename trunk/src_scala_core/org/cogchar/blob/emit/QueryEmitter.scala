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

import org.appdapter.core.item.{Ident, FreeIdent}
import org.appdapter.core.matdat.{SheetRepo}
import com.hp.hpl.jena.rdf.model.{Model, Statement, Resource, Property, Literal, RDFNode, ModelFactory, InfModel}

import com.hp.hpl.jena.query.{Query, QueryFactory, QueryExecution, QueryExecutionFactory, QuerySolution, QuerySolutionMap, Syntax};
import com.hp.hpl.jena.query.{Dataset, DatasetFactory, DataSource};
import com.hp.hpl.jena.query.{ResultSet, ResultSetFormatter, ResultSetRewindable, ResultSetFactory};

import scala.collection.immutable.StringOps
import scala.collection.mutable.HashMap
import scala.collection.JavaConversions._

/**
 * @author Ryan Biggs
 */

/** A still-under-construction API layer for the handling of query-based config data
 *
 */

class QueryEmitter extends QueryInterface {
  
  private def ensureRepo {
	if (QuerySheet.repo == null) {
	  QuerySheet.repo = QuerySheet.loadSheetRepo
	}
  }
  
  def reloadSheetRepo {
	QuerySheet.repo = QuerySheet.loadSheetRepo
  }
  
  
  /** Returns the current cached SheetRepo
   *
   * @return The test sheet SheetRepo
   */
  def getSheet = {
	ensureRepo
	QuerySheet.repo
  }
  
  /** Queries the configuration spreadsheet (with query sheet URI currently set in code as final val QUERY_SHEET).
   *
   * @param queryUri The QName of the query which should be run, found on the query sheet currently set in code
   * @param keyVarName The query variable name of resources by which the SolutionMap should be keyed
   * @return A SolutionMap of QuerySolutions with keys of the URIs of instances of keyVarName in solutions
   */
  def getQueryResultMap(queryUri:String, keyVarName:String): SolutionMap[Ident]={
	ensureRepo
	getQueryResultMap(queryUri, keyVarName, QuerySheet.repo)
  }
  
  /** Queries the configuration spreadsheet (with query sheet URI currently set in code as final val QUERY_SHEET).
   *  Updates the query specified by queryUri with the graph ident in qGraph
   *
   * @param queryUri The QName of the query which should be run, found on the query sheet currently set in code
   * @param keyVarName The query variable name of resources by which the SolutionMap should be keyed
   * @param qGraph The Ident of the graph to be used in query
   * @return A SolutionMap of QuerySolutions with keys of the URIs of instances of keyVarName in solutions
   */
  def getQueryResultMap(queryUri:String, keyVarName:String, qGraph:Ident): SolutionMap[Ident] = {
	val qText = getCompletedQueryFromTemplate(queryUri, QuerySheet.GRAPH_QUERY_VAR, qGraph)
	getTextQueryResultMap(qText, keyVarName)
  }
  
  /** Queries the provided SheetRepo (with query sheet URI currently set in code as final val QUERY_SHEET).
   *
   * @param queryUri The QName of the query which should be run, found on the query sheet currently set in code
   * @param keyVarName The query variable name of resources by which the SolutionMap should be keyed
   * @param sr The SheetRepo to be referenced for query
   * @return A SolutionMap of QuerySolutions with keys of the URIs of instances of keyVarName in solutions
   */
  def getQueryResultMap(queryUri:String, keyVarName:String, sr:SheetRepo): SolutionMap[Ident]={
	val qText = sr.getQueryText(QuerySheet.QUERY_SHEET, queryUri)
	getTextQueryResultMap(qText, keyVarName, sr)
  }
  
  /** Queries the configuration spreadsheet with a provided query
   *
   * @param qText The text of the query to be run
   * @param keyVarName The query variable name of resources by which the SolutionMap should be keyed
   * @return A SolutionMap of QuerySolutions with keys of the URIs of instances of keyVarName in solutions
   */
  def getTextQueryResultMap(qText:String, keyVarName:String): SolutionMap[Ident]={
	ensureRepo
	getTextQueryResultMap(qText, keyVarName, QuerySheet.repo)
  }
  
  /** Queries the configuration spreadsheet with a provided query and specified qGraph
   *
   * @param qText The text of the query to be run
   * @param keyVarName The query variable name of resources by which the SolutionMap should be keyed
   * @param qGraph The Ident of the graph to be used in query
   * @return A SolutionMap of QuerySolutions with keys of the URIs of instances of keyVarName in solutions
   */
  def getTextQueryResultMap(qText:String, keyVarName:String, qGraph:Ident): SolutionMap[Ident] = {
	val query = setQueryVar(qText, QuerySheet.GRAPH_QUERY_VAR, qGraph)
	getTextQueryResultMap(query, keyVarName)
  }
  
  /** Queries the provided SheetRepo with a provided query
   *
   * @param qText The text of the query to be run
   * @param keyVarName The query variable name of resources by which the SolutionMap should be keyed
   * @param sr The SheetRepo to be referenced for query
   * @return A SolutionMap of QuerySolutions with keys of the URIs of instances of keyVarName in solutions
   */
  def getTextQueryResultMap(qText:String, keyVarName:String, sr:SheetRepo): SolutionMap[Ident]={
	val solutionMap = new SolutionMap[Ident]
	val parsedQ = sr.parseQueryText(qText);
	val solnList : scala.collection.mutable.Buffer[QuerySolution] = sr.findAllSolutions(parsedQ, null);
	solnList.foreach(solution => {
		if (solution contains keyVarName) {
		  solutionMap.map(new FreeIdent(solution.getResource(keyVarName).getURI, solution.getResource(keyVarName).getLocalName)) = new Solution(solution)
		}
	  })
	solutionMap
  }
  
  /** Queries the provided SheetRepo with a provided query
   *
   * @param qText The text of the query to be run
   * @param keyVarName The query variable name of resources by which the SolutionMap should be keyed
   * @param sr The SheetRepo to be referenced for query
   * @return A SolutionMap of QuerySolutions with keys of the URIs of instances of keyVarName in solutions
   */
  def getTextQueryResultMapByStringKey(qText:String, keyVarName:String, sr:SheetRepo): SolutionMap[String]={
	val solutionMap = new SolutionMap[String]
	val parsedQ = sr.parseQueryText(qText);
	val solnList : scala.collection.mutable.Buffer[QuerySolution] = sr.findAllSolutions(parsedQ, null);
	solnList.foreach(solution => {
		if (solution contains keyVarName) {
		  solutionMap.map(solution.getLiteral(keyVarName).getString) = new Solution(solution) 
		}
	  })
	solutionMap
  }
  
  /** Queries the configuration spreadsheet with a provided query and specified qGraph
   *
   * @param qText The text of the query to be run
   * @param keyVarName The query variable name of resources by which the SolutionMap should be keyed
   * @param qGraph The Ident of the graph to be used in query
   * @return A SolutionMap of QuerySolutions with keys of the URIs of instances of keyVarName in solutions
   */
  def getTextQueryResultMapByStringKey(qText:String, keyVarName:String, qGraph:Ident): SolutionMap[String] = {
	val query = setQueryVar(qText, QuerySheet.GRAPH_QUERY_VAR, qGraph)
	getTextQueryResultMapByStringKey(query, keyVarName)
  }
  
  /** Queries the configuration spreadsheet with a provided query
   *
   * @param qText The text of the query to be run
   * @param keyVarName The query variable name of resources by which the SolutionMap should be keyed
   * @return A SolutionMap of QuerySolutions with keys of the URIs of instances of keyVarName in solutions
   */
  def getTextQueryResultMapByStringKey(qText:String, keyVarName:String): SolutionMap[String]={
	ensureRepo
	getTextQueryResultMapByStringKey(qText, keyVarName, QuerySheet.repo);
  }
  
  /** Queries the configuration spreadsheet (with query sheet URI currently set in code as final val QUERY_SHEET).
   *
   * @param queryUri The QName of the query which should be run, found on the query sheet currently set in code
   * @return A SolutionList of QuerySolutions
   */
  def getQueryResultList(queryUri:String): SolutionList={
	ensureRepo
	getQueryResultList(queryUri, QuerySheet.repo)
  }
  
  /** Queries the configuration spreadsheet (with query sheet URI currently set in code as final val QUERY_SHEET)
   *  with graph Ident
   *
   * @param queryUri The QName of the query which should be run, found on the query sheet currently set in code
   * @param qGraph The Ident of the graph to be used in query
   * @return A SolutionList of QuerySolutions
   */
  def getQueryResultList(queryUri:String, qGraph:Ident): SolutionList = {
	val qText = getCompletedQueryFromTemplate(queryUri, QuerySheet.GRAPH_QUERY_VAR, qGraph)
	getTextQueryResultList(qText);
  }
  
  /** Queries the provided SheetRepo (with query sheet URI currently set in code as final val QUERY_SHEET).
   *
   * @param queryUri The QName of the query which should be run, found on the query sheet currently set in code
   * @param sr The SheetRepo to be referenced for query
   * @return A SolutionList of QuerySolutions
   */
  def getQueryResultList(queryUri:String, sr:SheetRepo): SolutionList={
	val qText = sr.getQueryText(QuerySheet.QUERY_SHEET, queryUri)
	getTextQueryResultList(qText, sr);
  }
  
  /** Queries the configuration spreadsheet with a provided query
   *
   * @param qText The text of the query to be run
   * @return A SolutionList of QuerySolutions
   */
  def getTextQueryResultList(qText:String): SolutionList={
	ensureRepo
	getTextQueryResultList(qText, QuerySheet.repo)
  }
  
  /** Queries the configuration spreadsheet with a provided query and graph Ident
   *
   * @param qText The text of the query to be run
   * @param qGraph The Ident of the graph to be used in query
   * @return A SolutionList of QuerySolutions
   */
  def getTextQueryResultList(qText:String, qGraph:Ident): SolutionList = {
	val query = setQueryVar(qText, QuerySheet.GRAPH_QUERY_VAR, qGraph)
	getTextQueryResultList(query);
  }
  
  /** Queries the provided SheetRepo with a provided query
   *
   * @param qText The text of the query to be run
   * @param sr The SheetRepo to be referenced for query
   * @return A SolutionList of QuerySolutions
   */
  def getTextQueryResultList(qText: String, sr:SheetRepo): SolutionList = {
	val solutionList = new SolutionList
	val parsedQ = sr.parseQueryText(qText);
	val nativeSolutionList: scala.collection.mutable.Buffer[QuerySolution] = sr.findAllSolutions(parsedQ, null)
	nativeSolutionList.foreach( solution => solutionList.list += new Solution(solution))
	solutionList
  }
 
  /** Gets a query from the query sheet page (specfied in code as QUERY_SHEET) of the test sheet
   *
   * @param queryUri The QName of the query which should be returned, found on the query sheet currently set in code
   * @return String containing query
   */
  def getQuery(queryUri:String):String={
	ensureRepo
	QuerySheet.repo.getQueryText(QuerySheet.QUERY_SHEET, queryUri);
  }
  
  /** Gets a query from the query sheet page (specfied in code as QUERY_SHEET) of the provided SheetRepo
   *
   * @param queryUri The QName of the query which should be returned, found on the query sheet currently set in code
   * @param sr The sheet repo in which the queries are located
   * @return String containing query
   */
  def getQuery(queryUri:String, sr:SheetRepo):String = {
	sr.getQueryText(QuerySheet.QUERY_SHEET, queryUri);
  }
  
  /** A convenience method to replace a query variable (indicated with "!!" prefix) in a query with its String value
   *
   * @param query The text of the query to be modified
   * @param queryVarName The name of the query variable (with "!!" prefix omitted)
   * @param value The value of the query variable
   * @return New query with variable replaced with literal
   */
  def setQueryVar(query:String, queryVarName: String, value: String) = {
	var queryOps = new StringOps(query) // wasn't working implicitly for some reason
	queryOps.replaceAll("!!" + queryVarName, value)
  }
  
  /** A convenience method to replace a query variable (indicated with "!!" prefix) in a query with its URI value
   *
   * @param query The text of the query to be modified
   * @param queryVarName The name of the query variable (with "!!" prefix omitted)
   * @param value The value of the query variable
   * @return New query with variable replaced with literal
   */
  def setQueryVar(query:String, queryVarName: String, value: Ident): String = {
	var newQuery = ""
	if (query != null) {
	  newQuery = query.replaceAll("!!" + queryVarName, "<" + value.getAbsUriString + ">")
	}
	newQuery
  }
  
  def getCompletedQueryFromTemplate(templateUri:String, queryVarName: String, queryVarValue: Ident) = {
	val query = getQuery(templateUri)
	setQueryVar(query, queryVarName, queryVarValue)
  }
  
  /** Gets a string literal from a query solution located in a SolutionMap and keyed by a selector URI
   *
   * @param solutionMap The SolutionMap in which the desired solution is located
   * @param selectorUri The key URI which selects the desired solution
   * @param variableName The query variable name for the string literal desired
   * @return The selected String literal
   */
  def getStringFromSolution(solutionMap:SolutionMap[Ident], selectorUri:Ident, variableName:String) = {
	var literal: String = null
	if (solutionMap.map contains selectorUri) {
	  literal = solutionMap.map(selectorUri).solution.getLiteral(variableName).getString
	}
	literal
  }
  
  /** Gets a string literal from a query solution located in a SolutionMap and keyed by a selector String
   *
   * @param solutionMap The SolutionMap in which the desired solution is located
   * @param selector The key String which selects the desired solution
   * @param variableName The query variable name for the string literal desired
   * @return The selected String literal
   */
  def getStringFromSolution(solutionMap:SolutionMap[String], selector:String, variableName:String) = {
	var literal: String = null
	if (solutionMap.map contains selector) {
	  literal = solutionMap.map(selector).solution.getLiteral(variableName).getString
	}
	literal
  }
  
  /** Gets a string literal from a single query solution
   *
   * @param solution The Solution in which the desired solution is located
   * @param variableName The query variable name for the string literal desired
   * @return The selected string literal
   */
  def getStringFromSolution(solution:Solution, variableName:String): String = {
	getStringFromSolution(solution, variableName, null)
  }
   
  /** Gets a string literal from a single query solution with a provided default if solution variable is not found
   *
   * @param solution The Solution in which the desired solution is located
   * @param variableName The query variable name for the string literal desired
   * @param default The String to return if the query variable is not found in solution
   * @return The selected string literal
   */
  def getStringFromSolution(solution:Solution, variableName:String, default:String): String = {
	var literal: String = default
	if (solution.solution.contains(variableName)) {
	  literal = solution.solution.getLiteral(variableName).getString
	}
	literal
  }
  
  /** Gets (an ArrayBuffer?) of string literals from each of the query solutions located in a SolutionList
   *
   * @param solutionList The SolutionList in which the desired solutions are located
   * @param variableName The query variable name for the string literals desired
   * @return The selected string literals
   */
  def getStringsFromSolution(solutionList:SolutionList, variableName:String) = {
	for (i <- 0 until solutionList.list.length) yield solutionList.list(i).solution.getLiteral(variableName).getString
  }
  
  def getStringsFromSolutionAsJava(solutionList:SolutionList, variableName:String): java.util.List[String] = getStringsFromSolution(solutionList, variableName)
  
  def getIdentFromSolution(solution:Solution, variableName:String) = {
	var ident:Ident = null;
	if (solution.solution.contains(variableName)) {
	  ident = new FreeIdent(solution.solution.getResource(variableName).getURI, solution.solution.getResource(variableName).getLocalName)
	}
	ident
  }
  
  def getIdentsFromSolution(solutionList:SolutionList, variableName:String) = {
	val identList = new scala.collection.mutable.ArrayBuffer[Ident];
	solutionList.list.foreach(solution => {
		identList += new FreeIdent(solution.solution.getResource(variableName).getURI, solution.solution.getResource(variableName).getLocalName)
	  })
	identList
  }
  
  def getIdentsFromSolutionAsJava(solutionList:SolutionList, variableName:String): java.util.List[Ident] = getIdentsFromSolution(solutionList, variableName)
  
  def getFloatFromSolution(solutionMap:SolutionMap[Ident], selector:Ident, variableName:String) = {
	var literal: Float = Float.NaN
	if (solutionMap.map contains selector) {
	  literal = solutionMap.map(selector).solution.getLiteral(variableName).getFloat
	}
	literal
  }
  
  def getFloatFromSolution(solution:Solution, variableName:String, default:Float) = {
	var literal: Float = default
	if (solution.solution.contains(variableName)) {
	  literal = solution.solution.getLiteral(variableName).getFloat
	}
	literal
  }
  
  def getDoubleFromSolution(solutionMap:SolutionMap[Ident], selector:Ident, variableName:String) = {
	var literal: Double = Double.NaN
	if (solutionMap.map contains selector) {
	  literal = solutionMap.map(selector).solution.getLiteral(variableName).getDouble
	}
	literal
  }
  
  def getDoubleFromSolution(solutionMap:SolutionMap[String], selector:String, variableName:String) = {
	var literal: Double = Double.NaN
	if (solutionMap.map contains selector) {
	  literal = solutionMap.map(selector).solution.getLiteral(variableName).getDouble
	}
	literal
  }
  
   /** Gets a double literal from a single query solution with a provided default if solution variable is not found
   *
   * @param solution The Solution in which the desired double is located
   * @param variableName The query variable name for the double literal desired
   * @param default The double to return if the query variable is not found in solution
   * @return The selected double literal
   */
  def getDoubleFromSolution(solution:Solution, variableName:String, default:Double): Double = {
	var literal: Double = default
	if (solution.solution.contains(variableName)) {
	  literal = solution.solution.getLiteral(variableName).getDouble
	}
	literal
  }
  
  def getIntegerFromSolution(solutionMap:SolutionMap[Ident], selector:Ident, variableName:String) = {
	// I'd really prefer to set this to null to result in NPE in subsequent Java code if it's not found in solution
	// But Scala won't allow that for Int (or Float), and use of an Option seems inappropriate when this will be often called from Java code
	var literal: Int = 0
	if (solutionMap.map contains selector) {
	  literal = solutionMap.map(selector).solution.getLiteral(variableName).getInt
	}
	literal
  }
  
  /** Gets a boolean literal from a query solution located in a SolutionMap and keyed by a selector Ident
   *
   * @param solutionMap The SolutionMap in which the desired literal is located
   * @param selector The key Ident which selects the desired solution
   * @param variableName The query variable name for the boolean literal desired
   * @return The selected boolean literal
   */
  def getBooleanFromSolution(solutionMap:SolutionMap[Ident], selector:Ident, variableName:String): Boolean = {
	var literal: Boolean = false
	if (solutionMap.map contains selector) {
	  literal = solutionMap.map(selector).solution.getLiteral(variableName).getBoolean
	}
	literal
  }
 
  /** Gets a boolean literal from a single query solution
   *
   * @param solution The Solution in which the desired solution is located
   * @param variableName The query variable name for the boolean literal desired
   * @return The selected boolean literal
   */
  def getBooleanFromSolution(solution:Solution, variableName:String) = {
	var literal: Boolean = false
	if (solution.solution contains variableName) {
	  literal = solution.solution.getLiteral(variableName).getBoolean
	}
	literal
  }
}

object QuerySheet {
  
  final val SHEET_KEY = "0ArBjkBoH40tndDdsVEVHZXhVRHFETTB5MGhGcWFmeGc" // Main test sheet!
  //final val SHEET_KEY = "0Ajj1Rnx7FCoHdDN2VFdVazMzRGNGY3BMQmk1TXZzUHc" // Biggs test sheet!
  //final val SHEET_KEY = "0AlpQRNQ-L8QUdDNWQXpmSW9iNzROcHktZEJZdTJhY2c" // Workshop v010_004 test sheet
  //final val SHEET_KEY = "0AlpQRNQ-L8QUdGx2RkhDX1VEWklrS256cEVOcy0yb2c" // Workshop v010_005 test sheet
  final val NS_SHEET_NUM = 9
  final val DIR_SHEET_NUM = 8
  final val QUERY_SHEET = "ccrt:qry_sheet_22"
  final val GRAPH_QUERY_VAR = "qGraph"
  var repo: SheetRepo = null;
  var interface: QueryInterface = null;
  
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
	val dirModel : Model = SheetRepo.readDirectoryModelFromGoog(QuerySheet.SHEET_KEY, QuerySheet.NS_SHEET_NUM, QuerySheet.DIR_SHEET_NUM) 
	val sr = new SheetRepo(dirModel)
	sr.loadSheetModelsIntoMainDataset()
	sr
  }
  
  // A temporary hook-in to allow current clients of QueryEmitter to easily get a "primary" instance until they 
  // start using the managed service version - really this should happen in a registry but this is a short-term fix
  def getInterface = {
	if (interface == null) {
	  interface = new QueryEmitter()
	}
	interface
  }
}

