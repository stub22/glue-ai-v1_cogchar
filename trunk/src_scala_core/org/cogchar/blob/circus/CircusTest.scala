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
 */

object CircusTest extends VarargsLogging with AuditSvcFinderGlobal {
  // val myReactorModel: rdf2go.model.Model = new rdf2go.impl.jena.ModelImplJena(myLocalModel)
  def main(args: Array[String]): Unit = {
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
		info1("Got output recipes, serving as seeds for circus-node space, and as column-keys for result value rows: {}", outputs)
		
		// Each output recipe is now instantiated as an output node in our circus.
		// We connect each such node to input nodes corresponding to their input recipes, with exactly one
		// node for each recipe individual URI(/bNode).   We contine connecting input nodes until the input
		// set is closed, giving us a runnable node circus.  
		// To run the circus we:
		// 1) Clear node values
		// 2) Set externally known input node values
		// 3) Solve for all node values needed to determine the output node values.
		// In principle it is possible to set the same node's value more than once during processing step #3,
		// but in general we are only interested in the final (settled) values, and we expect the circus solver to 
		// manage whatever intermediate results there are effectively.
		// The output (frame) from step 3 is a result row/map value, with keys corresponding to output recipe individuals 
		// (or hasOutputXyz specialization properties of the circusRecipe individual, when preferred), and values
		// typed by the corresponding recipe types.
		
		// As of 2015-02-25, we have 8 main categories of recipe available to upstream authors+systems to supply us with.
		// These are essentially the value types of our implied expression language, instances of which may be bound
		// to nodes of the running circus.  Alphabetically they are:
		// 
		// Broker, Circus, GHost, Graph, Literal, Matrix, QueryGrSet, Uri
		// 
		// 6 of these 8 represent chunks of data, of two main superkinds:  Graph and Matrix.
		// GHosts represent all input/output RDF files, disk-folders, and local databases.
		// The data they contain is referred to thru the forms of (subkinds of) Graph, Uri, Literal, and QueryGrSet.
		// Matrix is the other data-data type, covering the kinds of data that don't efficiently fit into graphs.
		// Broker and Circus are objects defining the OO programmer's boundary to this system.
		// Brokers are app-specific, and Circuses are the main tools that an app's brokers use to access the data-data kinds .
		// 
		// The function of the CircusRecipe system is to evaluate pipelines of expressions and equations over these types.
		// Such a system can supply sufficient expressive richness for a large category of computing applications,
		// although a particular runtime system implementation will be usable and efficient for only some 
		// limited subcategories of functionality.
		// 
		// The CircusRecipe system is a convenient wrapper around the functionality in the RDF + SPARQL standards, 
		// with an emphasis on authorable pipelines of explicit structure maps, filters and equations, locally robust 
		// operation, and reasonable network scalability.
		
	}	
}		
trait ValueKey { 
}
trait ValueRowReader {
	def getValueAt[VT <: java.lang.Object](key : ValueKey) : VT
	def getKeysInOrder : Array[ValueKey]
	def isEmpty : Boolean
}
trait ValueRowWriter {
	def writeValueAt[VT <: java.lang.Object](key : ValueKey, v : VT)

}
trait ValueRowAbsorber {
	def absorbValues(inRow : ValueRowReader)
}
trait CircusNode {

	def clearNodeValues() : Unit

	def setKnownValues(cvRow : ValueRowReader)

	// This is the only way to produce the output values.
	// It produces one output row, to be fetched with getOutputValues.
	def solve() : Unit

	def getOutputValues : ValueRowReader
}

class ValueRowMutable extends ValueRowReader with ValueRowWriter with ValueRowAbsorber {
	def absorbValues(inRow: ValueRowReader): Unit = ???

	// Members declared in org.cogchar.blob.circus.CircusTest.ValueRowReader
	def getKeysInOrder: Array[ValueKey] = ???
	def getValueAt[VT <: Object](key: ValueKey): VT = ???
	def isEmpty: Boolean = ???

	// Members declared in org.cogchar.blob.circus.CircusTest.ValueRowWriter
	def writeValueAt[VT <: Object](key: ValueKey,v: VT): Unit = ???
}

abstract class CircusNodeSimpleBase extends CircusNode {
	private var myKnownValues : ValueRowMutable = new ValueRowMutable
	private var myOutputValues : ValueRowMutable = new ValueRowMutable
	override def clearNodeValues() : Unit = {
		if (!myKnownValues.isEmpty) {
			myKnownValues = new ValueRowMutable
		}
		if (!myOutputValues.isEmpty) {
			myOutputValues = new ValueRowMutable
		}
	}

	override  def setKnownValues(knownRow : ValueRowReader) {
		myKnownValues.absorbValues(knownRow)
	}


	// def solve() : Unit

	// 
	override def getOutputValues : ValueRowReader = myOutputValues

}
class CircusNodeForRecipe(myRecipe : CircusRecipe) extends CircusNodeSimpleBase {
	override def solve() : Unit = ???
}

