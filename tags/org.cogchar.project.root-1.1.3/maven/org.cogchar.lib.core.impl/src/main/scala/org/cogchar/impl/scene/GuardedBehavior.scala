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

package org.cogchar.impl.scene
import org.appdapter.core.name.{Ident, FreeIdent};
import org.appdapter.core.item.{Item, ItemFuncs};
import org.appdapter.bind.rdf.jena.assembly.ItemAssemblyReader;


import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;
import org.cogchar.name.behavior.{SceneFieldNames};


import org.cogchar.api.scene.Behavior

import org.cogchar.api.perform.{Performance};


import org.cogchar.api.thing.{ThingActionSpec}

import scala.collection.mutable.HashSet
/**
 * @author Stu B. <www.texpedient.com>
 *
 */

class GuardedBehavior (val myGBS: GuardedBehaviorSpec) extends BehaviorImpl(myGBS) {

  import scala.collection.mutable.HashSet

  val myPendingStepExecs  = new HashSet[GuardedStepExec]()

  def makeStepExecs() {
    for (gss <- myGBS.myStepSpecs) {
      val gse = gss.makeStepExecutor.asInstanceOf[GuardedStepExec]
      myPendingStepExecs.add(gse)
    }
  }
  override protected def doStart(scn : BScene) {
    super.doStart(scn)
    makeStepExecs();
  }
  override protected def doRunOnce(scn : BScene,  runSeqNum : Long) {
    // Every "pending" StepExec is given a chance to proceed, then removed from
    // "pending" on success. We copy the pending collection before iterating it
    // to remove any ambiguity.
    val pendingList = myPendingStepExecs.toList
    for (gse <- pendingList) {
      val didIt = gse.proceed(scn, this)
      if (didIt) {
        myPendingStepExecs.remove(gse)
      }
    }
    // TODO:  Check for "finalStep" processed without other steps necessary.
    if (myPendingStepExecs.size == 0) {
      getLogger().debug("Used up all my steps, so self-requesting module stop on : {}", myGBS.getIdent);
      markStopRequested();
      getLogger().info("Finished requesting stop, so this should be my last runOnce().");
    }
  }
  override def getFieldSummary() : String = {
    return  super.getFieldSummary() // +  ", nextStepIndex=" + myNextStepIndex;
  }
}

case class GuardedBehaviorSpec() extends BehaviorSpec {
  import scala.collection.JavaConversions._;

  var		myStepSpecs : List[BehaviorStepSpec] = List();

  // The field summary is used only for logging
  override def getFieldSummary() : String = {
    return  super.getFieldSummary() +  ", details=" + myDetails + ", steps=" + myStepSpecs;
  }

  override def makeBehavior() : Behavior[BScene] = {
    new GuardedBehavior(this);
  }
  override def completeInit(configItem : Item, reader : ItemAssemblyReader, assmblr : Assembler , mode: Mode) {
    /**
     public static String		P_initialStep		= NS_ccScn + "initialStep";
     public static String		P_step			= NS_ccScn + "step";
     public static String		P_finalStep		= NS_ccScn + "finalStep";
     public static String		P_waitForStart		= NS_ccScn + "waitForStart";
     public static String		P_waitForEnd		= NS_ccScn + "waitForEnd";
     */
    myDetails = "brimmingOver";

    val stepPropID = ItemFuncs.getNeighborIdent(configItem, SceneFieldNames.P_step)
    val stepItems : java.util.Set[Item] = configItem.getLinkedItemSet(stepPropID, Item.LinkDirection.FORWARD)

    getLogger().debug("GBS got stepItems: {}", stepItems);
    for (stepItem : Item  <- stepItems) {

      // Abstractly, a step has a set of guards and an action.
      // The guards are predicates that must be satisfied for the step to be taken.

      // Once the guard is passed, the step may "proceed", meaning the action is taken and then this step
      // is complete.   We generally define action as an asynchronous act that cannot "fail" - that would
      // be a system failure rather than a step failure.
      // Simple action types:
      //    a) A piece of text to be passed to a channel, for example:
      //			a1) Animation name or command
      //			a2) Output speech text

      getLogger().debug("Got stepItem: {}", stepItem)
      val stepIdent = stepItem.getIdent();

      // Haven't decided yet what to do with this "offsetSec" in the GuardedBehaviorStep case.
      val offsetSec = reader.readConfigValDouble(stepIdent, SceneFieldNames.P_startOffsetSec, stepItem, null);
      val offsetMillisec : Int = if (offsetSec == null) 0 else (1000.0 * offsetSec.doubleValue()).toInt;

      /***
       * We need to decide what kind of ActionSpec to construct, based on the available data in the step spec.
       * First demos were all done with TextActionSpec, but now we are making UseActionSpecs as well.
       * Eventually we should fetch an inferred type from the datamodel to fully specify the class of the ActionSpec.
       * Meanwhile, we use some ugly heuristics based on which fields are present in the step.
       *
       * Note that these BehaviorActionSpecs used within our Behavior Steps are, so far, mostly separate from the concept
       * of ThingActionSpec.   The name collision is unfortunate but we are living with it for the present.
       */

      // Collects the Ident for the firesThingAction column
      val thingActionToFireObjectList =
          reader.findOrMakeLinkedObjects(
            stepItem,
            SceneFieldNames.P_firesThingAction,
            assmblr,
            mode,
            null)
      val thingActionToFireList: List[ThingActionSpec] = thingActionToFireObjectList map (_.asInstanceOf[ThingActionSpec]) toList


      // Collects the Ident for the outputTAGraph column, expanded for safety
      val stepOutputTAGraph_RawSet =
        stepItem.getLinkedItemSet(
          new FreeIdent(SceneFieldNames.P_outputTAGraph),
          Item.LinkDirection.FORWARD);
      val stepOutputTAGraph =
        if (stepOutputTAGraph_RawSet != null && !stepOutputTAGraph_RawSet.isEmpty)
      { stepOutputTAGraph_RawSet.head.getIdent() }
        else null;

      val stepActionText = reader.readConfigValString(stepItem.getIdent(), SceneFieldNames.P_text, stepItem, null);
      val waitChanGuardProp = ItemFuncs.getNeighborIdent(configItem, SceneFieldNames.P_waitForChan);
      val chanFilterProp = ItemFuncs.getNeighborIdent(configItem, SceneFieldNames.P_chanFilter);
      val chanGuardItems = stepItem.getLinkedItemSet(waitChanGuardProp, Item.LinkDirection.FORWARD)
      val chanGuardCount = chanGuardItems.size
      val chanFilterItems = stepItem.getLinkedItemSet(chanFilterProp, Item.LinkDirection.FORWARD)
      val chanFilterCount = chanFilterItems.size
      getLogger().info("Step {} has {} chanGuards and {} chanFilters", Array(stepIdent, chanGuardCount, chanFilterCount).asInstanceOf[Array[AnyRef]])
      // Right now we are treating Text vs. Use actions as either-or, but there is some potential future overlap.
      val actionSpec : BehaviorActionSpec =
      if (stepActionText != null) new TextActionSpec(stepActionText)
      else if (chanGuardCount == 1) {
        val guardedChanID = chanGuardItems.head.getIdent
        val optFilterID = if (chanFilterCount == 1) Some(chanFilterItems.head.getIdent) else None
        new UseThingActionSpec(guardedChanID, optFilterID)
      } else if(thingActionToFireList != null && !thingActionToFireList.isEmpty && stepOutputTAGraph != null) {
        new FireThingActionSpec(thingActionToFireList, stepOutputTAGraph)
      } else {
        new TextActionSpec("This Action is Broken")
      }

      actionSpec.wireChannelSpecs(stepItem, reader, assmblr, mode)

      val guardSpecSet = new HashSet[org.cogchar.api.scene.GuardSpec]()

      val waitStartGuardProp = ItemFuncs.getNeighborIdent(configItem, SceneFieldNames.P_waitForStart)
      val waitEndGuardProp = ItemFuncs.getNeighborIdent(configItem, SceneFieldNames.P_waitForEnd)
      val taGuardProp = ItemFuncs.getNeighborIdent(configItem, SceneFieldNames.P_taGuard)

      val waitStartGuardItems = stepItem.getLinkedItemSet(waitStartGuardProp, Item.LinkDirection.FORWARD)
      val waitEndGuardItems = stepItem.getLinkedItemSet(waitEndGuardProp, Item.LinkDirection.FORWARD)
      val taGuardItems = stepItem.getLinkedItemSet(taGuardProp, Item.LinkDirection.FORWARD)

      for (wsg : Item <- waitStartGuardItems) {
        val guardSpec = new PerfMonGuardSpec(wsg.getIdent, Performance.State.PLAYING)
        guardSpecSet.add(guardSpec)
      }
      for (weg : Item <- waitEndGuardItems) {
        val guardSpec = new PerfMonGuardSpec(weg.getIdent, Performance.State.STOPPING)
        guardSpecSet.add(guardSpec)
      }
      for (tag : Item <- taGuardItems) {
        val chanID : Ident = tag.getSingleLinkedItem(waitChanGuardProp, Item.LinkDirection.FORWARD).getIdent
        val filterID_RawSet = tag.getLinkedItemSet(chanFilterProp, Item.LinkDirection.FORWARD);// expanded for safty
        val filterID : Ident = if( filterID_RawSet != null && !filterID_RawSet.isEmpty ) {
          filterID_RawSet.head.getIdent
        } else null

        //How to get a list of filters
//        val chanFilterProp = ItemFuncs.getNeighborIdent(configItem, SceneFieldNames.P_chanFilter);
//        val filterItems = stepItem.getLinkedItemSet(chanFilterProp)
//        for (filter : Item <- filterItems) {
//          filter.getIdent
//        }

        val guardSpec = new ThingActionGuardSpec(chanID, filterID)
        guardSpecSet.add(guardSpec)
      }

      val stepSpec = new GuardedStepSpec(stepIdent, actionSpec, guardSpecSet.toSet) // offsetMillisec, actionSpec);
      getLogger().debug("Built stepSpec: {}", stepSpec);
      myStepSpecs = myStepSpecs :+ stepSpec;
    }
  }
}
