/*
 *  Copyright 2013 by The Cogchar Project (www.cogchar.org).
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

package org.cogchar.impl.channel
import org.appdapter.core.name.{Ident, FreeIdent};
import org.appdapter.core.item.{Item};
import org.appdapter.bind.rdf.jena.assembly.KnownComponentImpl;
import org.appdapter.help.repo.{RepoClient, RepoClientImpl, InitialBindingImpl} 
// import org.appdapter.impl.store.{FancyRepo};
// import org.appdapter.core.matdat.{SheetRepo}
import com.hp.hpl.jena.query.{QuerySolution} // Query, QueryFactory, QueryExecution, QueryExecutionFactory, , QuerySolutionMap, Syntax};

/**
 * @author Stu B. <www.texpedient.com>
 * 
 *	A File could be local, resource, or web based.
 */

class FileSpec(val myRelPath : String, val myOptParentSpec : Option[FolderSpec]) extends KnownComponentImpl {
	override def getFieldSummary() : String = {
		super.getFieldSummary + ", relPath=" + myRelPath + ", optParentSpec=" + myOptParentSpec;
	}
}
class FolderSpec(relPath : String, optParentSpec : Option[FolderSpec]) extends FileSpec (relPath, optParentSpec) {
}
class FancyFile(val mySpec : FileSpec, val myResolvedFullPath : String) {
	override def toString() : String = { 
		"FancyFile[resolvedPath=" + myResolvedFullPath + ", spec=" + mySpec + "]";
	}
}
class FancyFolder {
}
import org.cogchar.blob.emit.BehaviorConfigEmitter
object AnimFileSpecReader {
	val ANIM_IDENT = "anim"
	val ANIM_REL_PATH = "relPath"
	val ANIM_FOLDER_PATH = "folderPath"
	val animQueryQN = "ccrt:find_anims_99" // The QName of a query in the "Queries" model/tab
	val animGraphQN = "ccrt:anim_sheet_22" // The QName of a graph = model = tab, as given by directory model.   
	import scala.collection.JavaConversions._   
	def findAnimFileSpecs (behavCE : BehaviorConfigEmitter) : List[FancyFile] = {
		var daList : List[FancyFile] = Nil
		if ((behavCE.myDefaultRepoClient == null) || (behavCE.myAnimPathModelID == null)) {
			return Nil;
		}
		val repoClient = behavCE.myDefaultRepoClient
		val folderz = new scala.collection.mutable.HashMap[String,FolderSpec]()
		val solList = repoClient.queryIndirectForAllSolutions(animQueryQN, animGraphQN)

		solList.javaList foreach (animFile => {
				println("Got animFile soln: " + animFile);
				val animIdent = animFile.getIdentResultVar(ANIM_IDENT);
				val animRelPath = animFile.getStringResultVar(ANIM_REL_PATH);
				val animFolderPath = animFile.getStringResultVar(ANIM_FOLDER_PATH);
				println("ident=" + animIdent + ", relPath=" + animRelPath + ", folderPath=" + animFolderPath)
				
				val animFolderSpec = new FolderSpec(animFolderPath, None)
				val animFileSpec = new FileSpec(animRelPath, Some(animFolderSpec))
				animFileSpec.setIdent(animIdent)
				val animFullPath = animFolderPath + animRelPath
				val animFancyFile = new FancyFile(animFileSpec, animFullPath)
				daList = animFancyFile :: daList;
			})
		daList.reverse
	}

	def findAnimFileSpecsForJava (behavCE : BehaviorConfigEmitter) : java.util.List[FancyFile] = findAnimFileSpecs(behavCE)
}
