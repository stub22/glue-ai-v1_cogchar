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

import scala.collection.JavaConversions._

/**
 * @author Stu B. <www.texpedient.com>
 */

object HumanoidConfigEmitter {
  
  final val QUERY_SHEET = "ccrt:qry_sheet_22"
  final val HUMANOID_QUERY = "ccrt:find_humanoids_99"
  
  final val ROBOT_URI_VAR_NAME = "humInd"
  final val ROBOT_ID_VAR_NAME = "rkRobotID"
  final val MESH_PATH_VAR_NAME = "meshPath"
  final val BONY_CONFIG_PATH_VAR_NAME = "bonyConfigPath"
  final val JOINT_CONFIG_PATH_VAR_NAME = "jointConfigPath"
  final val INITIAL_POSITION_VAR_NAMES = Array("initX", "initY", "initZ")
  final val PHYSICS_FLAG_VAR_NAME = "physics"
  final val BONE_VAR_NAME = "boneName"
  
  val solutionMap = new scala.collection.mutable.HashMap[Ident, QuerySolution]
  
  def main(args: Array[String]) : Unit = {

	val sr : SheetRepo = SheetRepo.loadTestSheetRepo()
	val qText = sr.getQueryText(QUERY_SHEET, HUMANOID_QUERY)
	println("Found query text: " + qText)
		
	val parsedQ = sr.parseQueryText(qText);
	val solnJavaList : java.util.List[QuerySolution] = sr.findAllSolutions(parsedQ, null);
	println("Found solutions: " + solnJavaList)
  }
	
  def getCharConfigData {
	//val sr : SheetRepo = SheetRepo.loadTestSheetRepo()
	val sr : SheetRepo = QueryEmitter.getSheet // By getting sr this way, we use the cached copy and only need load it once for init
	val qText = sr.getQueryText(QUERY_SHEET, HUMANOID_QUERY)
	val parsedQ = sr.parseQueryText(qText);
	val solnList : scala.collection.mutable.Buffer[QuerySolution] = sr.findAllSolutions(parsedQ, null);
	solnList.foreach(solution => {
		if (solution contains ROBOT_URI_VAR_NAME) {
		  solutionMap(new FreeIdent(solution.getResource(ROBOT_URI_VAR_NAME).getURI, solution.getResource(ROBOT_URI_VAR_NAME).getLocalName)) = solution
		}
	  })
  }
  
  def ensureMapReady {
	if (solutionMap isEmpty) {
	  getCharConfigData
	}
  }
	
  def getRobotIdentsAsScala: scala.collection.Set[Ident] = {
	ensureMapReady
	solutionMap.keySet
  }
  
  def getRobotIdents: java.util.Set[Ident] = getRobotIdentsAsScala
  
  def getRobotId(robotIdent:Ident): String = {
	getStringFromSolution(robotIdent, ROBOT_ID_VAR_NAME)
  }
  
  def getRobotId(robotUri:String): String = {
	var robotIdent: Ident = null
	getRobotIdentsAsScala.foreach(ident => {
		if (ident.getAbsUriString.equals(robotUri)) robotIdent = ident
	  })
	var robotId: String = null;
	if (robotIdent != null) robotId = getRobotId(robotIdent)
	robotId
  }
  
  // This is a "band-aid" method to get a robot ident from its ccrt:rkRobotID
  // Used by some old ItemFuncs based RDF init, likely unnecessary after full conversion to query based config
  def getRobotIdent(robotId:String): Ident = {
	var robotIdent: Ident = null
	ensureMapReady
	solutionMap.keySet.foreach(ident => {
		if (getRobotId(ident).equals(robotId)) robotIdent = ident
	  })
	robotIdent
  }
  
  def getMeshPath(robotIdent:Ident): String = {
	getStringFromSolution(robotIdent, MESH_PATH_VAR_NAME)
  }
  
  def getBonyConfigPath(robotIdent:Ident): String = {
	getStringFromSolution(robotIdent, BONY_CONFIG_PATH_VAR_NAME)
  }
  
  def getJointConfigPath(robotIdent:Ident): String = {
	getStringFromSolution(robotIdent, JOINT_CONFIG_PATH_VAR_NAME)
  }
  
  def getStringFromSolution(robotIdent:Ident, variableName:String) = {
	ensureMapReady
	var literal: String = null
	if (solutionMap contains robotIdent) {
	  literal = solutionMap(robotIdent).getLiteral(variableName).getString
	}
	literal
  }
  
  def getInitialPosition(robotIdent:Ident) = {
	ensureMapReady
	var position = new Array[Float](3)
	if (solutionMap contains robotIdent) {
	  for (i <- 0 until position.length) position(i) = solutionMap(robotIdent).getLiteral(INITIAL_POSITION_VAR_NAMES(i)).getFloat
	}
	position
  }
  
  def getPhysicsFlag(robotIdent:Ident) = {
	ensureMapReady
	var flag = false
	if (solutionMap contains robotIdent) {
	  flag = solutionMap(robotIdent).getLiteral(PHYSICS_FLAG_VAR_NAME).getBoolean
	}
	flag
  }
  
}
