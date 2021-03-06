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
package org.cogchar.blob.emit

import org.appdapter.trigger.bind.jena.TriggerImpl
import org.appdapter.api.trigger.{ BoxContext }
import org.appdapter.core.store.{ Repo }
// import org.appdapter.core.repo._
//import org.appdapter.core.repo.OmniLoaderRepoTest
//import org.appdapter.core.repo.RepoSpec
//import org.appdapter.core.matdat.SimplistRepoSpec
import org.appdapter.demo.DemoBrowserUI
import org.appdapter.trigger.bind.jena.{ FullBox, FullTrigger }
import org.appdapter.fancy.rspec.RepoSpec
//import org.appdapter.scafun.{ BoxOne, TriggerOne}
//import org.appdapter.gui.trigger.SysTriggers;
/**
 * @author Stu B. <www.texpedient.com>
 */

class RepoFabric {
  private var myEntries: List[Entry] = Nil;

  def addEntry(spec: RepoSpec): Unit = {
    myEntries = myEntries ::: List(new Entry(spec));
  }
  def getEntries(): List[Entry] = myEntries;

  class Entry(val mySpec: RepoSpec) {
    lazy val myRepo = mySpec.getOrMakeRepo();

    //lazy val myRepoClient = new EnhancedRepoClient(myRepo);

    // private lazy val	myRepoBox = new org.appdapter.gui.demo.DemoBrowser$DemoRepoBoxImpl()

    def getMutableBox() = {
      new ScreenBoxForImmutableRepo(myRepo)
      // myRepoBox.setRepo(myRepo.asInstanceOf[Repo.Mutable]);
      // myRepoBox;
    }
  }
}
class GraphBox(val myURI: String) extends org.appdapter.trigger.bind.jena.FullBox[GraphTrigger] {
  setShortLabel("tweak-" + myURI);
}
class GraphTrigger extends TriggerImpl[GraphBox] with org.appdapter.trigger.bind.jena.FullTrigger[GraphBox] {
  override def fire(box: GraphBox): Unit = {
    println(this.toString() + " firing on " + box.toString());
  }
}
class ScreenBoxForImmutableRepo(val myRepo: Repo) extends org.appdapter.trigger.bind.jena.FullBox[GraphTrigger] {
  import scala.collection.JavaConversions._;
  def resyncChildrenToTree(): Unit = {
    val ctx: BoxContext = getBoxContext();
    val graphStats: List[Repo.GraphStat] = myRepo.getGraphStats().toList;
    for (gs <- graphStats) {
      val graphBox = new GraphBox(gs.graphURI);
      val gt = new GraphTrigger();
      gt.setShortLabel("have-some-fun with " + gs)
      graphBox.attachTrigger(gt);
      ctx.contextualizeAndAttachChildBox(this, graphBox)
    }
  }
}
class FabricBox(val myFabric: RepoFabric) extends org.appdapter.trigger.bind.jena.FullBox[GraphTrigger] {
  /*		BT result = CachingComponentAssembler.makeEmptyComponent(boxClass);
	 result.setShortLabel(label);
	 result.setDescription("full description for box with label: " + label);
	 */
  setShortLabel("Repo-Fabric-Box");
  setDescription("Repo-Fabric-Box for " + myFabric.toString);

  def resyncChildrenToTree(): Unit = {
    val ctx: BoxContext = getBoxContext();
    val ocb = ctx.getOpenChildBoxes(this);
    for (e <- myFabric.getEntries) {
      val repoScreenBox = e.getMutableBox()
      repoScreenBox.setShortLabel(e.mySpec.toString())
      ctx.contextualizeAndAttachChildBox(this, repoScreenBox)
      repoScreenBox.resyncChildrenToTree()
    }
  }
}

object RepoFabricTest {
  def main(args: Array[String]) = {
    print("Start Whackamole");
    val repoNav = DemoBrowserUI.makeDemoNavigatorCtrl(args);
    print("Create a Goog Sheet Spec");
    // Tests need to provide their own constants
    /*val repoSpec = new GoogSheetRepoSpec(OmniLoaderRepoTest.BMC_SHEET_KEY, OmniLoaderRepoTest.BMC_NAMESPACE_SHEET_NUM, OmniLoaderRepoTest.BMC_DIRECTORY_SHEET_NUM);
    val repo = repoSpec.makeRepo;
    repo.getMainQueryDataset();
    //repo.loadDerivedModelsIntoMainDataset(null);
    repoNav.addObject(repo.toString(), repo, true);
    print("Make RepoFabric");
    val rf = new RepoFabric();
    print("Make FabricBox");
    val fb = new FabricBox(rf);
    fb.setShortLabel("Short Label")
    // Add this as an "entry" in the RepoFabric
    print("Add to Entry");
    rf.addEntry(new SimplistRepoSpec(repo))
    print("Resync");
    repoNav.addObject(null, fb, true);
    java.lang.Thread.sleep(60000000);*/
  }
}

