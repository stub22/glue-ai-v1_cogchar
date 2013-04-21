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

class PipelineSpec(val myPipeID: Ident, val dirRepo: Repo.WithDirectory) extends RepoSpec {
  val mySourceIdSet = new java.util.HashSet[Ident]();
  override def toString(): String = {
    "PipelineSpec[pipeID=" + myPipeID + ", sourceIDS=" + mySourceIdSet + "]";
  }
  override def makeRepo(): PipelineRepo = {
    new PipelineRepo(myPipeID, mySourceIdSet, dirRepo);
  }
}

// @TODO to be moved to org.appdapter.lib.core
class PipelineRepo(val myPipeID: Ident, val mySourceIdSet: java.util.Set[Ident], val dirRepo: Repo.WithDirectory) extends DirectRepo(dirRepo.getDirectoryModel) {
  @Override def getDfltQrySrcGraphQName(): String = {
    myPipeID.getAbsUriString()
  }
  override def makeMainQueryDataset(): Dataset = {
    // this is lazy as we can get
    val mainDset: Dataset = DatasetFactory.create() // becomes   createMem() in later Jena versions.
    val mainDsource: DataSource = mainDset.asInstanceOf[DataSource];
    populateFromSourceSet(mainDsource, mySourceIdSet)
    mainDset;
  }

  def loadSheetModelsIntoMainDataset() = {
    // this calls our 
    getMainQueryDataset; // calls makeMainQueryDataset
  }

  import scala.collection.JavaConversions._;
  protected def populateFromSourceSet(mainDset: DataSource, mySourceIdSet: java.util.Set[Ident]) = {
    val myName = myPipeID.getAbsUriString;
    for (name <- mySourceIdSet.toList) {
      try {
        val srcModel: Model = dirRepo.getNamedModel(name);
        if (srcModel == null)
          throw new RuntimeException("PipelineRepo: no named repo called " + name + " findable by " + myName)
        var destModel: Model = mainDset.getNamedModel(myName);
        if (destModel == null) destModel = ModelFactory.createDefaultModel();
        val resultModel = destModel.union(srcModel);
        mainDset.replaceNamedModel(myName, resultModel);
        getLogger.debug("Loaded: " + name + " s=" + srcModel.size + " d=" + destModel.size + " r=" + resultModel.size + " into " + myName)
      } catch {
        case except => {
          println(" ex " + except);
          getLogger.error("PipelineRepo: error loading " + name + " into " + myName, except)
        }
      }

    }
  }

  def showDebugInfo() = {
    loadSheetModelsIntoMainDataset()
    //findAllSolutions(JenaArqQueryFuncs.parseQueryText(""))
  }

}
