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

package org.cogchar.impl.perform

import  org.cogchar.api.event.{Event}
import  org.cogchar.api.perform.{Media, PerfChannel, Performance, BasicTextChannel, BasicFramedChannel, BasicTextPerformance, BasicFramedPerformance, BasicPerformanceEvent};

import org.appdapter.api.module.Module.State;
import org.appdapter.core.log.{BasicDebugger};
import org.appdapter.core.name.{Ident, FreeIdent};
import org.appdapter.core.item.{Item};
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
 */

class FancyTime (val myStampMsec : Long) {
}
class FancyTextCursor(pos : Int) extends Media.ImmutableTextPosition(pos) {
}

class FancyTextMedia (val myTxt: String) extends  Media.Text[FancyTextCursor] {
	override def getFullText : String = myTxt
	override def getCursorBeforeStart = new FancyTextCursor(0)
	override def getCursorAfterEnd = new FancyTextCursor(myTxt.length)
	
}

class FancyTextInstruction(kind : Performance.Instruction.Kind, cursor : FancyTextCursor) 
		extends Performance.Instruction[FancyTextCursor] {
	myKind = kind
	myCursor = cursor;
}
trait FancyChanStuff {
}

class FancyTextPerfEvent(src: FancyTextPerf, worldTime: FancyTime, prevState : Performance.State,  nextState: Performance.State, 
					mediaCursor : FancyTextCursor) extends BasicPerformanceEvent[FancyTextCursor,FancyTextMedia, 
					FancyTime](src, worldTime, prevState, nextState, mediaCursor)

abstract class FancyTextChan(id: Ident)//  extends BasicTextChannel[FancyTextCursor, FancyTextMedia, FancyTime](id) {
		extends BasicTextChannel(id) {
			/*
	override def  makePerformanceForMedia(media : FancyTextMedia, initCursor: FancyTextCursor) 
			: Performance[FancyTextCursor, FancyTextMedia, FancyTime] =  {
		new FancyTextPerf(media, this, initCursor);
			
	}		
	*/
}
// abstract class FancyFramedChan[F](id: Ident) extends BasicFramedChannel[FancyTime,F](id) {}	


class FancyTextPerf(media : FancyTextMedia, chan: FancyTextChan, initCursor: FancyTextCursor) 
		extends  BasicTextPerformance[FancyTextCursor, FancyTextMedia, FancyTime] (media, chan, initCursor) {
		//, FancyTextPerf, Event[FancyTextPerf, FancyTime]]
		override protected def getCurrentWorldTime() = new FancyTime(System.currentTimeMillis);
		
		override protected def	makeStateChangeEvent(worldTime: FancyTime, prevState : Performance.State,  nextState: Performance.State, 
					mediaCursor : FancyTextCursor )	= new FancyTextPerfEvent(this, worldTime, prevState, nextState, mediaCursor)
				// new BasicPerformanceEvent[FancyTextCursor,FancyTextMedia, FancyTime](
					
}
/*
class FancyFramedPerf[F](media : Media.Framed[F], chan: Channel.Framed[FancyTime,F]) 
		extends  BasicFramedPerformance[FancyTime, F, FancyFramedPerf[F], Event[FancyFramedPerf[F], FancyTime]](media, chan) {
}
*/

class DummyTextChan(id: Ident) extends FancyTextChan(id) {
	@throws(classOf[Throwable])	
	override protected def attemptMediaPlayNow(m : Media[_]) {
	// override protected def attemptMediaStartNow[ FTC <: FancyTextCursor,  FTM <: Media.Text[FTC]](m : FTM) {
	//override protected def attemptMediaStartNow(m : FancyTextMedia) {
		m match {
			case ftm : FancyTextMedia => {
				val textString = ftm.getFullText();
				getLogger().info("************* START DUMMY TEXT PERFORMANCE on [" + getName() + "] of [" + textString + "]");
			}
			case _ => {
				getLogger().warn("Could not play media [{}] ", m)
			}
		}

	}
}


