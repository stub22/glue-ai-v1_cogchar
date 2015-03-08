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
import org.cogchar.api.owrap.mdir._

import org.ontoware.rdf2go
import com.hp.hpl.jena

import java.io.File

import jena.rdf.model.{ Model => JenaModel, ModelFactory => JenaModelFactory }
import jena.ontology.Individual
import rdf2go.model.{Model => R2GoModel}

import org.cogchar.blob.entry.{EntryHost, PlainEntry, FolderEntry, DiskEntryHost, ResourceEntryHost}


/**
 * This code (along with rest of circus package) will probably be promoted to Appdapter eventually, but it is
 * easier for now to prototype with it in the Cogchar layer of Glue.AI.  It is not Cogchar feature-specific.
 * 
 * TODO:  We would like to make this scanning work for folders in an OSGi-bundle as well, but that will
 * require us to use Bundle.findEntries (returns iterator of URLs) or Bundle.getEntryPaths (iterator of Strings).
 * We may want to create a Folder interface that allows us to treat bundles and filesystems symmetrically.
 */

object GraphScanTest extends VarargsLogging {
	def main(args: Array[String]): Unit = {
		setupScanTestLogging
		info0("Starting GraphScanTest")
		scanCogcharOntoTestFiles
	}
	def scanCogcharOntoTestFiles() : Unit = {
		// Read files from these two cogchar onto folders as a test
		val ontoFolderPath = "org/cogchar/onto"
		val indivFolderPath = "org/cogchar/onto_indiv"
		
		// Those folders should both be findable within this ResourceEntryHost.
		// Hmmm.   That is working, sorta, but it is actually using the fact that copies of these files appear
		// in the local project via SVN mount.  It is not resolving them against the jar file of the lib.onto project.
		// So we see results like:
		// #hasUrlText "file:/E:/_mount/cogchar_trunk/maven/org.cogchar.lib.core.api/target/classes/org/cogchar/onto_indiv/bootSample_2015Q1_owl2.ttl"
		// ...which is not quite what we want!
		val markerClazz : java.lang.Class[_] = classOf[BSamp]
		val ontoResEntryHost = new ResourceEntryHost(markerClazz) 
		val maxEntries = 200
		
		// Make result models for the two scans.
		val ontoScanResultModel = makeEmptyTempR2GoModel
		val indivScanResultModel = makeEmptyTempR2GoModel
		
		// Scan the onto folder and print results
		val foundOntoCount = scanDeepGraphFolderIntoGHostRecords(ontoResEntryHost, ontoFolderPath, maxEntries, ontoScanResultModel)
		info3("Deep-scanned onto folder {} and found {} results: {}", ontoFolderPath, foundOntoCount : Integer, 
				  ontoScanResultModel.getUnderlyingModelImplementation)
		
		// Scan the indiv folder and print results
		val foundIndivCount = scanDeepGraphFolderIntoGHostRecords(ontoResEntryHost, indivFolderPath, maxEntries, indivScanResultModel)
		info3("Deep-scanned indiv folder {} and found {} results: {}", indivFolderPath, foundIndivCount : Integer, 
				  indivScanResultModel.getUnderlyingModelImplementation)		
		
	}

	// This could in some cases be a lot of pathnames, and thus the collection of names could be large.
	// User should ensure that either the folder or the filterFunc is sufficiently narrow to prevent over-match.
	val graphFileSuffixes = Set(".ttl", ".n3")// 

	def scanDeepGraphFolderIntoGHostRecords(entryHost : EntryHost, folderPath : String, maxEnts : Int, r2goModel : R2GoModel) : Int = {
		val folderEntry_opt : Option[FolderEntry] = entryHost.findFolderEntry(folderPath)
		if (folderEntry_opt.isDefined) {
			GraphScanTest.makeGHostRecordsForDeepFolderEntryOfTripleFiles(r2goModel, folderEntry_opt.get, maxEnts)
		} else -1
	}	
	def makeGHostRecordsForDeepFolderEntryOfTripleFiles(r2goModel : rdf2go.model.Model, deepFolderEntry : FolderEntry, maxEntries : Int) : Int = {
		// TODO:  Make one or more GHost4Serial records identifying the folders, and link the GHost3 records to them.
		
		val graphEntries : Set[PlainEntry] = deepFolderEntry.searchDeepPlainEntriesBySuffix(graphFileSuffixes, maxEntries)		
		val handles : Set[GraphHost3Serial] = graphEntries.map(sge => {
			makeGHost3RecordForGraphAtURL(r2goModel, sge.getJavaURI)
		})
		handles.size
	}	
	// We are using the mdir:hasUrlText property to record the file location in "file:" URL form.
	// (We say URL rather than URI, because in this case we expect the path to be directly dereferencable).
	
	// Normally we do not attach content-oriented metadata directly to the GHost3 record.  
	// However we sometimes do attach physical statistics such as file-size and last-modified time.
	// 
//	private def findOrMakeGHost3RecordForFileGraph(r2goModel : rdf2go.model.Model, singleGraphFile : File) : GraphHost3Serial  = {
		// TODO:  Find any matching GraphHost3Serial record in the graph, unless disabled by flag (for optimization).
//		makeGHost3RecordForGraphAtURL(r2goModel, singleGraphFile.toURI)
//	}
	// The returned record may correspond to a blank node, or may have a randomly generated URI (and here we do mean
	// "URI" in every sense, since it is for a semantic instance.  URN would also be appropriate].
	// 
	// The methodName says "URL" to emphasize physicality, and logically the singleGraphUrl value is a URL, but is
	// typed as java.net.URI for java-centric reasons.   See comments at bottom of EntryHost.scala.
	def makeGHost3RecordForGraphAtURL(r2goModel : rdf2go.model.Model, singleGraphUrl : java.net.URI) : GraphHost3Serial  = {
		// TODO:  Make sure this URL is "absolute"...?
	
		// val fileURL : java.net.URI = singleGraphFile.toURI
		
		// This constructor form creates a random record-instance URI.  It's a URN-flavored URI, usually un-fetchable.
		val gh3sHandle = new GraphHost3Serial(r2goModel, true)
		// TODO -- add an appropriate subtype tag for the given file extension.  Possibly also taste the files contents
		// to be more sure.
		
		// Store the physical graph URL into the index model.
		gh3sHandle.setUrlText(singleGraphUrl.toString)  // This is logically a URL, referring to a fetchable resource.
		gh3sHandle
	}
	
	def makeEmptyTempR2GoModel() : R2GoModel = {
		val jenaModelForGHostRecs : JenaModel = JenaModelFactory.createDefaultModel
		val r2goModel: R2GoModel = new rdf2go.impl.jena.ModelImplJena(jenaModelForGHostRecs)		
		r2goModel.open
		r2goModel
	}	
	def setupScanTestLogging() : Unit = { 
		org.apache.log4j.BasicConfigurator.configure();
		org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.ALL);		
		setLogLevelToInfoForClz(classOf[org.ontoware.rdf2go.impl.jena.TypeConversion])
		setLogLevelToInfoForClz(classOf[org.ontoware.rdfreactor.runtime.RDFReactorRuntime])
		setLogLevelToInfoForClz(classOf[org.ontoware.rdfreactor.runtime.ReactorRuntimeEntity])
		setLogLevelToInfoForClz(classOf[com.hp.hpl.jena.shared.LockMRSW])
		// These 2 are *packages*, so we can't use the classOf trick.
		setLogLevelToInfoForPkg("com.hp.hpl.jena.tdb.transaction")
		setLogLevelToInfoForPkg("org.apache.jena.info")
	}
	private def setLogLevelToInfoForClz(clz: Class[_]) {
		org.apache.log4j.Logger.getLogger(clz).setLevel(org.apache.log4j.Level.INFO)
	}
	private def setLogLevelToInfoForPkg(pkgName : String) {
		org.apache.log4j.Logger.getLogger(pkgName).setLevel(org.apache.log4j.Level.INFO)
	}
	
	// Methods below are obsolete - they use java.io.File directly instead of going through FolderEntry system.
	
	@Deprecated private def deepSearchReadableGraphTripleFiles(folder : File) : Set[File] = {
		val deh = new org.cogchar.blob.entry.DiskEntryHost(None)

		deh.deepSearchReadablePlainFilesWithSuffixes(folder, graphFileSuffixes)
	}

	// Return number of index records created.
	// This method is temporarily used by some other (unpublished) boot tests.  
	@Deprecated def makeGHostRecordsForDeepFolderOfTripleFiles(r2goModel : rdf2go.model.Model, deepFolder : File) : Int = {
		// TODO:  Make one or more GHost4Serial records identifying the folders, and link the GHost3 records to them.
		val graphFiles : Set[File] = deepSearchReadableGraphTripleFiles(deepFolder)
		val handles : Set[GraphHost3Serial] = graphFiles.map(sgf => {
			makeGHost3RecordForGraphAtURL(r2goModel, sgf.toURI)
		})
		handles.size
	}
	// This still uses the crude java.io.File based API.
	// We are replacing it with "EntryHost" based functionality.
	@Deprecated def naiveFolderScanAndIndex(rootFolderPath : String, relativeFolderPath : String, resultModel : R2GoModel) : Int = {
		
		// Here is a source-tree oriented path, not a bundle/class-rez path.
		// Temporarily needed for compatibility with our current "folder"-based scan below.
		// To be relaced with EntryHost approach.
		val scanFolderPath = relativeFolderPath;
		// We want to this once for each folder of graphs that is needed by some broker we want to init (or later feed)
		val deepFolder : File = new File(scanFolderPath)
		val numGraphs_orNumRecs : Int = makeGHostRecordsForDeepFolderOfTripleFiles(resultModel, deepFolder) 
		info3("Got {} recs from {} into r2goModel: {}", numGraphs_orNumRecs : Integer, scanFolderPath,  resultModel)
		numGraphs_orNumRecs	
	}
	
}
