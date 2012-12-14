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

class FancyThingSparqlGen {
	
}
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

class FancyThingModelWriter {
	def writeParamsUsingWeakConvention(m : Model,  parentRes : Resource, tvm : TypedValueMap) : Unit = {
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
	def writeTASpecToNewModel(tas : ThingActionSpec) : Model  = {
		val model : Model = ModelFactory.createDefaultModel();
		val actionSpec : Ident = tas.getActionSpecID();
		val verb : Ident = tas.getVerbID();
		val targetThing : Ident = tas.getTargetThingID();
		val srcAgentID : Ident	= tas.getSourceAgentID();
		val tvm : TypedValueMap = tas.getParamTVM();
		
		val mci = new ModelClientImpl(model);
		val thingRes : Resource = mci.makeResourceForIdent(targetThing)
		
		writeParamsUsingWeakConvention(model, thingRes, tvm)
		println("thingRes=" + thingRes)
		println("model after write=" + model)
		model;

	}
	import java.io.ByteArrayOutputStream;	
	
	def writeTASpecToString (tas : ThingActionSpec) : String = {
		val specModel  = writeTASpecToNewModel(tas)
		
		val baos : ByteArrayOutputStream = new ByteArrayOutputStream();
		val outLang = "TURTLE";
		specModel.write(baos, outLang);
		val encoding = "UTF8";
		val turtleTriples = baos.toString(encoding)
		
		val stg = new SparqlTextGen(specModel);
		
		val tgtGraphQN = "ccrt:user_access_sheet_22";

		val upRqTxt = stg.emitSingleGraphInsert(tgtGraphQN, turtleTriples);
		
		upRqTxt;
	}
}
