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

package org.cogchar.bind.midi.in;

import javax.sound.midi.Receiver;
import org.appdapter.core.log.BasicDebugger;
import org.cogchar.bind.midi.general.FunMidiEventRouter;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class CCParamRouter {
	private		ControlChangeParamBinding[]	myBindingsByNumber = new ControlChangeParamBinding[128];
	
	public CCParamRouter(FunMidiEventRouter fmer) {
		OurListener ol = new OurListener();
		fmer.registerListener(ol);	
	}
	// Something not unlike a Novation Nocturn, in x Channels
	public void connectAndMapContSurf (String devName, int chanCount) {
		
	}
	public class OurListener extends BasicDebugger implements MidiEventReporter.Listener {

		@Override public void reportEvent(InterestingMidiEvent ime) {
			getLogger().debug("*** Oulist received midi event: {} ", ime);
			Receiver recvr = ime.myReceiver;
			
			if (recvr instanceof  MidiReceiverOurs) {
				MidiReceiverOurs mro = (MidiReceiverOurs) recvr;
				if (ime instanceof InterestingMidiEvent.ControlChange) {
					InterestingMidiEvent.ControlChange ccEvent = (InterestingMidiEvent.ControlChange) ime;
					int ccNum = ccEvent.myController;
					float normalVal = ccEvent.myValue / 127.0001f;
					ControlChangeParamBinding ccpb = myBindingsByNumber[ccNum];
					getLogger().debug("Got normalVal={} for ccNum={} with binding={}", normalVal, ccNum, ccpb);
					if (ccpb != null) {
						ccpb.noticeNormalizedValue(normalVal);
					}
					
				}
				if (mro.myKind == MidiReceiverOurs.KnownKind.NovationAutomap) {
					
				}
			}
		}
	}
	static class ControlChangeParamBinding  {
		String				myParamName;
		ParamValueListener	myListener;
		public void noticeNormalizedValue(float val) {
			myListener.setNormalizedNumericParam(myParamName, val);
		}
		public String toString() {
			return "[paramName=" + myParamName + ", listener=" + myListener + "]";
		}
	}
	

	
	/**
	 * Overwrites any existing listener binding for this param.
	 * TODO:  Also map by midi-channel.
	 * @param ccNum
	 * @param paramName
	 * @param listener 
	 */
	public void putControlChangeParamBinding(int ccNum, String paramName, ParamValueListener listener) {
		ControlChangeParamBinding ccpb = new ControlChangeParamBinding();
		ccpb.myParamName = paramName;
		ccpb.myListener = listener;
		myBindingsByNumber[ccNum] = ccpb;
	}
	
}
