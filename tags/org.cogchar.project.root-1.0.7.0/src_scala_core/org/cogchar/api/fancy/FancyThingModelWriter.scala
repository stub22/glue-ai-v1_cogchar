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

package org.cogchar.api.fancy


import java.io.Reader;
import java.util.Iterator;
import org.appdapter.bind.csv.datmat.TestSheetReadMain;
import au.com.bytecode.opencsv.CSVReader;
import org.appdapter.core.name.FreeIdent;
import org.appdapter.core.name.Ident;
// import org.cogchar.impl.thing.basic.{BasicThingActionSpec, BasicTypedValueMap}


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
import org.cogchar.name.dir.{NamespaceDir}
import org.cogchar.name.thing.ThingCN;

/**
 * ...
 *
 * @author stub22
 * @author Jason Randolph Eads <jeads362@gmail.com>
 */
class FancyThingModelWriter extends BasicDebugger {
	

	val RDF_NS = NamespaceDir.RDF_NS; // "http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	val XSD_NS = NamespaceDir.XSD_NS; // "http://www.w3.org/2001/XMLSchema#"

	val CCRT_NS = NamespaceDir.CCRT_NS; // "urn:ftd:cogchar.org:2012:runtime#"
	
	val TA_NS = NamespaceDir.TA_NS; // "http://www.cogchar.org  /thing/action#"
	val GOODY_NS = NamespaceDir.GOODY_NS; // "urn:ftd:cogchar.org:2012:goody#"
	
	val P_rdfType = RDF_NS + "type";
	
	import java.util.Random;
	

	def writeTASpecToNewModel(tas : ThingActionSpec, ran: Random) : Model  = {
        if(tas == null){
          throw new NullPointerException("Unable to write null ThingActionSpec to model")
        }
		val m : Model = ModelFactory.createDefaultModel();
		val mci = new ModelClientImpl(m);
		val rr = new ResourceResolver(m, None);
		val actionSpecID : Ident = tas.getActionSpecID(); //Must always be present
        if(actionSpecID == null){
          throw new NullPointerException("Found ThingActionSpec with null actionSpecIdent: " + tas)
        }
		val actionSpecRes : Resource = mci.makeResourceForIdent(actionSpecID) //Must always be present
        
        
        val rdfTypeProp : Property = rr.findOrMakeProperty(m, P_rdfType)
		val taTypeRes : Resource =  rr.findOrMakeResource(m, ThingCN.T_ThingAction);
		val actTypeStmt = m.createStatement(actionSpecRes, rdfTypeProp, taTypeRes)
		m.add(actTypeStmt)
        
        // Collect the ThingAction's verb, if any
		val verbID : Ident = tas.getVerbID();
        if(verbID != null){
          val verbProp  : Property = rr.findOrMakeProperty(m, ThingCN.P_verb);
          val verbRes : Resource = mci.makeResourceForIdent(verbID)
          val actVerbStmt = m.createStatement(actionSpecRes, verbProp, verbRes)
          m.add(actVerbStmt)
        }
    
        // Collect the ThingAction's targetThing and targetThingType, if any
		val targetThing : Ident = tas.getTargetThingID();
        val targetThingType : Ident = tas.getTargetThingTypeID();
        if(targetThing != null){
          val targetThingProp  : Property = rr.findOrMakeProperty(m, ThingCN.P_targetThing);
          val thingRes : Resource = mci.makeResourceForIdent(targetThing)
          val actThingStmt = m.createStatement(actionSpecRes, targetThingProp, thingRes)
          m.add(actThingStmt)  
    
          if(targetThingType != null){
            val targetThingTypeProp  : Property = rr.findOrMakeProperty(m, ThingCN.P_targetThingType);
            val thingTypeRes :  Resource = mci.makeResourceForIdent(targetThingType)
            val actThingTypeStmt = m.createStatement(thingRes, rdfTypeProp, thingTypeRes)
            m.add(actThingTypeStmt)
          }
        }
    
		val srcAgentID : Ident	= tas.getSourceAgentID();
    
		val tvm : TypedValueMap = tas.getParamTVM();
    
        //TODO: this may need additional validation to avoid bad data in the system.
		val postedStampMSec : java.lang.Long = tas.getPostedTimestamp();
        if( postedStampMSec != null ) {
          val postedTSMProp  : Property = rr.findOrMakeProperty(m, ThingCN.P_postedTSMsec);
          val tstampLit : Literal = mci.makeTypedLiteral(postedStampMSec.toString(), XSDDatatype.XSDlong);
          val actStampStmt = m.createStatement(actionSpecRes, postedTSMProp, tstampLit);
          m.add(actStampStmt)
        }

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
	
	// In the weak convention, each param is held in a separate record attached to the parent thing.
	def writeParamsUsingWeakConvention(mci : ModelClientImpl,  actionRes : Resource, tvm : TypedValueMap, ran: Random) : Unit = {
		val m : Model = mci.getModel;

		val rr = new ResourceResolver(m, None);
		
        // TODO: Extract this to ontology?
        // Declare URIs
		val paramLabelProp : Property = rr.findOrMakeProperty(m, ThingCN.P_paramIdent)
        val paramValueProp : Property = rr.findOrMakeProperty(m, ThingCN.P_paramValue)
		val rdfTypeProp : Property = rr.findOrMakeProperty(m, P_rdfType);
		val identAttachedToThingActionProp  : Property = rr.findOrMakeProperty(m, ThingCN.P_IdentAttachedToThingAction);
		
		val ptRes : Resource = rr.findOrMakeResource(m, ThingCN.T_ThingActionParam);
		
		// val tm = com.hp.hpl.jena.datatypes.TypeMapper.getInstance()
		val nameIter : Iterator[Ident]  = tvm.iterateKeys();
		
		val random = new java.util.Random
		val pBaseName = "tap_" + ran.nextInt + "_";
		var paramNum = 1;
		while (nameIter.hasNext()) {
			val pName : String = pBaseName + paramNum;
			val pRes : Resource = mci.makeResourceForURI(CCRT_NS + pName);
			
			val paramLabelID : Ident = nameIter.next();
//			 val paramProp : Property = rr.findOrMakeProperty(m, paramID.getAbsUriString)
			val paramLabelRes : Resource = mci.makeResourceForIdent(paramLabelID)
			
            // TODO: Test for robust handling
			val ptStmt = m.createStatement(pRes, rdfTypeProp, ptRes) 
			val paStmt = m.createStatement(pRes, identAttachedToThingActionProp, actionRes) 
			val plStmt = m.createStatement(pRes, paramLabelProp, paramLabelRes) 
            m.add(ptStmt);
			m.add(paStmt);
			m.add(plStmt);
            
            // Collect the param value
            val pvRaw = tvm.getRaw(paramLabelID)
            
            // Aquire writable node
			val pvNode : RDFNode = pvRaw match  {
				case idVal: Ident =>  mci.makeResourceForIdent(idVal)
				case other =>  m.createTypedLiteral(other)
			}
            
            
            val pValStmt = m.createStatement(pRes, paramValueProp, pvNode)
            m.add(pValStmt)
//            val paramIdentValueProp : Property = rr.findOrMakeProperty(m, ThingCN.P_paramIdentValue)
//            val paramStringValueProp : Property = rr.findOrMakeProperty(m, ThingCN.P_paramStringValue)
//            val paramIntValueProp : Property = rr.findOrMakeProperty(m, ThingCN.P_paramIntValue)
//            val paramFloatValueProp : Property = rr.findOrMakeProperty(m, ThingCN.P_paramFloatValue)
//            // TODO: This needs to be tested for detection of non-string params
//            if(pvRaw.isInstanceOf[String]) {
//              val pValStmt = m.createStatement(pRes, paramStringValueProp, pvNode)
//              m.add(pValStmt)
//            }
//            else if (pvRaw.isInstanceOf[Ident]) {
//              val pValStmt = m.createStatement(pRes, paramIdentValueProp, pvNode)
//              m.add(pValStmt)
//            }
//            else if (pvRaw.isInstanceOf[Int]) {
//              val pValStmt = m.createStatement(pRes, paramIntValueProp, pvNode)
//              m.add(pValStmt)
//            }
//            else if (pvRaw.isInstanceOf[Float]) {
//              val pValStmt = m.createStatement(pRes, paramFloatValueProp, pvNode)
//              m.add(pValStmt)
//            }
            
			paramNum = paramNum + 1
		}
	}
    
	// Unused, so far.  In the stron convention, params are simply written as properties of the parent (probly the ActionSpec)
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
	
}
