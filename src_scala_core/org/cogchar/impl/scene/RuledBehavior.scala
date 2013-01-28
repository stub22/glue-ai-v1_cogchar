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

package org.cogchar.impl.scene

import org.cogchar.impl.perform.{ChannelSpec};

import org.appdapter.core.name.{Ident, FreeIdent};
import org.appdapter.core.item.{Item, JenaResourceItem}
import org.appdapter.core.log.{BasicDebugger, Loggable};

import org.appdapter.bind.rdf.jena.assembly.ItemAssemblyReader;
import org.appdapter.bind.rdf.jena.model.JenaModelUtils;
import org.appdapter.bind.rdf.jena.query.SPARQL_Utils;
import org.appdapter.bind.rdf.jena.reason.JenaReasonerUtils;


import org.appdapter.module.basic.{EmptyTimedModule,BasicModulator}
import org.appdapter.api.module.{Module, Modulator}
import org.appdapter.api.module.Module.State;


import com.hp.hpl.jena.assembler.{Assembler, Mode}
import com.hp.hpl.jena.assembler.assemblers.AssemblerBase;
import com.hp.hpl.jena.rdf.model.{Resource, Model, InfModel, ModelFactory};
import com.hp.hpl.jena.query.{ResultSet, ResultSetFormatter, ResultSetFactory, ResultSetRewindable};
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;


/**
 * @author Stu B. <www.texpedient.com>
 */

class RuledBehavior (myRBS: RuledBehaviorSpec) extends Behavior(myRBS) {
	var	myInfModel : InfModel = null;
	
	override protected def doStart(scn : BScene) {
		super.doStart(scn);
		val baseModel = ModelFactory.createDefaultModel();
		val sillyProperty = baseModel.createProperty("urn:sillyNamespace#", "sillyProperty");
		
		// Create a statement about each known channel
		for (val cs <- scn.mySceneSpec.myChannelSpecs.values) {
			val csJRI = cs.getIdent().asInstanceOf[JenaResourceItem];
			val csJenaRes = csJRI.getJenaResource();
			val sillyLit = baseModel.createTypedLiteral(cs.getShortLabel(), XSDDatatype.XSDstring);
			val stmt = baseModel.createStatement(csJenaRes, sillyProperty, sillyLit);
			logInfo("Created statement: " + stmt);
			baseModel.add(stmt);
		}
		// We are paying this serializatio-to-string cost on every start!
		logInfo("Built dummy base model [" + baseModel + "]");
		myInfModel = JenaReasonerUtils.createInferenceModelUsingGenericRulesWithMacros (baseModel, myRBS.myJenaGeneralRules);
	}
	override protected def doRunOnce(scn : BScene,  runSeqNum : Long) {
		/*
		//	1) Check for stopping fact, and mark ourselves stopped if we're told to.
		//		markStopRequested();
		//		logMe("Finished requesting stop, so this should be my last runOnce().");
		//		
		//	2) Check for "new" action facts, and act on them!
		First options for fact queries:
			A) SPARQL - (using regular text syntax, or pre-parsed SPIN) query to produce a result set.
				Recent SPARQL versions support path-based queries.  Is that in Jena 2.6.4?
						If not, we could still use FresnelQuery, but it's becoming obsolete.
						
				SPARQL-UPDATE is supported, so we can treat it as a command language for marking models.
				
			B) Jena API - treat the model as a java collection of statement objects.
		*/
	   
		logInfo("********************************************************************\nExecuting query");
		
		// Could speed this up by caching the parsed query
		val resultSet = SPARQL_Utils.execQueryToProduceResultSet(myInfModel, myRBS.mySparqlQuery);
		
		val rsRewindable = ResultSetFactory.makeRewindable(resultSet);
		val resultXML = ResultSetFormatter.asXMLString(rsRewindable);
		// This XML can be routed/transformed as a packet using XLST, Dom4J, Cocoon, XProc, or other XML services.
		logInfo("Got resultXML:\n" + resultXML);
		rsRewindable.reset();
		while (rsRewindable.hasNext()) {
			val qSoln = rsRewindable.next();
			logInfo("Got qsoln" + qSoln + " with s=[" + qSoln.get("s") + "], p=[" + qSoln.get("p") + "], o=[" 
							+ qSoln.get("o") +"]");
		}
		
		// Since action-processing logic is not ready, currently we always stop immediately
		logInfo("********************************************************************\nRequesting Stop");
		markStopRequested();
	}
	override protected def doStop(scn : BScene) {
		logInfo("##############################################  Stopping");
	}
}
class RuledBehaviorSpec() extends BehaviorSpec {	
	import scala.collection.JavaConversions._;	
	
	var		myJenaGeneralRules : String = "";
	var		mySparqlQuery : String = "";

	// The field summary is used only for logging
	override def getFieldSummary() : String = {
		return  super.getFieldSummary() +  ", rules=" + myJenaGeneralRules + ", query=" + mySparqlQuery;
	}
	
	override def makeBehavior() : Behavior = {
		new RuledBehavior(this);
	}
	override def completeInit(configItem : Item, reader : ItemAssemblyReader, assmblr : Assembler , mode: Mode) {
		myDetails = "spar-QLY!";
		
		myJenaGeneralRules = reader.readConfigValString(configItem.getIdent(), SceneFieldNames.P_rules, configItem, null);
		mySparqlQuery = reader.readConfigValString(configItem.getIdent(), SceneFieldNames.P_query, configItem, null);

	}
}

/*
 *  From:  http://jena.sourceforge.net/inference/#rules
 *
 * To keep rules readable qname syntax is supported for URI refs. The set of known prefixes is those registered 
 * with the PrintUtil object. This initially knows about rdf, rdfs, owl, daml, xsd and a test namespace eg, but 
 * more mappings can be registered in java code. In addition it is possible to define additional prefix mappings 
 * in the rule file, see below.
 * 
 * Rule files may be loaded and parsed using:

List rules = Rule.rulesFromURL("file:myfile.rules");
or
BufferedReader br = /* open reader */ ;
List rules = Rule.parseRules( Rule.rulesParserFromReader(br) );
or
String ruleSrc = /* list of rules in line */
List rules = Rule.parseRules( rulesSrc );
In the first two cases (reading from a URL or a BufferedReader) the rule file is preprocessed by a simple processor which strips comments and supports some additional macro commands:
# ...
A comment line.
// ...
A comment line.

@prefix pre: <http://domain/url#>.
Defines a prefix pre which can be used in the rules. The prefix is local to the rule file.

@include <urlToRuleFile>.
Includes the rules defined in the given file in this file. The included rules will appear before the user defined rules, irrespective of where in the file the @include directive appears. A set of special cases is supported to allow a rule file to include the predefined rules for RDFS and OWL - in place of a real URL for a rule file use one of the keywords RDFS OWL OWLMicro OWLMini (case insensitive).
So an example complete rule file which includes the RDFS rules and defines a single extra rule is:

# Example rule file
@prefix pre: <http://jena.hpl.hp.com/prefix#>.
@include <RDFS>.

[rule1: (?f pre:father ?a) (?u pre:brother ?f) -> (?u pre:uncle ?a)]
 * 
 */