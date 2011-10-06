/*
 *  Copyright 2011 by The Cogchar Project (www.cogchar.org).
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

package org.cogchar.speech;

import java.util.logging.Logger;
import org.cogchar.nwrap.core.NativeEngine;

/**
 *
 * @author josh
 */
public class TTSEngine extends NativeEngine implements ISpeakText
{
	private static Logger	theLogger = Logger.getLogger(TTSEngine.class.getName());

    private long m_ptr;

    public TTSEngine() {
        startup();
    }

    private native void createEngine();
    private native void shutdownEngine();
    
    private native void addObserverNative(ITTSEngineObserver obs);
    private native long speakNative(java.lang.String words);
    private native long cancelSpeechNative();	
    
    public synchronized void shutdown() {
        shutdownEngine();
    }

    public synchronized void startup() {
        createEngine();
    }
    
    public synchronized long speak(String utterance) {
		// TODO : set up a chain of pluggable filters/scrubbers above this level.
		// RoboNativeWrap is not the right place to do this filtering, but
		// Netbeans is suddenly having trouble with FindUsages and Refactoring.
		// This may be related to our new _btools project setup, and/or this bug:
		// http://netbeans.org/bugzilla/show_bug.cgi?id=186314

		String scrubbedUtterance = SpeechOutputScrubber.scrubText(utterance);
        return speakNative(scrubbedUtterance);
    }
    public synchronized long cancelSpeech() {
        return cancelSpeechNative();
    }
    public synchronized void addObserver(ITTSEngineObserver observer) {
        addObserverNative(observer);
    }
    public void removeObserver(ITTSEngineObserver observer) {
		throw new RuntimeException("RemoveObserver not implemented in TTSEngine");
	}

}
