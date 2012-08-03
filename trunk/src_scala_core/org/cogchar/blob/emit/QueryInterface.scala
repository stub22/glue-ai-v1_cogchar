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

import org.appdapter.core.item.Ident
import org.appdapter.core.matdat.SheetRepo

trait QueryInterface {

  def reloadSheetRepo
  
  /** Returns the current cached SheetRepo
   *
   * @return The test sheet SheetRepo
   */
  def getSheet: SheetRepo
  
  /** Queries the configuration spreadsheet (with query sheet URI currently set in code as final val QUERY_SHEET).
   *
   * @param queryUri The QName of the query which should be run, found on the query sheet currently set in code
   * @param keyVarName The query variable name of resources by which the SolutionMap should be keyed
   * @return A SolutionMap of QuerySolutions with keys of the URIs of instances of keyVarName in solutions
   */
  def getQueryResultMap(queryUri:String, keyVarName:String): SolutionMap[Ident]
  
  /** Queries the provided SheetRepo (with query sheet URI currently set in code as final val QUERY_SHEET).
   *
   * @param queryUri The QName of the query which should be run, found on the query sheet currently set in code
   * @param keyVarName The query variable name of resources by which the SolutionMap should be keyed
   * @param sr The SheetRepo to be referenced for query
   * @return A SolutionMap of QuerySolutions with keys of the URIs of instances of keyVarName in solutions
   */
  def getQueryResultMap(queryUri:String, keyVarName:String, sr:SheetRepo): SolutionMap[Ident]
  
  /** Queries the configuration spreadsheet with a provided query
   *
   * @param qText The text of the query to be run
   * @param keyVarName The query variable name of resources by which the SolutionMap should be keyed
   * @return A SolutionMap of QuerySolutions with keys of the URIs of instances of keyVarName in solutions
   */
  def getTextQueryResultMap(qText:String, keyVarName:String): SolutionMap[Ident]
  
  /** Queries the provided SheetRepo with a provided query
   *
   * @param qText The text of the query to be run
   * @param keyVarName The query variable name of resources by which the SolutionMap should be keyed
   * @param sr The SheetRepo to be referenced for query
   * @return A SolutionMap of QuerySolutions with keys of the URIs of instances of keyVarName in solutions
   */
  def getTextQueryResultMap(qText:String, keyVarName:String, sr:SheetRepo): SolutionMap[Ident]
  
  /** Queries the provided SheetRepo with a provided query
   *
   * @param qText The text of the query to be run
   * @param keyVarName The query variable name of resources by which the SolutionMap should be keyed
   * @param sr The SheetRepo to be referenced for query
   * @return A SolutionMap of QuerySolutions with keys of the URIs of instances of keyVarName in solutions
   */
  def getTextQueryResultMapByStringKey(qText:String, keyVarName:String, sr:SheetRepo): SolutionMap[String]
  
  /** Queries the configuration spreadsheet with a provided query
   *
   * @param qText The text of the query to be run
   * @param keyVarName The query variable name of resources by which the SolutionMap should be keyed
   * @return A SolutionMap of QuerySolutions with keys of the URIs of instances of keyVarName in solutions
   */
  def getTextQueryResultMapByStringKey(qText:String, keyVarName:String): SolutionMap[String]
  
  /** Queries the configuration spreadsheet (with query sheet URI currently set in code as final val QUERY_SHEET).
   *
   * @param queryUri The QName of the query which should be run, found on the query sheet currently set in code
   * @return A SolutionList of QuerySolutions
   */
  def getQueryResultList(queryUri:String): SolutionList
  
  /** Queries the provided SheetRepo (with query sheet URI currently set in code as final val QUERY_SHEET).
   *
   * @param queryUri The QName of the query which should be run, found on the query sheet currently set in code
   * @param sr The SheetRepo to be referenced for query
   * @return A SolutionList of QuerySolutions
   */
  def getQueryResultList(queryUri:String, sr:SheetRepo): SolutionList
  
  /** Queries the configuration spreadsheet with a provided query
   *
   * @param qText The text of the query to be run
   * @return A SolutionList of QuerySolutions
   */
  def getTextQueryResultList(qText:String): SolutionList
  
  /** Queries the provided SheetRepo with a provided query
   *
   * @param qText The text of the query to be run
   * @param sr The SheetRepo to be referenced for query
   * @return A SolutionList of QuerySolutions
   */
  def getTextQueryResultList(qText: String, sr:SheetRepo): SolutionList
 
  /** Gets a query from the query sheet page (specfied in code as QUERY_SHEET) of the test sheet
   *
   * @param queryUri The QName of the query which should be returned, found on the query sheet currently set in code
   * @return String containing query
   */
  def getQuery(queryUri:String): String
  
  /** Gets a query from the query sheet page (specfied in code as QUERY_SHEET) of the provided SheetRepo
   *
   * @param queryUri The QName of the query which should be returned, found on the query sheet currently set in code
   * @param sr The sheet repo in which the queries are located
   * @return String containing query
   */
  def getQuery(queryUri:String, sr:SheetRepo): String
  
  /** A convenience method to replace a query variable (indicated with "!!" prefix) in a query with its String value
   *
   * @param query The text of the query to be modified
   * @param queryVarName The name of the query variable (with "!!" prefix omitted)
   * @param value The value of the query variable
   * @return New query with variable replaced with literal
   */
  def setQueryVar(query:String, queryVarName: String, value: String): String
  
  /** A convenience method to replace a query variable (indicated with "!!" prefix) in a query with its URI value
   *
   * @param query The text of the query to be modified
   * @param queryVarName The name of the query variable (with "!!" prefix omitted)
   * @param value The value of the query variable
   * @return New query with variable replaced with literal
   */
  def setQueryVar(query:String, queryVarName: String, value: Ident): String
  
  def getCompletedQueryFromTemplate(templateUri:String, queryVarName: String, queryVarValue: Ident): String
  
  /** Gets a string literal from a query solution located in a SolutionMap and keyed by a selector URI
   *
   * @param solutionMap The SolutionMap in which the desired solution is located
   * @param selectorUri The key URI which selects the desired solution
   * @param variableName The query variable name for the string literal desired
   * @return The selected String literal
   */
  def getStringFromSolution(solutionMap:SolutionMap[Ident], selectorUri:Ident, variableName:String): String
  
  /** Gets a string literal from a query solution located in a SolutionMap and keyed by a selector String
   *
   * @param solutionMap The SolutionMap in which the desired solution is located
   * @param selector The key String which selects the desired solution
   * @param variableName The query variable name for the string literal desired
   * @return The selected String literal
   */
  def getStringFromSolution(solutionMap:SolutionMap[String], selector:String, variableName:String): String
  
  /** Gets a string literal from a single query solution
   *
   * @param solution The Solution in which the desired solution is located
   * @param variableName The query variable name for the string literal desired
   * @return The selected string literal
   */
  def getStringFromSolution(solution:Solution, variableName:String): String
   
  /** Gets a string literal from a single query solution with a provided default if solution variable is not found
   *
   * @param solution The Solution in which the desired solution is located
   * @param variableName The query variable name for the string literal desired
   * @param default The String to return if the query variable is not found in solution
   * @return The selected string literal
   */
  def getStringFromSolution(solution:Solution, variableName:String, default:String): String
  
  /** Gets an IndexedSeq of string literals from each of the query solutions located in a SolutionList
   *
   * @param solutionList The SolutionList in which the desired solutions are located
   * @param variableName The query variable name for the string literals desired
   * @return The selected string literals
   */
  def getStringsFromSolution(solutionList:SolutionList, variableName:String): scala.collection.immutable.IndexedSeq[String]
   
  def getStringsFromSolutionAsJava(solutionList:SolutionList, variableName:String): java.util.List[String]
  
  def getIdentFromSolution(solution:Solution, variableName:String): Ident
  
  def getIdentsFromSolution(solutionList:SolutionList, variableName:String): scala.collection.mutable.ArrayBuffer[Ident]
  
  def getIdentsFromSolutionAsJava(solutionList:SolutionList, variableName:String): java.util.List[Ident]
  
  def getFloatFromSolution(solutionMap:SolutionMap[Ident], selector:Ident, variableName:String): Float

  def getFloatFromSolution(solution:Solution, variableName:String, default:Float): Float
  
  def getDoubleFromSolution(solutionMap:SolutionMap[Ident], selector:Ident, variableName:String): Double
  
  def getDoubleFromSolution(solutionMap:SolutionMap[String], selector:String, variableName:String): Double
  
  def getIntegerFromSolution(solutionMap:SolutionMap[Ident], selector:Ident, variableName:String): Int
 
  /** Gets a boolean literal from a single query solution
   *
   * @param solution The Solution in which the desired solution is located
   * @param variableName The query variable name for the boolean literal desired
   * @return The selected boolean literal
   */
  def getBooleanFromSolution(solution:Solution, variableName:String): Boolean
  
}
