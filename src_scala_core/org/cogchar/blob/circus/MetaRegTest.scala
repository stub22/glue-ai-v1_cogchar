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

import org.cogchar.gen.indiv.{ BootSample_2015Q1_owl2 => BSamp }

import org.cogchar.api.owrap.crcp._

import org.ontoware.rdf2go
import com.hp.hpl.jena

/**
 * This code (along with rest of circus package) will probably be promoted to Appdapter eventually, but it is
 * easier for now to prototype with it in the Cogchar layer of Glue.AI..  It is not Cogchar feature-specific.
 
 * MetaReg holds a set of feature-specific registry graphs and associated object maps.
 * Can be traversed in the small and also queried in the large.
 */

object MetaRegTest  extends VarargsLogging {
	def main(args: Array[String]): Unit = {
		org.apache.log4j.BasicConfigurator.configure();
		org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.ALL);
		info0("Starting MetaRegTest")
	}
	
}

