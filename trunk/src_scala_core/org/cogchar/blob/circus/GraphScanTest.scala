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
		org.apache.log4j.BasicConfigurator.configure();
		org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.ALL);
		info0("Starting GraphScanTest")
		
		
	}
	// We return Set because there is no ordering assumed on the returned collection.
	// Regarding equality of members within this set, we note the following about the File.equals() method:
	// http://docs.oracle.com/javase/7/docs/api/java/io/File.html#equals(java.lang.Object)
	// "Tests this abstract pathname for equality with the given object. Returns true if and only if the argument is 
	// not null and is an abstract pathname that denotes the same file or directory as this abstract pathname. 
	// Whether or not two abstract pathnames are equal depends upon the underlying system. On UNIX systems, 
	// alphabetic case is significant in comparing pathnames; on Microsoft Windows systems it is not."

	// This could in some cases be a lot of pathnames, and thus the collection of names could be large.
	// User should ensure that either the folder or the filterFunc is sufficiently narrow to prevent over-match.
	def deepSearchReadableGraphTripleFiles(folder : File) : Set[File] = {
		val deh = new org.cogchar.blob.entry.DiskEntryHost()
		val suffixes = Set(".ttl", ".n3")
		deh.deepSearchReadablePlainFilesWithSuffixes(folder, suffixes)
	}
	// Return number of index records created.
	def makeGHostRecordsForDeepFolderOfTripleFiles(r2goModel : rdf2go.model.Model, deepFolder : File) : Int = {
		// TODO:  Make one or more GHost4Serial records identifying the folders, and link the GHost3 records to them.
		val graphFiles : Set[File] = deepSearchReadableGraphTripleFiles(deepFolder)
		val handles : Set[GraphHost3Serial] = graphFiles.map(sgf => {
			makeGHost3RecordForFileGraph(r2goModel, sgf)
		})
		handles.size
	}
	// We are using the mdir:hasUrlText property to record the file location in "file:" URL form.
	// (We say URL rather than URI, because in this case we expect the path to be directly dereferencable).
	
	// Normally we do not attach content-oriented metadata directly to the GHost3 record.  
	// However we sometimes do attach physical statistics such as file-size and last-modified time.
	// 
	def findOrMakeGHost3RecordForFileGraph(r2goModel : rdf2go.model.Model, singleGraphFile : File) : GraphHost3Serial  = {
		// TODO:  Find any matching GraphHost3Serial record in the graph, unless disabled by flag (for optimization).
		makeGHost3RecordForFileGraph(r2goModel, singleGraphFile)
	}
	// The returned record may correspond to a blank node, or may have a randomly generated URI.
	def makeGHost3RecordForFileGraph(r2goModel : rdf2go.model.Model, singleGraphFile : File) : GraphHost3Serial  = {
		// TODO:  Make sure this URI is "absolute"...?
		val fileURL : java.net.URI = singleGraphFile.toURI
		
		// TODO -- make the appropriate subtype for the given file extension.  Possibly also taste the files contents
		// to be more sure.
		// This constructor form creates a random record-instance URI.
		val gh3sHandle = new GraphHost3Serial(r2goModel, true)
		// Store the physical graph URL into the index model.
		gh3sHandle.setUrlText(fileURL.toString)
		gh3sHandle
	}
	
}
