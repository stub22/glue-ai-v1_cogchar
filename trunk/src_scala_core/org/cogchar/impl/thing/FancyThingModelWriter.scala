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

package org.cogchar.impl.thing


import java.io.Reader;
import java.util.Iterator;
import org.appdapter.bind.csv.datmat.TestSheetReadMain;
import au.com.bytecode.opencsv.CSVReader;
import org.appdapter.core.name.FreeIdent;
import org.appdapter.core.name.Ident;
import org.cogchar.api.thing.BasicThingActionSpec;
import org.cogchar.api.thing.BasicTypedValueMap;


import org.appdapter.core.log.BasicDebugger;

import com.hp.hpl.jena.rdf.model.{Model, Statement, Resource, Property, Literal, RDFNode, ModelFactory}
import com.hp.hpl.jena.query.{ResultSet, ResultSetFormatter, ResultSetRewindable, ResultSetFactory, QuerySolution};
import com.hp.hpl.jena.ontology.{OntProperty, ObjectProperty, DatatypeProperty}
import com.hp.hpl.jena.datatypes.{RDFDatatype, TypeMapper}
import com.hp.hpl.jena.datatypes.xsd.{XSDDatatype}
import com.hp.hpl.jena.shared.{PrefixMapping}

import org.cogchar.api.thing.{TypedValueMap, ThingActionSpec}

import org.appdapter.impl.store.{ModelClientImpl, ResourceResolver};
import org.cogchar.blob.emit.{SparqlTextGen}

class FancyThingModelWriter extends BasicDebugger {
	

	val RDF_NS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	val XSD_NS = "http://www.w3.org/2001/XMLSchema#"

	val CCRT_NS = "urn:ftd:cogchar.org:2012:runtime#"
	
	val TA_NS = "http://www.cogchar.org/thing/action#"
	val GOODY_NS = "urn:ftd:cogchar.org:2012:goody#"
	
	
	import java.util.Random;
	
	def writeParamsUsingWeakConvention(mci : ModelClientImpl,  actionRes : Resource, tvm : TypedValueMap, ran: Random) : Unit = {
		val m : Model = mci.getModel;

		val rr = new ResourceResolver(m, None);

		// rdf:type	ta:targetAction	ta:verb	ta:targetThing	ta:paramIdent	ta:paramValue
		
		val paramLabelProp : Property = rr.findOrMakeProperty(m, TA_NS + "paramIdent")
		val paramValueProp : Property = rr.findOrMakeProperty(m, TA_NS + "paramValue")
		val rdfTypeProp : Property = rr.findOrMakeProperty(m, RDF_NS + "type")
		val targetActionProp  : Property = rr.findOrMakeProperty(m, TA_NS + "targetAction")
		
		val ptRes : Resource = rr.findOrMakeResource(m, CCRT_NS + "ThingActionParam")
		
		// val tm = com.hp.hpl.jena.datatypes.TypeMapper.getInstance()
		val nameIter : Iterator[Ident]  = tvm.iterateKeys();
		
		val random = new java.util.Random
		val pBaseName = "tap_" + ran.nextInt + "_";
		var paramNum = 1;
		while (nameIter.hasNext()) {
			val pName : String = pBaseName + paramNum;
			val pRes : Resource = mci.makeResourceForURI(CCRT_NS + pName);
			
			val paramLabelID : Ident = nameIter.next();
			// val paramProp : Property = rr.findOrMakeProperty(m, paramID.getAbsUriString)
			val paramLabelRes : Resource = mci.makeResourceForIdent(paramLabelID)
			val pvRaw = tvm.getRaw(paramLabelID)
			val pvNode : RDFNode = pvRaw match  {
				case idVal: Ident =>  mci.makeResourceForIdent(idVal)
				case other =>  m.createTypedLiteral(other)
			}
			val ptStmt = m.createStatement(pRes, rdfTypeProp, ptRes) 
			val paStmt = m.createStatement(pRes, targetActionProp, actionRes) 
			val plStmt = m.createStatement(pRes, paramLabelProp, paramLabelRes) 
			val pvStmt = m.createStatement(pRes, paramValueProp, pvNode)
			
			m.add(ptStmt);
			m.add(paStmt);
			m.add(plStmt);
			m.add(pvStmt);
			paramNum = paramNum + 1
		}
	}
	def writeTASpecToNewModel(tas : ThingActionSpec, ran: Random) : Model  = {
		val m : Model = ModelFactory.createDefaultModel();
		val actionSpecID : Ident = tas.getActionSpecID();
		val verbID : Ident = tas.getVerbID();
		val targetThing : Ident = tas.getTargetThingID();
		val targetThingType : Ident = tas.getTargetThingTypeID();
		val srcAgentID : Ident	= tas.getSourceAgentID();
		val tvm : TypedValueMap = tas.getParamTVM();

		val mci = new ModelClientImpl(m);
		val rr = new ResourceResolver(m, None);
		
		val rdfTypeProp : Property = rr.findOrMakeProperty(m, RDF_NS + "type")
		val verbProp  : Property = rr.findOrMakeProperty(m, TA_NS + "verb")
		val targetThingProp  : Property = rr.findOrMakeProperty(m, TA_NS + "targetThing")		

		val taTypeRes : Resource =  rr.findOrMakeResource(m, CCRT_NS + "ThingAction")
		
		val actionSpecRes : Resource = mci.makeResourceForIdent(actionSpecID)
		val thingRes : Resource = mci.makeResourceForIdent(targetThing)
		val thingTypeRes :  Resource = mci.makeResourceForIdent(targetThingType)
		val verbRes : Resource = mci.makeResourceForIdent(verbID)


		// Write statements for TAS top facts
		// rdf:type	ta:verb	ta:targetThing
		val actTypeStmt = m.createStatement(actionSpecRes, rdfTypeProp, taTypeRes)
		val actVerbStmt = m.createStatement(actionSpecRes, verbProp, verbRes)
		val actThingStmt = m.createStatement(actionSpecRes, targetThingProp, thingRes)
		val actThingTypeStmt = m.createStatement(thingRes, rdfTypeProp, thingTypeRes)
		
		m.add(actTypeStmt)
		m.add(actVerbStmt)
		m.add(actThingStmt)
		m.add(actThingTypeStmt)
		
		writeParamsUsingWeakConvention(mci, actionSpecRes, tvm, ran)
		// println("thingRes=" + thingRes)
		// println("model after write=" + m)
		m;
	}
	import java.io.ByteArrayOutputStream;	
	
	def writeTASpecToString (tas : ThingActionSpec, tgtGraphQN : String, ran : Random) : String = {
		val specModel : Model  = writeTASpecToNewModel(tas, ran)
		
		specModel.setNsPrefix("rdf", RDF_NS);
		specModel.setNsPrefix("xsd" , XSD_NS)
		
		specModel.setNsPrefix("ccrt" , CCRT_NS)
		specModel.setNsPrefix("ta" , TA_NS)
		specModel.setNsPrefix("goody" , GOODY_NS)
		
		val baos : ByteArrayOutputStream = new ByteArrayOutputStream();
		val outLang = "TURTLE";
		specModel.write(baos, outLang);
		val encoding = "UTF8";
		val turtleTriples = baos.toString(encoding)
		
		val lastPreStart = turtleTriples.lastIndexOf("@prefix");
		val lastPreEnd = turtleTriples.indexOf("\n", lastPreStart)
		
		val turtleTriplesBare = turtleTriples.substring(lastPreEnd)
		
		val stg = new SparqlTextGen(specModel);
		
		val upRqTxt = stg.emitSingleGraphInsert(tgtGraphQN, turtleTriplesBare);
		
		upRqTxt;
	}
	
	// Unused
	/*
	def writeParamsUsingStrongConvention(m : Model,  parentRes : Resource, tvm : TypedValueMap) : Unit = {
		val mci = new ModelClientImpl(m);
		val rr = new ResourceResolver(m, None);
		// val tm = com.hp.hpl.jena.datatypes.TypeMapper.getInstance()
		val nameIter : Iterator[Ident]  = tvm.iterateKeys();
		while (nameIter.hasNext()) {
			val paramID : Ident = nameIter.next();
			// val paramProp : Property = rr.findOrMakeProperty(m, paramID.getAbsUriString)
			val paramPropRes : Resource = mci.makeResourceForIdent(paramID)
			val paramProp : Property = paramPropRes.as(classOf[Property])
			val pvRaw = tvm.getRaw(paramID)
			val pvNode : RDFNode = pvRaw match  {
				case idVal: Ident =>  mci.makeResourceForIdent(idVal)
				case other =>  m.createTypedLiteral(other)
			}
			val stmt = m.createStatement(parentRes, paramProp, pvNode) 
			m.add(stmt)
		}
	}
	*/
}
