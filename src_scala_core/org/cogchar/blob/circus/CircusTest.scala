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
		
		
		val nodeForRecipe = new CircusNodeForRecipe(bootRecipe_r2go)
		
		// Each output recipe is now instantiated as an output node in our circus.
		// We connect each such node to input nodes corresponding to their input recipes, with exactly one
		// node for each recipe individual URI(/bNode).   We contine connecting input nodes until the input
		// set is closed, giving us a runnable node circus.  
		// 
		// To run the circus we:
		// 1) Clear node values
		// 2) Set externally known input node values
		// 3) Solve for all node values needed to determine the output node values.
		// 
		// In principle it is possible to internally set the same node's value more than once during processing step #3.
		// But generally from the outside we are only interested in the final (settled) values of the output nodes, 
		// and we expect the circus solver to manage whatever internal + intermediate results there are effectively,
		// and in accordance with whatever directives are specified in the particular Recipes.
		// 
		// The output (frame) from step 3 is a result row/map value, with keys corresponding to output recipe individuals 
		// (or hasOutputXyz specialization properties of the circusRecipe individual, when preferred), and values
		// typed by the corresponding recipe types.
		
		// As of 2015-02-25, we have 8 main categories of ccrp:Recipe available to upstream authors+systems to use.
		// These are essentially the value types of our implied expression language, instances of which may be bound
		// to nodes of the running circus.  Alphabetically they are:
		// 
		// Broker, Circus, GHost, Graph, Literal, Matrix, QueryGrSet, Uri
		// 
		// 6 of these 8 represent chunks of data, of two main superkinds:  Graph and Matrix.
		// GHosts represent all input/output RDF files, disk-folders, and local databases (of dim 3, 4, 5). 
		// The data they contain is referred to thru the forms of (subkinds of) Graph, QueryGrSet, Uri, Literal,
		// which may be thought of as our symbolic(+atomic primitive) working types of dimension 3, 4, 1, 1, respectively.
		// Matrix is our other data-data type, covering the kinds of data (of whatever dimension k) that fit better
		// into rectangular structures (of dim k) than into graphs.
		// Broker and Circus are objects defining the OO programmer's boundary to data pipelines of this system.
		// Brokers are app-specific, and Circuses (authored/configured/generated using Recipes) are the main tools 
		// that an app's brokers use to access the data-data kinds.
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
// Can we assume that an RNodeKey should always be a URI leading to a Recipe?
trait RNodeKey {
	def getAbsUriString : String
}
trait RecipeFinder {
	def findRecipe(key : RNodeKey) : Option[Recipe]
}
// class RNodeUri 
class R2goRecipeFinder(r2goModel : rdf2go.model.Model) extends RecipeFinder {
	override def findRecipe(key : RNodeKey) : Option[Recipe] = None
}

// Must clarify rules regarding value existence 
trait ValueRowReader {
	def getValueAt[VT <: java.lang.Object](key : RNodeKey) : VT // What if there is no value at that key?
	def getKeysInOrder : Array[RNodeKey] // Do we assume there must be a non-empty value at each such key?
	def isEmpty : Boolean // This means there are no *keys*?
}
trait ValueRowWriter {
	def writeValueAt[VT <: java.lang.Object](key : RNodeKey, v : VT)

}
trait ValueRowAbsorber {
	def absorbValues(inRow : ValueRowReader)
}
trait CircusNode {

	def clearNodeValues() : Unit

	// Client supplies us with input values corresponding to some known recipes in the circusRecipe input-closure.
	// Those recipes should correspond to value types compatible (or potentially coercible from) the actual values 
	// supplied in the cvRow, and should also be
	// compatible with the idea of a "constant", "known" or "supplied" value (as opposed to one that we expect to
	// be computed).
	def setKnownNodeValues(cvRow : ValueRowReader)

	// Variations of "solveXyz" are how we produce the output values.
	// It produces one output row, to be fetched with getOutputValues.
	// solveCompletely =>
	// Run as hard as possible in current thread until outputs are ready, or timeout is exceeded.
	// Timeout < 0 means run forever.  (Not advised)
	def solveCompletely(timeoutMsec : Long) : Unit 

	def getOutputNodeValues : ValueRowReader
}

class ValueRowMutable extends ValueRowReader with ValueRowWriter with ValueRowAbsorber {
	def absorbValues(inRow: ValueRowReader): Unit = ???

	// Members declared in org.cogchar.blob.circus.CircusTest.ValueRowReader
	def getKeysInOrder: Array[RNodeKey] = ???
	def getValueAt[VT <: Object](key: RNodeKey): VT = ???
	def isEmpty: Boolean = ???

	// Members declared in org.cogchar.blob.circus.CircusTest.ValueRowWriter
	def writeValueAt[VT <: Object](key: RNodeKey,v: VT): Unit = ???
	
	def toValueMapByRecipe(rcpFinder : RecipeFinder) : Map[Recipe, java.lang.Object] = {
		val tuplePairs = for (vkey <- getKeysInOrder) {
			val recipe_opt = rcpFinder.findRecipe(vkey)
			val pairOpt = recipe_opt.map(r => {
				val nodeVal : java.lang.Object =  getValueAt(vkey)
				r -> nodeVal
			})
			if (pairOpt.isDefined) {
	//			yield(pairOpt.get)
			}
		}
	//	tuplePairs.toMap
		Map()
	}
}

abstract class CircusNodeSimpleBase extends CircusNode {
	// These inputs + outputs may be thought of as keyed maps or orderered func-params (compatible in concept with SQL)
	protected var myKnownValues : ValueRowMutable = new ValueRowMutable
	protected var myOutputValues : ValueRowMutable = new ValueRowMutable
	override def clearNodeValues() : Unit = {
		if (!myKnownValues.isEmpty) {
			myKnownValues = new ValueRowMutable
		}
		if (!myOutputValues.isEmpty) {
			myOutputValues = new ValueRowMutable
		}
	}

	override  def setKnownNodeValues(knownRow : ValueRowReader) {
		myKnownValues.absorbValues(knownRow)
	}

	override def getOutputNodeValues : ValueRowReader = myOutputValues

	// A node may need access to audit system and logging system
}
// We presume that the recipe and all subcontents are backed by a permanently open, immutable, in-memory RDF2go model.
class CircusNodeForRecipe(private val myCircRecipe : CircusRecipe) extends CircusNodeSimpleBase {
	lazy val myOutRecipes : Array[Recipe] = myCircRecipe.getAllOutRecipe_as.asArray
	// This signature makes sense to keep track of the node-values that *have* been assigned.
	protected var mySolvedNodeMap : Map[Recipe, java.lang.Object] = Map()
	protected var myUnsolvedNodes : Set[Recipe] = Set()
	
	protected def wantForwardSolveOnFirstLoop : Boolean = true
	// These steps seek to advance the calculation somewhat, without taking up too much time.
	// We do these operations in an auditable way, and under time+resource control.
	// In many cases a calculation can be finished quickly, in a single backward step.
 
	
	lazy private val myRecipeFinder = new R2goRecipeFinder(myCircRecipe.getModel)
	protected def getRecipeFinder = myRecipeFinder
	
	// On the first time through the loop, would we rather run forward chaining first?
	override def clearNodeValues() : Unit = {
		super.clearNodeValues()
		mySolvedNodeMap = Map()
	}
	
	def findUnsolvedNodes () {
		// For all outRecipes, if there is not already a solved value, add the recipe.
		// Then (breadth-first?) search the unsolved set for input recipes, and if they are not solved, add them.		
		// Continue expanding set of unfinished recipes until it is complete.  
		// CONSIDER:  While collecting the above, we could also collect the number of downstream recipes waiting for 
		// it, and the numberof layers of those.  We could use this info to construct some ordering of the unsolved nodes.

	}
	
	 
	// Run as hard as possible in current thread until outputs are ready, or impossibility of solution is detected,
	// or timeout is exceeded.
	// Timeout < 0 means run forever.  (Not advised)
	override def solveCompletely(timeoutMsec : Long) : Unit = {
		// Solution is usually mostly pulled to the output values via backward chained queries+rules.
		// However, the "best" values may or may not depend on forward chained rules.
		// Even then, we may be able to find *some* solution using only backward chained rules.

		val startTime : Long = System.currentTimeMillis
		doStartSolveJob
		
		var flag_timedOut : Boolean = false
		var flag_isFirstTime = true
		val timeoutDur : Long = if (timeoutMsec >= 0) timeoutMsec else {
			// Print warning
			100000000000L 
		}
			
		while ((!areOutputsReady) && (!flag_timedOut)) {
			// TODO:  Add timeout params for the individual solve steps.
			// if ((!flag_isFirstTime) || (wantForwardSolveOnFirstLoop)) {
			//	doForwardSolveStep
			// }
			// doBackwardSolveStep
			doNodeSolutionPass()
			val nowTime = System.currentTimeMillis
			val elapsedTime = nowTime - startTime
			if (elapsedTime > timeoutDur) {
				flag_timedOut = true
			}
			flag_isFirstTime = false
		}
	}
	protected def doStartSolveJob() : Unit = {
		// Our goal is to find all output values, without going unstable.   
		// Propagate known values into val map
		val knownValMap : Map[Recipe, java.lang.Object] = myKnownValues.toValueMapByRecipe(getRecipeFinder) 
		mySolvedNodeMap ++= knownValMap
		
		findUnsolvedNodes
		// ensure that [unsolved]nodeValSet contains pairs for all outputRecipes, and all the input recipes reachable
		// from them.  Should that set be sorted by dependency-order?
	}
	
	def markSolvedNode(r : Recipe, vObj : java.lang.Object) : Unit = {
		mySolvedNodeMap += (r -> vObj)
		myUnsolvedNodes -= r
	}

	// If all necessary inputs have been supplied, then calling this solve repeatedly should eventually completely succeed.
	// However, if some upstream inputs are still missing (and not solvable), then some output-nodes 
	// will not be solved.
		// Recipes which are not connected to a path to an output will not be processed by this algorithm.
		// Recipes which have presently-unsolvable inputs will not be processed by this algorithm.
	
	def doNodeSolutionPass() {
		// Copy unsolved rNodes for iteration
		//	For each unsolved rNode
		//	   if all input Nodes are solved
		//			compute the node value and markSolvedNode 
	}
	
	protected def doBackwardSolveStep : Unit = {
		// For all unsolvedNodes
		// 
		// Find all nodes that do not yet have values
		// 
		// THEN
		// For each pass:
		//		For all the rNodes in the pending (not-yet-solved) set, look to see which ones have all of their input values bound.
		//			These are ready-to-exec nodes.
		//		For each ready-to-exec node, do the computation and store the value.
		// ...now some more nodes will be ready-to-exec.
		// 
			
	}
	protected def doForwardSolveStep : Unit = {
		// Hmmm.
		// This would seem to be of value only in some exceptional case 
	}

	protected def areOutputsReady : Boolean = ???

	
}
//  r2goModel : rdf2go.model.Model,
