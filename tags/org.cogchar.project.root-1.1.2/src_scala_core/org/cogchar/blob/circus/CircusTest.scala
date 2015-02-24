/*
 *  Copyright 2015 by The Cogchar Project (www.cogchar.org).
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

package org.cogchar.blob.circus
import org.appdapter.fancy.log.VarargsLogging

import org.apache.jena.riot.RDFDataMgr

import org.cogchar.blob.audit._

import org.cogchar.gen.indiv.{BootSample_2015Q1_owl2 => BSamp}

import org.cogchar.api.owrap.crcp._

import org.ontoware.rdf2go
import com.hp.hpl.jena



/**
 */

object CircusTest extends VarargsLogging with AuditSvcFinderGlobal {
	// val myReactorModel: rdf2go.model.Model = new rdf2go.impl.jena.ModelImplJena(myLocalModel)
	def main(args: Array[String]) : Unit = {
		// Must enable "compile" or "provided" scope for Log4J dep in order to compile this code.
		org.apache.log4j.BasicConfigurator.configure();
		org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.ALL);
		info0("Starting CircusTest")
		val auditSvcTest = findSenderSvc
		val bootRecipe_ji = BSamp.CIRCUS_SAMP_BOOT_11
		info1("CIRCUS_SAMP_BOOT_11={}", BSamp.CIRCUS_SAMP_BOOT_11)
		
		// val bootRecipe = new CircusRecipe()
		val pathInOntoBundle_indivs = "org/cogchar/onto_indiv/"
		val bootSamp_Filename = "bootSample_2015Q1_owl2.ttl"
		
		val bootSamp_pathInOntoBundle = pathInOntoBundle_indivs + bootSamp_Filename
		val bootSamp_jenaModel = RDFDataMgr.loadModel(bootSamp_pathInOntoBundle)
		info2("Loaded bootSamp model from {}, found contents {}", bootSamp_pathInOntoBundle, bootSamp_jenaModel)
		
		val bootSamp_r2goModel : rdf2go.model.Model = new rdf2go.impl.jena.ModelImplJena(bootSamp_jenaModel)
		if (!bootSamp_r2goModel.isOpen) {
			bootSamp_r2goModel.open
		}
		val bootRecipe_r2go = new CircusRecipe(bootSamp_r2goModel, bootRecipe_ji.getURI, false)
		info1("Got bootRecipe_r2go: {}", bootRecipe_r2go)
		val outputs = bootRecipe_r2go.getAllOutRecipe_as.asArray
		info1("Got outputs: {}", outputs)

	}
}

