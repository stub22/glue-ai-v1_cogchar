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

import java.io.Reader;
import java.util.Iterator;
import org.appdapter.bind.csv.datmat.TestSheetReadMain;
import au.com.bytecode.opencsv.CSVReader;

import org.appdapter.core.log.BasicDebugger;

import com.hp.hpl.jena.rdf.model.{Model, Statement, Resource, Property, Literal, RDFNode, ModelFactory}
import com.hp.hpl.jena.query.{ResultSet, ResultSetFormatter, ResultSetRewindable, ResultSetFactory, QuerySolution};
import com.hp.hpl.jena.ontology.{OntProperty, ObjectProperty, DatatypeProperty}
import com.hp.hpl.jena.datatypes.{RDFDatatype, TypeMapper}
import com.hp.hpl.jena.datatypes.xsd.{XSDDatatype}
import com.hp.hpl.jena.shared.{PrefixMapping}


import com.hp.hpl.jena.shared.{PrefixMapping}

class SparqlTextGen(val myPrefixMap : PrefixMapping ) {
	def emitPrefixDeclarationLine(abbrev : String) : String = {
		val fullUri = myPrefixMap.getNsPrefixURI(abbrev)
		"PREFIX " + abbrev + ": <" + fullUri + ">\n"
	}
	def emitAllPrefixDeclarations() : String = {
		val buffer = new StringBuffer("");
		val keys = myPrefixMap.getNsPrefixMap().keySet()
		val kit : Iterator[String] = keys.iterator();
		while (kit.hasNext()) {
			val abbrev = kit.next();
			val decl = emitPrefixDeclarationLine(abbrev)
			buffer.append(decl)
		}
		buffer.toString()
	}
	
	def emitSingleGraphInsert(graphQName : String, bodyTurtle : String) : String = {
		val allDeclsText : String = emitAllPrefixDeclarations();
		allDeclsText + "INSERT DATA {  GRAPH  " + graphQName + " { \n" + bodyTurtle + "}}\n"
	}
}
/*
	static String PREFIX_FOAF = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n",
			PREFIX_XSD = "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n",
			PREFIX_DC = "PREFIX dc:      <http://purl.org/dc/elements/1.1/>\n",
			PREFIX_DBO = "PREFIX dbo: <http://dbpedia.org/ontology/>\n",
			PREFIX_BOOKS = "PREFIX books:   <http://example.org/book/>\n",
			PREFIX_CCRT = "PREFIX ccrt:  <urn:ftd:cogchar.org:2012:runtime#>\n",
			PREFIX_UA = "PREFIX ua:    <http://www.cogchar.org/lift/user/config#>\n";
*/
object SparqlTextGen extends BasicDebugger {
	def main(args: Array[String]) : Unit = {
		// forceLog4jConfig();		
		val prefixModel : Model = ModelFactory.createDefaultModel();
		
		prefixModel.setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
		prefixModel.setNsPrefix("dc", "http://purl.org/dc/elements/1.1/");
		
		val stg = new SparqlTextGen(prefixModel);
		
		
		println (stg.emitPrefixDeclarationLine("xsd"));
		println (stg.emitPrefixDeclarationLine("dc"));
		
		val allDecls = stg.emitAllPrefixDeclarations();
		
		println("AllDecls:\n" + allDecls);
		
		val testGraphQN = "ccrt:user_access_sheet_22";
		val testBodyTTL = "<http://weird.com/stuff> dc:title 'Lets all be revolting' ;    dc:creator 'Ernesto G.'. ";
		val upRqTxt = stg.emitSingleGraphInsert(testGraphQN, testBodyTTL);
		println("Update Request:\n" + upRqTxt)
		val tm = com.hp.hpl.jena.datatypes.TypeMapper.getInstance()
		println("Type for bloop = " + tm.getTypeByValue("bloop"))
		println("Type for 22 = " + tm.getTypeByValue(22))
		println("Type for 38.3 = " + tm.getTypeByValue(38.3))
		
		
	}

}
