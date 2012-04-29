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
import org.appdapter.core.item.{Ident, Item, FreeIdent}
import org.appdapter.bind.rdf.jena.assembly.ItemAssemblyReader;


import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;
import com.hp.hpl.jena.assembler.assemblers.AssemblerBase;
import com.hp.hpl.jena.rdf.model.Resource;

import org.appdapter.module.basic.{EmptyTimedModule,BasicModulator}
import org.appdapter.api.module.{Module, Modulator}
import org.appdapter.api.module.Module.State;

import org.cogchar.impl.perform.{ChannelSpec};
import org.appdapter.core.log.{BasicDebugger, Loggable};
/**
 * @author Stu B. <www.texpedient.com>
 */

class RuledBehavior (rbs: RuledBehaviorSpec) extends Behavior(rbs) {
	override protected def doRunOnce(scn : BScene,  runSeqNum : Long) {
		/*
		//	1) Check for stopping fact, and mark ourselves stopped if we're told to.
		//		markStopRequested();
		//		logMe("Finished requesting stop, so this should be my last runOnce().");
		//		
		//	2) Check for "new" action facts, and act on them!
		First options for fact queries:
			SPARQL - (using regular text syntax, or pre-parsed SPIN) query to produce a result set.
				Recent SPARQL versions support path-based queries.  Is that in Jena 2.6.4?
				SPARQL-UPDATE is supported, so we can treat it as a command language for marking models.
			Jena API - treat the model as a java collection of statement objects.
		*/
	   
		
		
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
	}
}