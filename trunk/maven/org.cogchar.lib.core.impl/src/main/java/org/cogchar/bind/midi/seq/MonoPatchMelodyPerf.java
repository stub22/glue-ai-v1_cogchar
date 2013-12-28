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
import java.io.InputStream;
import java.net.URL;
import javax.sound.midi.ControllerEventListener;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import org.appdapter.core.log.BasicDebugger;
import org.cogchar.bind.midi.in.MidiMessageExtractor;
import org.cogchar.bind.midi.general.MidiNameConstants;

/**
 * When we play a MIDI sequence that acts only on one melody (i.e. not-drum)
 * patch (e.g. piano), then it is relatively simple to expose metadata about 
 * musical events on that single melody patch, mainly notes.  
 * Such sequences may be polyphonic; but whether or not they use
 * multiple MIDI channels (e.g. to play 2 "A#" notes at the same time)
 * is not yet accounted for in this class definition - it is treated as
 * a parallel stream "across all channels" (compatible with MIDI "promiscuous"
 * listening mode - but again, all presumed to be on a single patch of
 * melody timber). 
 * @author Stu B. <www.texpedient.com>
 */

public class MonoPatchMelodyPerf extends BasicDebugger {

	private		Sequence		mySequence;
	private		Sequencer		mySequencer;
	
	public MonoPatchMelodyPerf(URL url) throws Throwable  {
		mySequence = MidiSystem.getSequence(url);
	}
	public MonoPatchMelodyPerf(File file) throws Throwable  {
		mySequence = MidiSystem.getSequence(file);
	}
	public MonoPatchMelodyPerf(InputStream istream) throws Throwable {
		mySequence = MidiSystem.getSequence(istream);
	}
	private void connectToSeqr() throws Throwable {
		if (mySequencer != null) {
			getLogger().info("Already connected to  sequencer {}", mySequencer);
		} else {
			boolean doConnectSeqToSynth = true;
			mySequencer = MidiSystem.getSequencer(doConnectSeqToSynth);
			listenForSeqrEvents(mySequencer);
			getLogger().info("Connected to sys default sequencer {}, of class {}", mySequencer, 
						(mySequencer != null) ? mySequencer.getClass() : "NULL");
			
		}
	}
	public void startPlaying() {
		try {
			analyzeTracks();
			connectToSeqr();
			mySequencer.open();
			mySequencer.setSequence(mySequence);
			mySequencer.start();
		} catch (Throwable t) {
			getLogger().error("MIDI Out problem", t);
		}
	}
	public void close() {
		mySequencer.close();		
	}
	public void analyzeTracks() { 
		Track[] tracks = mySequence.getTracks();
		getLogger().info("My seq has {} tracks: {}", tracks.length, tracks);
		for (int tidx = 0; tidx < tracks.length; tidx++) {
			Track tr = tracks[tidx];
			long tickCount = tr.ticks();
			int eventCount = tr.size();
			getLogger().info("Track[{}] has {} events and {} ticks", tidx, eventCount, tickCount);
			for(int eidx = 0; eidx < 8; eidx++) {
				if (eidx >= eventCount) {
					break;
				}
				MidiEvent mev = tr.get(eidx);
				long evTick = mev.getTick();
				MidiMessage msg = mev.getMessage();
				getLogger().info("Event[{}] at tick={} has msg={}", eidx, evTick, msg);
			}
		}
	}
	private void listenForSeqrEvents(final Sequencer seqr) throws Throwable {
		// Code initially copied from jsresources.org 
		seqr.addMetaEventListener(new MetaEventListener() {
			public void meta(MetaMessage message) {

				getLogger().info("%%% MetaMessage: " + message);
				
				getLogger().info("%%% MetaMessage type: [{}]  length: {}", message.getType(), message.getLength());
				String decodedMsg = decodeMessage(message);
				getLogger().info("Decoded as:  " + decodedMsg);
				// Meta event 47 is "end of track"
				if (message.getType() == 47) {
					noticeSeqrTrackEnd(seqr);
				}
				
			}
		});
		int[] allControllersMask = new int[128];
		for (int i = 0; i < allControllersMask.length; i++) {
			allControllersMask[i] = i;
		}
		seqr.addControllerEventListener(
			new ControllerEventListener() {
			public void controlChange(ShortMessage message) {
				getLogger().info("%%% ShortMessage: {}", message);
				getLogger().info("%%% ShortMessage controller={}, value={} ", message.getData1(), message.getData2());
			}
		}, allControllersMask);
	}	
	private void noticeSeqrTrackEnd(final Sequencer seqr) {
		getLogger().info("MidiPlayer.<...>.meta(): end of track message received, closing sequencer and attached MidiDevices...");
		seqr.close();

	}
	public String decodeMessage(MetaMessage message)
	{
		byte[]	abMessage = message.getMessage();
		byte[]	abData = message.getData();
		int	nDataLength = message.getLength();
		String	strMessage = null;
		// System.out.println("data array length: " + abData.length);
		switch (message.getType())
		{
		case 0:
			int	nSequenceNumber = ((abData[0] & 0xFF) << 8) | (abData[1] & 0xFF);
			strMessage = "Sequence Number: " + nSequenceNumber;
			break;

		case 1:
			String	strText = new String(abData);
			strMessage = "Text Event: " + strText;
			break;

		case 2:
			String	strCopyrightText = new String(abData);
			strMessage = "Copyright Notice: " + strCopyrightText;
			break;

		case 3:
			String	strTrackName = new String(abData);
			strMessage = "Sequence/Track Name: " +  strTrackName;
			break;

		case 4:
			String	strInstrumentName = new String(abData);
			strMessage = "Instrument Name: " + strInstrumentName;
			break;

		case 5:
			String	strLyrics = new String(abData);
			strMessage = "Lyric: " + strLyrics;
			break;

		case 6:
			String	strMarkerText = new String(abData);
			strMessage = "Marker: " + strMarkerText;
			break;

		case 7:
			String	strCuePointText = new String(abData);
			strMessage = "Cue Point: " + strCuePointText;
			break;

		case 0x20:
			int	nChannelPrefix = abData[0] & 0xFF;
			strMessage = "MIDI Channel Prefix: " + nChannelPrefix;
			break;

		case 0x2F:
			strMessage = "End of Track";
			break;

		case 0x51:
			int	nTempo = ((abData[0] & 0xFF) << 16)
					| ((abData[1] & 0xFF) << 8)
					| (abData[2] & 0xFF);           // tempo in microseconds per beat
			float bpm = MidiMessageExtractor.convertTempo(nTempo);
			// truncate it to 2 digits after dot
			bpm = (float) (Math.round(bpm*100.0f)/100.0f);
			strMessage = "Set Tempo: "+bpm+" bpm";
			break;

		case 0x54:
			// System.out.println("data array length: " + abData.length);
			strMessage = "SMTPE Offset: "
				+ (abData[0] & 0xFF) + ":"
				+ (abData[1] & 0xFF) + ":"
				+ (abData[2] & 0xFF) + "."
				+ (abData[3] & 0xFF) + "."
				+ (abData[4] & 0xFF);
			break;

		case 0x58:
			strMessage = "Time Signature: "
				+ (abData[0] & 0xFF) + "/" + (1 << (abData[1] & 0xFF))
				+ ", MIDI clocks per metronome tick: " + (abData[2] & 0xFF)
				+ ", 1/32 per 24 MIDI clocks: " + (abData[3] & 0xFF);
			break;

		case 0x59:
			String	strGender = (abData[1] == 1) ? "minor" : "major";
			strMessage = "Key Signature: " + MidiNameConstants.sm_astrKeySignatures[abData[0] + 7] + " " + strGender;
			break;

		case 0x7F:
			// TODO: decode vendor code, dump data in rows
			String	strDataDump = MidiMessageExtractor.getHexString(abData);
			strMessage = "Sequencer-Specific Meta event: " + strDataDump;
			break;

		default:
			String	strUnknownDump = MidiMessageExtractor.getHexString(abData);
			strMessage = "unknown Meta event: " + strUnknownDump;
			break;

		}
		return strMessage;
	}	
}
