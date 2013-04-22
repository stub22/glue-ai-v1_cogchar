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

package org.cogchar.bind.lift;

import org.appdapter.core.name.FreeIdent;
import org.appdapter.core.name.Ident;
import org.cogchar.impl.perform.FancyTextCursor;
import org.cogchar.impl.perform.FancyTextMedia;
import org.cogchar.impl.perform.FancyTextPerf;
import org.cogchar.impl.perform.FancyTextPerfChan;
import org.cogchar.name.lifter.LiftAN;

/**
 *
 * A channel to handle triggers of web app output via FancyTextMedia. 
 * 
 * Right now this only can be used to select new web screens for all web sessions. 
 * We need to figure out some schema for expressing other commands in the form of single URIs.
 * 
 * @author Ryan Biggs <rbiggs@hansonrobokind.com>
 */


public class WebOutTriggerChan extends FancyTextPerfChan {
	
	public WebOutTriggerChan(Ident id) {
		super(id);
	}

	@Override public void fancyFastCueAndPlay (FancyTextMedia textMedia, FancyTextCursor cuePos, FancyTextPerf perf)  {	
		String commandUriString = textMedia.getFullText();
		if (commandUriString == null) { // is this check necessary? 
			getLogger().warn("WebOutTriggerChan received a null FancyTextMedia message");
		} else if (commandUriString.startsWith(LiftAN.NS_LifterConfig)) {
			LiftAmbassador.getLiftAmbassador().activateControlsFromUri(new FreeIdent(commandUriString));
		} else if (commandUriString.startsWith(LiftAN.NS_LifterInstance)) {
			LiftAmbassador.getLiftAmbassador().activateControlAction(new FreeIdent(commandUriString));
		} else {
			getLogger().warn("WebOutTriggerChan is currently a prototype can can only handle full liftconfigs by URI. "
					+ "I got this URI, which I don't know how to handle: {}", commandUriString);
		}
	}
	@Override public void updatePerfStatusQuickly(FancyTextPerf perf) {
		// TODO:  Keep track of output job associated with the performance, and report it
		// in this callback using perf.markState (and possibly even perf.markCursor, for finer grained info).  
	}
}
