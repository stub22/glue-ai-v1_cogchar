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

package org.cogchar.impl.perform

import org.appdapter.bind.rdf.jena.assembly.ItemAssemblyReader
import org.appdapter.core.item.Item
import org.cogchar.api.perform.{ FancyPerfChan, FancyPerformance, Performance }
import org.cogchar.impl.channel.FancyChannelSpec

import com.hp.hpl.jena.assembler.{ Assembler, Mode }
/**
 * @author Stu B. <www.texpedient.com>
 */
// We mark OutJob as nullable to facilitate Java interop.
trait FancyPerfChan_MovedToJava[OutJob >: Null] extends FancyPerfChan[OutJob] {
  val myJobsByPerf = new scala.collection.mutable.HashMap[FancyPerformance, OutJob]()

  def registerOutJobForPerf(perf: FancyPerformance, oj: OutJob) {
    myJobsByPerf.put(perf, oj)
  }
  def markPerfStoppedAndForget(perf: FancyPerformance) {
    myJobsByPerf.remove(perf)
    perf.markFancyState(Performance.State.STOPPING);
  }
  def getOutJobOrNull(perf: FancyPerformance): OutJob = myJobsByPerf.getOrElse(perf, null)

  // this should be overiden!
  def requestOutJobCancel(oj: OutJob): Unit = {
    getMyLogger().warn("Subclass did not impliment cancel for Job {}", oj);
  }

  //def getMyLogger() : Logger

  //def updatePerfStatusQuickly(perf: FancyPerformance)

  def requestOutJobCancelForPerf(perf: FancyPerformance) {
    val oj = getOutJobOrNull(perf)
    if (oj != null) {
      requestOutJobCancel(oj)
    } else {
      getMyLogger().warn("Cannot find output job to cancel for perf {}", perf);
    }
  }
}

/**
 * This trait sets up the monitorModule, using features from our abstract types.
 */
trait FancyPerformance_NowInJava extends FancyPerformance { //  extends Performance[_, _, _ <: FancyTime] {
  def getFancyPerfState: Performance.State
  def syncWithFancyPerfChanNow: Unit
  def markFancyState(s: Performance.State): Unit
  // We cannot narrow the type of this type param in the overrides.  We can only widen it!
  // So, this def winds up being no better than passing in Object as the argument.
  // def markFancyCursor[Cur](c : Cur, notify : Boolean) : Unit

  def getFancyPerfChan: FancyPerfChan[_]

  def requestOutputJobCancel(): Unit = {
    val chan = getFancyPerfChan
    chan.requestOutJobCancelForPerf(this)
  }
}
class FancyPerfChanSpec extends FancyChannelSpec {
  var myDetails: String = "EMPTY";
  override def getFieldSummary(): String = {
    return super.getFieldSummary() + ", details=" + myDetails;
  }
  override def completeInit(configItem: Item, reader: ItemAssemblyReader, assmblr: Assembler, mode: Mode) {
    super.completeInit(configItem, reader, assmblr, mode)
    myDetails = reader.readConfigValString(configItem.getIdent(), PerfChannelNames.P_details, configItem, null);
  }

}