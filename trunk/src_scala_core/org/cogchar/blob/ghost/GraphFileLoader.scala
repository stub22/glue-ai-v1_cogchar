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

package org.cogchar.blob.ghost

import org.appdapter.core.name.{Ident, FreeIdent};
import org.appdapter.fancy.log.VarargsLogging
import org.appdapter.fancy.gportal.{ GraphPortal, DelegatingPortal, LazyLocalDelegatingPortal, LocalGraphAbsorber, GraphSupplier, GraphQuerier, GraphAbsorber  }
import com.hp.hpl.jena
import jena.tdb.TDBFactory
import java.io.File
import jena.query.{Dataset, ReadWrite}
import org.apache.jena.riot.RDFDataMgr


class GraphFileLoader extends VarargsLogging {
	
	val OWL2_TURTLE_SUFFIX : String = "_owl2.ttl"

	// Used in TestFolioAdmin and TestMediaPtrVols.
	def loadOneGraphInMemDelgPortFromFolder(graphID : Ident, folderPath : String) : DelegatingPortal = {
		makeAndLoadOneGraphMemDelgPort(graphID, (loader, inMemDSet, absorber, grID) => {
			loader.mergeFolderFilesIntoGraph(inMemDSet, absorber, grID, folderPath)
		})
	}
	protected def mergeFolderFilesIntoGraph(inMemDSet : Dataset, absorber : GraphAbsorber,  graphID : Ident, folderPath : String) : Unit = {
		val dir : File = new File(folderPath)
		if(dir.exists && dir.isDirectory && dir.canRead){
			dir.listFiles.filter(p => { p.isFile && p.canRead && p.getName.endsWith(OWL2_TURTLE_SUFFIX)})
				.foreach(p => {
					val filePathUrlTxt = p.toURI.toString
					info2("Merging model from file: {} into graph: {}", filePathUrlTxt, graphID)
					inMemDSet.begin(ReadWrite.WRITE);
					mergeFileModelToGraph(absorber, graphID, filePathUrlTxt)
					inMemDSet.commit
				})
		}
		
	}
	protected def mergeFileModelToGraph(absorber : GraphAbsorber,  graphID : Ident,  inFileresPath : String) : Unit = {
		// Such import is more properly and generally done through a bracketed-oper.
		// (which will work for both local + remote absorbers, without needing the dataset passed around).
		val modelToMergeIn = RDFDataMgr.loadModel(inFileresPath)
		debug3("Merging into graph {} a model from {} of size {}", graphID, inFileresPath, modelToMergeIn.size : java.lang.Long) 
		absorber.addStatementsToNamedModel(graphID, modelToMergeIn) 
	}
	private def makeAndLoadOneGraphMemDelgPort(graphID: Ident, loadFunc : Function4[GraphFileLoader, Dataset, GraphAbsorber, Ident, Unit]) : DelegatingPortal = {
		makeAndLoadMemDelgPort((loader, inMemDSet, absorber) => {
			loadFunc(loader, inMemDSet, absorber, graphID)
		})
	}
		
	private def makeAndLoadMemDelgPort(loadFunc : Function3[GraphFileLoader, Dataset, GraphAbsorber, Unit]) : DelegatingPortal = {
		val inMemDSet = TDBFactory.createDataset()
		val dgp = new LazyLocalDelegatingPortal(inMemDSet)
		val absrbr = dgp.getAbsorber.asInstanceOf[LocalGraphAbsorber]
		loadFunc(this, inMemDSet, absrbr)
		dgp
	}
}
