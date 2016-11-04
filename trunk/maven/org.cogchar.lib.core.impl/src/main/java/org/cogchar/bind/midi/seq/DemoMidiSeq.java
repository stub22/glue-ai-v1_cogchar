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
package org.cogchar.bind.midi.seq;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.cogchar.bind.midi.general.MidiTransmitDevWrap;
import org.cogchar.platform.util.ClassLoaderUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class DemoMidiSeq {

	static Logger theLogger = LoggerFactory.getLogger(DemoMidiSeq.class);

	public enum DemoMonoMelody {

		GOOD_KING_WENCESLAS,
		GREENSLEEVES_MELODY,
		GREEN_THREECOUNT,
		AULD_LANG_SYNE,
		BEETHOVEN_MOONLIGHT,
		DOUJAN
	}
	public static MonoPatchMelodyPerf loadDemoMonoMelody(DemoMonoMelody melody) {
		List<ClassLoader> cLoaders = new ArrayList<ClassLoader>();
		ClassLoader localCL = org.appdapter.core.boot.ClassLoaderUtils.getClassLoader(DemoMidiSeq.class);
		theLogger.info("Found local classLoader: {}", localCL);
		cLoaders.add(localCL);
		return loadDemoMonoMelody(melody, cLoaders);
	}

	public static MonoPatchMelodyPerf loadDemoMonoMelody(DemoMonoMelody melody, List<ClassLoader> classLoaders) {
		MonoPatchMelodyPerf mpmp = null;
		String resPath = "NOT_FOUND";
		String pathHead = "midiseq/mutopia/";
		try {
			switch (melody) {
				case GOOD_KING_WENCESLAS:
					resPath = pathHead +  "GoodKingWenceslas.mid";
				break;
				case BEETHOVEN_MOONLIGHT:
					resPath = pathHead + "beethoven_moonlight_sonata_part1.mid";
				break;
				case GREENSLEEVES_MELODY:
					resPath = pathHead + "Greensleeves_melody.mid";					
				break;	
				case GREEN_THREECOUNT:
					resPath = pathHead + "Greensleeves_threecount.mid";					
				break;	
				case AULD_LANG_SYNE:
					resPath = pathHead + "auld_lang_syne.mid";					
				break;	
				case DOUJAN:
					resPath = pathHead + "Doujan.mid";					
				break;	
			}
			theLogger.info("Seq Path is {}", resPath);
			URL seqURL = ClassLoaderUtils.findResourceURL(resPath, classLoaders);
			theLogger.info("Seq URL is {}", seqURL);
			if (seqURL != null) {
				mpmp = new MonoPatchMelodyPerf(seqURL);
			}
		} catch (Throwable t) {
			theLogger.error("Problem loading melody seq from path {}", resPath, t);
		}
		return mpmp;
	}
	public static void shortTestPlay(DemoMonoMelody melody, int lengthMsec) { 
		try {
			MonoPatchMelodyPerf mpmp = loadDemoMonoMelody(melody);
			if (mpmp != null) {
				mpmp.startPlaying();
				Thread.sleep(lengthMsec);
				mpmp.stopPlaying();
				mpmp.close();
			} else {
				theLogger.warn("Cannot find demo sequence: {}", melody);
			}	
		} catch (Throwable t) {
			theLogger.error("Problem testing demo sequence: {}", melody);
		}		
	}
	/**
	 * Works only from inside the project, with local source file.
	 */
	public static void unitTestMPMP() {
		String path = "src/main/resources/midiseq/mutopia/GoodKingWenceslas.mid";
		try {

			File relFileDevOnly = new File(path);
			MonoPatchMelodyPerf mpmp = new MonoPatchMelodyPerf(relFileDevOnly);
			mpmp.startPlaying();
		} catch (Throwable t) {
			theLogger.error("Problem playing melody seq from path {}", path, t);
		}
	}
}
