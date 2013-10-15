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

import org.cogchar.api.event.{ Event }
import org.cogchar.api.perform.{ Media, PerfChannel, Performance }
import org.cogchar.impl.perform.basic.{ BasicPerformance, BasicPerfChan, BasicPerformanceListener, BasicPerformanceEvent }
// , BasicTextChannel, BasicFramedChannel, BasicTextPerformance, BasicFramedPerformance, BasicPerformanceEvent};

import org.cogchar.api.perform.{ PerfChannel, Media, Performance, FancyPerformance };
//import org.cogchar.impl.perform.{ FancyTime, FancyTextMedia, FancyTextPerf, FancyTextCursor, FancyTextPerfChan, FancyTextInstruction};

import org.appdapter.api.module.Module.State;
import org.appdapter.core.log.{ BasicDebugger };
import org.appdapter.core.name.{ Ident, FreeIdent };
import org.appdapter.core.item.{ Item };
import org.appdapter.core.component.KnownComponentImpl;
import org.appdapter.bind.rdf.jena.assembly.DynamicCachingComponentAssembler;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;
import com.hp.hpl.jena.assembler.assemblers.AssemblerBase;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * @author Stu B. <www.texpedient.com>
 *
 * We call these classes "Fancy", but the point of them is mainly to make relatively simple types
 * for extension, with fewer type parameters than the Basic____ classes.
 *
 * The FancyText_____ classes target all features that are driven by text messages, including
 * speech output and simple interpreters like the scripted-animation system from robokind.
 * (The latter is actually driven by the animation resource/file *pathname* as the FancyTextMedia,
 * at present - see AnimOutTrigChan in the o.c.b.bind.robokind project.).
 */

abstract class FancyTextPerfChan[OutJob >: Null](id: Ident) extends BasicPerfChan(id) with FancyPerfChan_MovedToJava[OutJob] {
  override def getMaxAllowedPerformances: Int = 1

  // Override this method to do real "fancy" work.  We assume it can be done with no delay
  // (hence the name "fast")
  protected def fancyFastCueAndPlay(ftm: FancyTextMedia, cur: FancyTextCursor, perf: FancyTextPerf)

  // def updatePerfStatusQuickly(perf: FancyTextPerf)

  override def getMyLogger() = getLogger()

  /**
   * This override plugs us into the type superstructure.  However,  the Throwable and Protected do not propagate
   * to Java visibility- it is just a public method according to Java - compiler.  Thus the @throws is superfluous but
   * we keep it to remind us.
   */
  @throws(classOf[Throwable])
  override def fastCueAndPlay[Cur, M <: Media[Cur], Time](m: M, c: Cur, perf: BasicPerformance[Cur, M, Time]) {
    m match {
      case ftm: FancyTextMedia => {
        c match {
          case ftc: FancyTextCursor => {
            perf match {
              case ftp: FancyTextPerf => {
                fancyFastCueAndPlay(ftm, ftc, ftp);
              }
              case _ => {
                getLogger().warn("Cannot play un-fancy performance [{}] ", perf)
              }
            }
          }
          case _ => {
            getLogger().warn("Cannot play un-fancy cursor [{}] ", c)
          }
        }
      }
      case _ => {
        getLogger().warn("Cannot play un-fancy media [{}] ", m)
      }
    }
  }

  // This convenience method may or may not be supplied by other kinds of PerfChans (non FancyText).
  def makePerfAndPlayAtBeginNow(media: FancyTextMedia): FancyTextPerf = {
    val initCursor: FancyTextCursor = media.getCursorBeforeStart();
    val perf = new FancyTextPerf(media, this, initCursor)
    // TODO : make a time representing now
    val nowTime = new FancyTime(0);
    perf.scheduleInstructPlayAtBegin(nowTime)
    perf;
  }
}

class FancyTextCursor(pos: Int) extends Media.ImmutableTextPosition(pos) {
}

class FancyTextMedia(val myTxt: String) extends Media.Text[FancyTextCursor] {
  override def getFullText: String = myTxt
  override def getCursorBeforeStart = new FancyTextCursor(0)
  override def getCursorAfterEnd = new FancyTextCursor(myTxt.length)

}

class FancyTextInstruction(kind: Performance.Instruction.Kind, cursor: FancyTextCursor)
  extends Performance.Instruction[FancyTextCursor] {
  myKind = kind
  myCursor = cursor;
}

class FancyTextPerfEvent(src: FancyTextPerf, worldTime: FancyTime, prevState: Performance.State, nextState: Performance.State,
  mediaCursor: FancyTextCursor) extends BasicPerformanceEvent[FancyTextCursor, FancyTextMedia, FancyTime](src, worldTime, prevState, nextState, mediaCursor)

trait FancyTextPerfListener extends BasicPerformanceListener[FancyTextCursor, FancyTextMedia, FancyTime] {
  override def notify(bpe: BasicPerformanceEvent[FancyTextCursor, FancyTextMedia, FancyTime]) {
    bpe match {
      case ftpe: FancyTextPerfEvent => {
        notifyFTPE(ftpe)
      }
      case _ => {
        getLogger().warn("Notified of un-fancy event [{}] ", bpe)
      }
    }
  }
  def notifyFTPE(ftpe: FancyTextPerfEvent)
  def getLogger(): org.slf4j.Logger;
}

class FancyTextPerf(media: FancyTextMedia, chan: FancyTextPerfChan[_], initCursor: FancyTextCursor)
  extends BasicPerformance[FancyTextCursor, FancyTextMedia, FancyTime](media, chan, initCursor)
  with FancyPerformance {

  override def requestOutputJobCancel: Unit = {
    val chan = getFancyPerfChan
    chan.requestOutJobCancelForPerf(this)
  }

  override def getCurrentWorldTime() = new FancyTime(System.currentTimeMillis);

  override def makeStateChangeEvent(worldTime: FancyTime, prevState: Performance.State, nextState: Performance.State,
    mediaCursor: FancyTextCursor) = new FancyTextPerfEvent(this, worldTime, prevState, nextState, mediaCursor)

  // Implement the two easy-peasy typed methods from FancyPerformance, using our fancier-typed equivalents.
  override def getFancyPerfState: Performance.State = getState
  override def getFancyPerfChan = chan

  override def syncWithFancyPerfChanNow: Unit = updateFromChan

  override def markFancyState(s: Performance.State) { super.markState(s) }
  def markFancyTextCursor(ftc: FancyTextCursor, notify: Boolean) { super.markCursor(ftc, notify) }

  def updateFromChan {
    chan.updatePerfStatusQuickly(this)
  }

  /**
   * This  async listener API is  are for integration outside the Theater/Scene/Perf system.
   * They are not currently used within it.
   */
  def addUnfilteredListener(listener: FancyTextPerfListener) {
    addListener(classOf[FancyTextPerfEvent], listener);
  }
  def removeUnfilteredListener(listener: FancyTextPerfListener) {
    removeListener(classOf[FancyTextPerfEvent], listener);
  }
  def addFilteredListener(eventClazz: Class[_ <: FancyTextPerfEvent], listener: FancyTextPerfListener) {
    addListener(eventClazz, listener);
  }
  def removeFilteredListener(eventClazz: Class[_ <: FancyTextPerfEvent], listener: FancyTextPerfListener) {
    removeListener(eventClazz, listener);
  }

  def scheduleInstructPlayAtBegin(playStartTime: FancyTime) {
    val actionCursor = initCursor;
    val instruction = new FancyTextInstruction(Performance.Instruction.Kind.PLAY, initCursor)
    val startResFlag = attemptToScheduleInstruction(playStartTime, instruction);
  }

}

class DummyOutJob

class DummyTextChan(id: Ident) extends FancyTextPerfChan[DummyOutJob](id) {
  @throws(classOf[Throwable])
  override protected def fancyFastCueAndPlay(ftm: FancyTextMedia, cur: FancyTextCursor, perf: FancyTextPerf) {
    val textString = ftm.getFullText();
    getLogger().info("************* START DUMMY TEXT PERFORMANCE on dummy-chan [" + getName() + "]");
  }
  override def updatePerfStatusQuickly(perf: FancyPerformance) {
    getLogger().info("************* Updating status of perf on dummy-chan [" + getName() + "]")
  }
  override def requestOutJobCancel(doj: DummyOutJob): Unit = {
    getLogger().info("************* Cancelling output job for dummy-chan [" + getName() + "]")
  }
}
