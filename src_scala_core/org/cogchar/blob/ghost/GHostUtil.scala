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
/**
 * @author Stu B. <www.texpedient.com>
 */

package org.cogchar.blob.ghost

import com.hp.hpl.jena
import org.apache.jena.riot.RDFDataMgr
import jena.rdf.model.{ Model => JenaModel, ModelFactory => JenaModelFactory }
import org.ontoware.rdf2go

import rdf2go.model.{Model => R2GoModel}

import org.appdapter.fancy.log.VarargsLogging

import org.cogchar.blob.entry.{EntryHost, PlainEntry, FolderEntry}

import org.cogchar.api.owrap
import owrap.mdir.{GraphHost3Serial, GH4SFolder, GraphPointer => MdirGraphPointer}


/*				   
class GHostWrap {
}
*/

case class IndexResult (attemptedCount : Int, loadedCount : Int, pointedCount : Int)

object GHostUtil extends VarargsLogging {
	
	// firstArg to the pointerMaker is the model of the *contents* of the graph pointed to (not the place the pointer
	// goes).
	// We assume that any handle needed to store the GraphPointer rec is embedded in the pointerMaker.
	def indexGH3Serials(serial3GHosts : Traversable[GraphHost3Serial], 
						pointerMaker : Function2[R2GoModel, GraphHost3Serial, Option[MdirGraphPointer]]) : IndexResult = {
		val attemptedCount : Int = serial3GHosts.size
		var loadedCount : Int = 0
		var pointerCount : Int = 0
		serial3GHosts.foreach (cs3gh => {
			val ser3GH : GraphHost3Serial = cs3gh
			val loadedModel_opt : Option[JenaModel] = GHostUtil.readModelFromGHost3Serial(ser3GH)
			if (loadedModel_opt.isDefined) {
				loadedCount += 1
				val contentR2GoM : R2GoModel = new rdf2go.impl.jena.ModelImplJena(loadedModel_opt.get)
				contentR2GoM.open
				val pointer_opt : Option[MdirGraphPointer] = pointerMaker(contentR2GoM, ser3GH)
				if (pointer_opt.isDefined) {
					pointerCount += 1
				}
			} 
		})
		debug3("Successfully loaded {} out of {} triple files, and created {} graphPointer records", 
					loadedCount : Integer, attemptedCount : Integer, pointerCount : Integer)		
		new IndexResult(attemptedCount, loadedCount, pointerCount)
	}
	
	// Scan the supplied entryHost and assert up to maxEntries gh3Serial records in the  openTgtModel,
	//  *and* returns their handles in a collection.   In making the collection, we naively assume that there are 
	//  not already any gh3Serial records in the openTgtModel.
	def recordGH3SerialsFoundInGH4Folders(folders : Traversable[GH4SFolder], entryHost : EntryHost, 
						maxEntries : Int, openTgtModel : R2GoModel) : Traversable[GraphHost3Serial] = {
		folders.foreach(ghf => {
			val gh4sFolder : GH4SFolder = ghf
			val ghostPath : String = gh4sFolder.getUrlText
			// Scan the supplied entryHost and assert up to maxEntries gh3Serial records in the index model.  
			// Optionally it might also copy our GH4SFolder metadata into the gh3IdxModel; left undefined.
			val foundModelCount = GraphScanTest.scanDeepGraphFolderIntoGHostRecords(entryHost, ghostPath, maxEntries, openTgtModel)
			info3("Deep-scanned GHost folder {} in host {} and found {} results.", ghostPath, entryHost, foundModelCount : Integer)	  
		})
		debug1("Index model now contains: {}", openTgtModel.getUnderlyingModelImplementation)
		// Dangerous assumption:  There weren't any other ghost3Serials in the openTgtModel when it was passed to us!
		val serial3GHosts : Array[_ <: GraphHost3Serial] = GraphHost3Serial.getAllInstances_as(openTgtModel).asArray
		serial3GHosts
	}
	def readModelFromGHost3Serial(ser3GH : GraphHost3Serial) : Option[JenaModel] = {
		var resultModel_opt : Option[JenaModel] = None
		val ghostPath : String = ser3GH.getUrlText
		debug2("Content triple GHost3 {} has content triples URL {}", ser3GH, ghostPath)
		try { 
			// So far RDFDataMgr is not working with absolute jar: URLs.
			// However it should work if we pulled out a path that is relative to a locator it has available.
			// val loadedJenaModel = RDFDataMgr.loadModel(ghostPath)
			// This also fails for same reason, as it delegates to RDFDataMgr.		
			// loadedJenaModel.read(ghostPath) ;
			// However, if we open the stream ourselves and supply the syntax ourselves, the jar:file: URL works.

			val javaNetURL = new java.net.URL(ghostPath)
			// In the case of "jar:file: ... ! ...", this path has the "jar:" prefix stripped, but retains the "file:" and "!"
			val urlPath : String = javaNetURL.getPath  
			val openStreamDirectly = true  // currently the true case works, the false case doesn't (for jar-embedded files).
			val loadedJenaModel : JenaModel = if (openStreamDirectly) {

				val syntax = jenaSyntaxForPath(urlPath)
				debug2("Extracted URL path: {} and syntax: {}", urlPath, syntax)
				val inStream = javaNetURL.openStream
				val modelToLoad = JenaModelFactory.createDefaultModel();
				modelToLoad.read(inStream, null, syntax)
				modelToLoad
			} else {
				// Let Jena try with just the "path" part of the URL, i.e. *without* the "jar:" prefix.
				// Nope, this doesn't work.  Must go deeper .
				RDFDataMgr.loadModel(urlPath)
			}
			resultModel_opt = Option(loadedJenaModel)
			debug2("Loaded jena model from URL {}, size={}", ghostPath, loadedJenaModel.size : java.lang.Long)
		} catch {
			case th : Throwable => error3("Problem loading contents of gHost3 {} from URL {}, exc: {}", ser3GH, ghostPath, th)
		}
		resultModel_opt
	}
	def jenaSyntaxForPath(path : String) : String = { 
		//// Returns "TURTLE", "N3", etc.
		// See known syntax kinds here:
		// http://jena.apache.org/documentation/io/index.html
		// http://jena.apache.org/documentation/io/rdf-input.html
		import org.apache.jena.riot.RDFLanguages
		// this works if there actually is a .ext at end of path
		val extIdx = path.lastIndexOf('.');
		val extension : String = if (extIdx > 0) path.substring(extIdx+1) else ""
		val lang = org.apache.jena.riot.RDFLanguages.nameToLang(extension)
		lang.getLabel		
	}	
}

