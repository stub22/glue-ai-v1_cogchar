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

package org.cogchar.blob.emit

import com.hp.hpl.jena.query.DataSource
import com.hp.hpl.jena.query.Dataset
import com.hp.hpl.jena.query.DatasetFactory
import com.hp.hpl.jena.rdf.model.Model
import com.hp.hpl.jena.rdf.model.ModelFactory
import java.lang.Override
import org.appdapter.core.name.Ident
import org.appdapter.core.store.Repo
import org.appdapter.impl.store.DirectRepo
import scala.collection.JavaConversions.asScalaSet

/**
 * @author LogicMOO <www.logicmoo.org>
 */

// FIXME:  The srcRepo should really not be given to the RepoSpec, because it
// is not serializable specData.
class PipelineRepoSpec(val myDGSpecs : Set[DerivedGraphSpec], val mySrcRepo : Repo.WithDirectory) extends RepoSpec {
	override def toString(): String = {
		"PipelineRepoSpec[pipeSpecs= " + myDGSpecs + "]";
	}
	override def makeRepo(): PipelineRepo = {
		val emptyDirModel = ModelFactory.createDefaultModel();			
		val derivedRepo = new PipelineRepo(emptyDirModel, this) 
		for (dgSpec <- myDGSpecs) {
			val derivedModel = dgSpec.makeDerivedModel(mySrcRepo)
			derivedRepo.replaceNamedModel(dgSpec.myTargetID, derivedModel)
		}
		derivedRepo
	}
}

// @TODO to be moved to org.appdapter.lib.core
class PipelineRepo(emptyDirModel : Model, val myRepoSpec : PipelineRepoSpec) extends DirectRepo(emptyDirModel) {

	// TODO:  Move this method up to Appdapter.DirectRepo
	def replaceNamedModel(modelID : Ident, jenaModel : Model) {
		val	repoDset : Dataset = getMainQueryDataset
		val repoDsource : DataSource = repoDset.asInstanceOf[DataSource];
		repoDsource.replaceNamedModel(modelID.getAbsUriString, jenaModel);
	}

}
