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

import org.appdapter.core.matdat.{SheetRepo}
import com.hp.hpl.jena.rdf.model.{Model, Statement, Resource, Property, Literal, RDFNode, ModelFactory, InfModel}

import com.hp.hpl.jena.query.{Query, QueryFactory, QueryExecution, QueryExecutionFactory, QuerySolution, QuerySolutionMap, Syntax};
import com.hp.hpl.jena.query.{Dataset, DatasetFactory, DataSource};
import com.hp.hpl.jena.query.{ResultSet, ResultSetFormatter, ResultSetRewindable, ResultSetFactory};

/**
 * @author Stu B. <www.texpedient.com>
 */

object HumanoidConfigEmitter {

	def main(args: Array[String]) : Unit = {

		val sr : SheetRepo = SheetRepo.loadTestSheetRepo()
		val qText = sr.getQueryText("ccrt:qry_sheet_22", "ccrt:find_humanoids_99")
		println("Found query text: " + qText)
		
		val parsedQ = sr.parseQueryText(qText);
		val solnJavaList : java.util.List[QuerySolution] = sr.findAllSolutions(parsedQ, null);
		println("Found solutions: " + solnJavaList)
	}

}
