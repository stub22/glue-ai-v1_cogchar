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
import org.appdapter.core.name.FreeIdent;
import org.appdapter.core.name.Ident;

import org.cogchar.api.perform.FancyPerformance;


import org.cogchar.name.lifter.LiftAN;

import org.cogchar.impl.web.config.LiftAmbassador;
/**
 * @author Stu B. <www.texpedient.com>
 */

class WebOutPerfChan(id : Ident)  extends  FancyTextPerfChan[WebOutPerfJob](id) {
	override def fancyFastCueAndPlay ( textMedia : FancyTextMedia, cuePos : FancyTextCursor, perf : FancyTextPerf ) 
				: Unit =  {	
					
		val commandUriString : String = textMedia.getFullText();
		getLogger().warn("Using old-form direct call from BThtr to LiftAmbassador for cmd: {}", commandUriString);
		if (commandUriString == null) { // is this check necessary? 
			getLogger().warn("WebOutTriggerChan received a null FancyTextMedia message");
		} else if (commandUriString.startsWith(LiftAN.NS_LifterConfig)) {
			LiftAmbassador.getLiftAmbassador().activateControlsFromUri(new FreeIdent(commandUriString));
		} else if (commandUriString.startsWith(LiftAN.NS_LifterInstance)) {
			LiftAmbassador.getLiftAmbassador().activateControlAction(new FreeIdent(commandUriString));
		} else {
			getLogger().warn("WebOutTriggerChan doesn't know how to handle the following URI: {}", commandUriString);
		}
	}
	override def updatePerfStatusQuickly( perf : FancyPerformance) : Unit = {
		// TODO:  Keep track of output job associated with the performance, and report it
		// in this callback using perf.markState (and possibly even perf.markCursor, for finer grained info).  
	}
	override def requestOutJobCancel(woj : WebOutPerfJob) : Unit =  {
		getLogger().info("************* Cancelling WebOut job on chan [" + getName() + "]");
	}	
}
class WebOutPerfJob {
	
}
