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

import org.cogchar.impl.channel.FancyChannelSpec
import org.appdapter.core.name.{Ident, FreeIdent};
import org.appdapter.core.item.{Item};
import org.appdapter.core.component.KnownComponentImpl;
import org.appdapter.bind.rdf.jena.assembly.DynamicCachingComponentAssembler;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;
import com.hp.hpl.jena.assembler.assemblers.AssemblerBase;
import com.hp.hpl.jena.rdf.model.Resource;
import org.appdapter.bind.rdf.jena.assembly.ItemAssemblyReader;
import org.slf4j.Logger;

import  org.cogchar.api.perform.{Media, PerfChannel, Performance}
import  org.cogchar.impl.perform.basic.{BasicPerfChan, BasicPerformance, BasicPerformanceListener, BasicPerformanceEvent}
/**
 * @author Stu B. <www.texpedient.com>
 */
// We mark OutJob as nullable to facilitate Java interop.
trait FancyPerfChan[OutJob >: Null] {
	val		myJobsByPerf = new scala.collection.mutable.HashMap[FancyPerformance, OutJob]()
	
	def registerOutJobForPerf(perf : FancyPerformance, oj : OutJob) {
		myJobsByPerf.put(perf, oj)
	}
	def markPerfStoppedAndForget(perf : FancyPerformance) {
		myJobsByPerf.remove(perf)
		perf.markFancyState(Performance.State.STOPPING);
	}
	def getOutJobOrNull(perf : FancyPerformance) : OutJob = myJobsByPerf.getOrElse(perf, null)
	
	protected def requestOutJobCancel(oj : OutJob)
	
	def getMyLogger() : Logger
	
	def updatePerfStatusQuickly(perf: FancyPerformance)
	
	def requestOutJobCancelForPerf(perf : FancyPerformance) { 
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
trait FancyPerformance  { //  extends Performance[_, _, _ <: FancyTime] {
	def getFancyPerfState : Performance.State
	def syncWithFancyPerfChanNow : Unit
	def markFancyState(s : Performance.State) : Unit
	// We cannot narrow the type of this type param in the overrides.  We can only widen it!
	// So, this def winds up being no better than passing in Object as the argument.
	// def markFancyCursor[Cur](c : Cur, notify : Boolean) : Unit
	
	def getFancyPerfChan : FancyPerfChan[_]
	
	def requestOutputJobCancel : Unit = {
		val chan = getFancyPerfChan
		chan.requestOutJobCancelForPerf(this)
	}
}
class FancyPerfChanSpec  extends FancyChannelSpec {
	var		myDetails : String = "EMPTY";
	override def getFieldSummary() : String = {
		return super.getFieldSummary() + ", details=" + myDetails;
	}
	override def completeInit(configItem : Item, reader : ItemAssemblyReader, assmblr : Assembler , mode: Mode) {
		super.completeInit(configItem, reader, assmblr, mode)
		myDetails = reader.readConfigValString(configItem.getIdent(), PerfChannelNames.P_details, configItem, null);
	}

}