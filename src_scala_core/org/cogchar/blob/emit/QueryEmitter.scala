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
object QueryEmitter {
  
  final val QUERY_SHEET = "ccrt:qry_sheet_22"
  
  /** Provided solely for testing of queries
  *
  */
  def main(args: Array[String]) : Unit = {

	val QUERY_TO_TEST = "ccrt:find_basicJointProperties_99"
	
	val sr : SheetRepo = SheetRepo.loadTestSheetRepo()
	val qText = sr.getQueryText(QUERY_SHEET, QUERY_TO_TEST)
	println("Found query text: " + qText)
		
	val parsedQ = sr.parseQueryText(qText);
	val solnJavaList : java.util.List[QuerySolution] = sr.findAllSolutions(parsedQ, null);
	println("Found solutions: " + solnJavaList)
  }
  
  /** Queries the configuration spreadsheet (with query sheet URI currently set in code as final val QUERY_SHEET).
   *
   * @param queryUri The QName of the query which should be run, found on the query sheet currently set in code
   * @param keyVarName The query variable name of resources by which the SolutionMap should be keyed
   * @return A SolutionMap of QuerySolutions with keys of the URIs of instances of keyVarName in solutions
   */
  def getQueryResultMap(queryUri:String, keyVarName:String): SolutionMap[Ident]={
	val sr : SheetRepo = SheetRepo.loadTestSheetRepo()
	getQueryResultMap(queryUri, keyVarName, sr)
  }
  
  /** Queries the provided SheetRepo (with query sheet URI currently set in code as final val QUERY_SHEET).
   *
   * @param queryUri The QName of the query which should be run, found on the query sheet currently set in code
   * @param keyVarName The query variable name of resources by which the SolutionMap should be keyed
   * @param sr The SheetRepo to be referenced for query
   * @return A SolutionMap of QuerySolutions with keys of the URIs of instances of keyVarName in solutions
   */
  def getQueryResultMap(queryUri:String, keyVarName:String, sr:SheetRepo): SolutionMap[Ident]={
	val qText = sr.getQueryText(QUERY_SHEET, queryUri)
	getTextQueryResultMap(qText, keyVarName, sr)
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
		  solutionMap.map(new FreeIdent(solution.getResource(keyVarName).getURI, solution.getResource(keyVarName).getLocalName)) = solution 
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
		  solutionMap.map(solution.getLiteral(keyVarName).getString) = solution 
		}
	  })
	solutionMap
  }
  
  /** Queries the configuration spreadsheet (with query sheet URI currently set in code as final val QUERY_SHEET).
   *
   * @param queryUri The QName of the query which should be run, found on the query sheet currently set in code
   * @return A SolutionList of QuerySolutions
   */
  def getQueryResultList(queryUri:String): SolutionList={
	val sr : SheetRepo = SheetRepo.loadTestSheetRepo()
	getQueryResultList(queryUri, sr)
  }
  
  /** Queries the provided SheetRepo (with query sheet URI currently set in code as final val QUERY_SHEET).
   *
   * @param queryUri The QName of the query which should be run, found on the query sheet currently set in code
   * @param sr The SheetRepo to be referenced for query
   * @return A SolutionList of QuerySolutions
   */
  def getQueryResultList(queryUri:String, sr:SheetRepo): SolutionList={
	val qText = sr.getQueryText(QUERY_SHEET, queryUri)
	getTextQueryResultList(qText, sr);
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
	solutionList.list = sr.findAllSolutions(parsedQ, null)
	solutionList
  }
  
  /** Loads the test sheet and returns its SheetRepo.
   *
   * @return The test sheet SheetRepo
   */
  def getSheet = SheetRepo.loadTestSheetRepo()
 
  /** Gets a query from the query sheet page (specfied in code as QUERY_SHEET) of the test sheet
   *
   * @param queryUri The QName of the query which should be returned, found on the query sheet currently set in code
   * @return String containing query
   */
  def getQuery(queryUri:String):String={
	val sr : SheetRepo = SheetRepo.loadTestSheetRepo()
	sr.getQueryText(QUERY_SHEET, queryUri);
  }
  
  /** Gets a query from the query sheet page (specfied in code as QUERY_SHEET) of the provided SheetRepo
   *
   * @param queryUri The QName of the query which should be returned, found on the query sheet currently set in code
   * @param sr The sheet repo in which the queries are located
   * @return String containing query
   */
  def getQuery(queryUri:String, sr:SheetRepo):String = {
	sr.getQueryText(QUERY_SHEET, queryUri);
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
  def setQueryVar(query:String, queryVarName: String, value: Ident) = {
	//var queryOps = new StringOps(query) // wasn't working implicitly for some reason
	query.replaceAll("!!" + queryVarName, "<" + value.getAbsUriString + ">")
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
	  literal = solutionMap.map(selectorUri).getLiteral(variableName).getString
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
	  literal = solutionMap.map(selector).getLiteral(variableName).getString
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
	for (i <- 0 until solutionList.list.length) yield solutionList.list(i).getLiteral(variableName).getString
  }
  
  def getIdentsFromSolution(solutionList:SolutionList, variableName:String) = {
	val identList = new scala.collection.mutable.ArrayBuffer[Ident];
	solutionList.list.foreach(solution => {
	  identList += new FreeIdent(solution.getResource(variableName).getURI, solution.getResource(variableName).getLocalName)
	})
	identList
  }
  
  def getIdentsFromSolutionAsJava(solutionList:SolutionList, variableName:String): java.util.List[Ident] = getIdentsFromSolution(solutionList, variableName)
  
  def getFloatFromSolution(solutionMap:SolutionMap[Ident], selector:Ident, variableName:String) = {
	var literal: Float = Float.NaN
	if (solutionMap.map contains selector) {
	  literal = solutionMap.map(selector).getLiteral(variableName).getFloat
	}
	literal
  }
  
  def getDoubleFromSolution(solutionMap:SolutionMap[Ident], selector:Ident, variableName:String) = {
	var literal: Double = Double.NaN
	if (solutionMap.map contains selector) {
	  literal = solutionMap.map(selector).getLiteral(variableName).getDouble
	}
	literal
  }
  
  def getDoubleFromSolution(solutionMap:SolutionMap[String], selector:String, variableName:String) = {
	var literal: Double = Double.NaN
	if (solutionMap.map contains selector) {
	  literal = solutionMap.map(selector).getLiteral(variableName).getDouble
	}
	literal
  }
  
  def getIntegerFromSolution(solutionMap:SolutionMap[Ident], selector:Ident, variableName:String) = {
	// I'd really prefer to set this to null to result in NPE in subsequent Java code if it's not found in solution
	// But Scala won't allow that for Int (or Float), and use of an Option seems inappropriate when this will be often called from Java code
	var literal: Int = 0
	if (solutionMap.map contains selector) {
	  literal = solutionMap.map(selector).getLiteral(variableName).getInt
	}
	literal
  }
  
}
