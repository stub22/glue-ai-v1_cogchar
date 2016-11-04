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
package org.cogchar.convoid.speech.tts;

import org.cogchar.zzz.nwrap.core.NativeEngine;

/**
 *
 * @author Stu B.  <www.texpedient.com>
 */
public class SpeechRecognitionEngine extends NativeEngine {

	private long m_nativeHolder;

	public SpeechRecognitionEngine() {
		startup();
	}

	/* startupNative does this ONLY:
	nativeSpeechRecognizer = SpeechRecognitionEngineSingleton::instance();
	 */
	private native void startupNative();

	/*
	char* paramString = com::hansonrobotics::jni::JNU_GetStringNativeChars(env, config);
	std::auto_ptr<Parameters> p(Parameters::createFromString(paramString));
	
	nativeSpeechRecognizer->configure(*p);
	SpeechSearchFactory* fac = SpeechSearchFactorySingleton::instance();
	fac->configure(*nativeSpeechRecognizer, *p);
	
	// Now handled by the singleton
	nativeSpeechRecognizer->startup();
	 */
	private native void manualStartupNative(String config);

	private native void addObserverNative(ISpeechRecognition obs);

	private native void shutdownNative();

	private native void recognizeFileNative(String file);

	/*
	 * Results in a call to:
	 * sfsSearchCreateFromList(session, recog, pronun-model-data, words, 0, numWords);
	 * where words=phrases + ".nota", and numWords = phrases.length + 1
	 */
	private native void recognizeListNative(String[] phrases);

	/*
	 * uses SpeechSearchFactory.loadWords() to  load configured interrupts + anaphora files.
	 * Treat's the word "can't" specially during those loads, and may be "#" dependent.
	 * 
	 * Uses SpeechSearchFactory.AddListOfItems to create a "grammar" in which
	 * each phrase is a single symbol, spaces replaced with '_'s.  
	 * The words/phrases in the grammar are our phrases, the interrupts+anaphora, 
	 * 4 hardcoded noise words: "ah", "uh",  "o",  "eh", 
	 * ... and the 3 special words:  ".any", ".pau", ".nota"
	 * (which perhaps should not be words at all?).
	 * Pronunciations for the special words are computed with sfsPronunCompute
	 *  The grammar looks like this ("words" is a hashmap for keeping track of total word count):
	 * 
	 *   grammar << "list =";
	AddListOfItems(phrases, grammar, words);
	grammar << ";\n";  
	
	grammar << "int =";
	AddListOfItems(*interrupts, grammar, words);
	grammar << ";\n";
	
	grammar << "ana =";
	AddListOfItems(*anaphora, grammar, words);
	grammar << ";\n";  
	
	grammar << "\nnoise = ah | uh | o | eh;\nfill  = .pau%% | .any%%;\n";
	grammar << "gra   = $fill (.nota | $list [$noise] | $ana [$noise] | $int [$fill]);\n";
	 * 
	 */
	private native void recognizeGrammarNative(String[] phrases);
	
	private native void recognizeFullGrammarNative(String grammarName, String grammar, String[] tokenText, String[] tokenPronun);

	public synchronized void shutdown() {
		shutdownNative();
	}

	public synchronized void startup() {
		startupNative();
	}
	/*
	 *		Stu notes that:
	 *	
	
	
	// Now handled by the singleton
	//nativeSpeechRecognizer->startup();
	
	 */

	public synchronized void manualStartup(String config) {
		manualStartupNative(config);
	}

	public synchronized void addObserver(ISpeechRecognition observer) {
		addObserverNative(observer);
	}

	public synchronized void recognizeList(String[] phrases) {
		recognizeListNative(phrases);
	}

	public synchronized void recognizeGrammar(String[] phrases) {
		recognizeGrammarNative(phrases);
	}
	/* Some pronunciations may be null, in which case default pronunciations will be computed */
	public synchronized void recognizeFullGrammar(String grammarName, String grammar,  String[] tokenText, String[] tokenPronun) {
		recognizeFullGrammarNative(grammarName, grammar, tokenText, tokenPronun);
	}

	public synchronized void recognizeFile(String file) {
		recognizeFileNative(file);
	}
}
