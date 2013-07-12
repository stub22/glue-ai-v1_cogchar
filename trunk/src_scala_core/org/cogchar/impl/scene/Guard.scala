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

package org.cogchar.impl.scene

/**
 * @author Stu B. <www.texpedient.com>
 */

import org.appdapter.core.log.{ BasicDebugger, Loggable }
import org.appdapter.core.name.{ Ident }
import org.cogchar.api.perform.{ Media, PerfChannel, Performance }
import org.cogchar.impl.perform.basic.{ BasicPerformance }
import org.cogchar.impl.perform.{ FancyTime, FancyPerformance }
import org.cogchar.impl.channel.{ ThingActionGraphChan }
import org.cogchar.api.thing._
import scala.collection.JavaConverters._
/**
 * @author Stu B. <www.texpedient.com>
 */

trait Guard {
  def isSatisfied(scn: BScene): Boolean
}
trait GuardSpec {
  def makeGuard: Guard
}

// Watches the performance of a previous step, and is satisfied when that Perf is marked STOPPING.
// Note that the guard itself is immutable (so far) and in principle could be used by many scenes.
// So far, the separation of the Guard and the spec is unnecessarily formal in this case, but that
// will probably help us soon.
class PerfMonitorGuard(mySpec: PerfMonGuardSpec) extends BasicDebugger with Guard {
  override def isSatisfied(scn: BScene): Boolean = {
    scn match {
      case fbs: FancyBScene => {
        val perfStatus = fbs.getPerfStatusForStep(mySpec.myUpstreamStepID)
        // FIXME:  We actually need to check for any state "equal or later" than the stateToMatch.
        if ((perfStatus == mySpec.myStateToMatchOrExceed) ||
          ((mySpec.myStateToMatchOrExceed == Performance.State.PLAYING)
            && (perfStatus == Performance.State.STOPPING))) {
          getLogger().debug("Treating perf-state {} as a match for guard {}", perfStatus, mySpec)
          true
        } else {
          getLogger().debug("Perf-state {} is not a match for guard {}", perfStatus, mySpec)
          false
        }
      }
      case _ => {
        throw new RuntimeException("Coding error:  PerfMonitorGuard asked to check on a non-fancy scene")
      }
    }
  }
  override def toString(): String = {
    "PerfMonitorGuard[spec=" + mySpec + "]";
  }
}
class PerfMonGuardSpec(val myUpstreamStepID: Ident, val myStateToMatchOrExceed: Performance.State) extends GuardSpec {
  override def makeGuard = new PerfMonitorGuard(this)
  override def toString(): String = {
    "PerfMonGuardSpec[upStepID=" + myUpstreamStepID + ", stateToMatchOrExceed=" + myStateToMatchOrExceed + "]"
  }
}
class ThingActionGuard(val mySpec: ThingActionGuardSpec) extends BasicDebugger with Guard {
  private var mySatisfiedFlag : Boolean = false;
  
  def makeTAFilter(myFilterID: Ident): ThingActionFilter = {
    //@todo use the assembler to make a TAF
    // ( ThingActionFilterBuilder )
    null
  }
  override def isSatisfied(scn: BScene): Boolean = {
    val filterToCheck: ThingActionFilter = makeTAFilter(mySpec.myFilterID);
    scn match {
      case fbs: FancyBScene => {
        val channelToCheck = fbs.getGraphChannel(mySpec.myListenChanID)
        val mustBeLaterThan = filterToCheck.getHasTStampGT();
        // FIXME:  We actually need to check for any state "equal or later" than the timestamp?
        
        channelToCheck match {                                 
          case tagc: ThingActionGraphChan => {
              val tasList : java.util.List[ThingActionSpec] = tagc.seeThingActions();
              for (tas <- tasList.asScala) {
                if (filterToCheck.test(tas)) {
                  getLogger().debug("ThingAction {} is a MATCH for TA-Filter {}", tas, filterToCheck)
                  return true
                } else {
                  getLogger().debug("ThingAction {} is NOT a match for TA-Filter {}", tas, filterToCheck)
                }
              }
          }
          case _ => getLogger().debug("GraphChannel {} is NOT a ThingActionGraphChannel, do not know how to get ThingAction", channelToCheck)             
        }                                                   
      }
      case _ => {
        throw new RuntimeException("Coding error:  PerfMonitorGuard asked to check on a non-fancy scene")
      }
    }
    false;
  }
}
class ThingActionGuardSpec(val myListenChanID: Ident, val myFilterID: Ident) extends GuardSpec {
  override def makeGuard = new ThingActionGuard(this)
  override def toString(): String = {
    "ThingActionGuardSpec[listenChanID=" + myListenChanID + ", filter=" + myFilterID + "]"
  }
}
