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
import  org.cogchar.api.perform.{Media, Channel, Performance, BasicTextChannel, BasicFramedChannel, BasicTextPerformance, BasicFramedPerformance};

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
 */

class FancyTime (val myStampMsec : Long) {
}

trait FancyChanStuff {
}

abstract class FancyTextChan(id: Ident) extends BasicTextChannel[FancyTime](id) {}
abstract class FancyFramedChan[F](id: Ident) extends BasicFramedChannel[FancyTime,F](id) {}	
	

class FancyTextPerf(media : Media.Text, chan: Channel.Text[FancyTime]) 
		extends  BasicTextPerformance[FancyTime, FancyTextPerf, Event[FancyTextPerf, FancyTime]](media, chan) {
}
class FancyFramedPerf[F](media : Media.Framed[F], chan: Channel.Framed[FancyTime,F]) 
		extends  BasicFramedPerformance[FancyTime, F, FancyFramedPerf[F], Event[FancyFramedPerf[F], FancyTime]](media, chan) {
}

class DummyTextChan(id: Ident) extends FancyTextChan(id) {
	@throws(classOf[Throwable])	
	override protected def attemptMediaStartNow(m : Media.Text) {
		val textString = m.getFullText();
		logInfo("************* START DUMMY TEXT PERFORMANCE on [" + getName() + "] of [" + textString + "]");
	}
	override def  makePerformanceForMedia(media : Media.Text) : Performance[Media.Text, FancyTime] =  {
		return new FancyTextPerf(media, this);
	}	
}


