package org.cogchar.bind.midi.in;

/*
 *	MidiReceiverDumpsAndNotifies.java
 *
 *	This file is part of jsresources.org
 */

/*
 * Copyright (c) 1999 - 2001 by Matthias Pfisterer
 * Copyright (c) 2003 by Florian Bomers
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import	java.io.PrintStream;

import	javax.sound.midi.MidiSystem;
import	javax.sound.midi.InvalidMidiDataException;
import	javax.sound.midi.Sequence;
import	javax.sound.midi.Track;
import	javax.sound.midi.MidiEvent;
import	javax.sound.midi.MidiMessage;
import	javax.sound.midi.ShortMessage;
import	javax.sound.midi.MetaMessage;
import	javax.sound.midi.SysexMessage;
import	javax.sound.midi.Receiver;
import org.cogchar.bind.midi.general.MidiNameConstants;



/**	Displays the file format information of a MIDI file.
 * 
 * 
 * Modified in place by StuB22 to call our "notice" methods from the strapped-on superclass.
 * 
 */
public class MidiReceiverDumpsAndNotifies extends MidiReceiverOurs 
	implements	Receiver
{
	


	public static long seByteCount = 0;
	public static long seCount = 0;
	public static long smCount = 0;



	private PrintStream		m_printStream;
	private boolean			m_bDebug;
	private boolean			m_bPrintTimeStampAsTicks;



	public MidiReceiverDumpsAndNotifies(PrintStream printStream)
	{
		this(printStream, false);
	}


	public MidiReceiverDumpsAndNotifies(PrintStream printStream,
			    boolean bPrintTimeStampAsTicks)
	{
		m_printStream = printStream;
		m_bDebug = false;
		m_bPrintTimeStampAsTicks = bPrintTimeStampAsTicks;
	}



	public void close()
	{
	}



	public void send(MidiMessage message, long lTimeStamp)
	{
		String	strMessage = null;
		if (message instanceof ShortMessage)
		{
			strMessage = decodeMessage((ShortMessage) message);
		}
		else if (message instanceof SysexMessage)
		{
			strMessage = decodeMessage((SysexMessage) message);
		}
		else if (message instanceof MetaMessage)
		{
			strMessage = decodeMessage((MetaMessage) message);
		}
		else
		{
			strMessage = "unknown message type";
		}
		String	strTimeStamp = null;
		if (m_bPrintTimeStampAsTicks)
		{
			strTimeStamp = "tick " + lTimeStamp + ": ";
		}
		else
		{
			if (lTimeStamp == -1L)
			{
				strTimeStamp = "timestamp [unknown]: ";
			}
			else
			{
				strTimeStamp = "timestamp " + lTimeStamp + " us: ";
			}
		}
		if (m_printStream != null) {
			m_printStream.println(strTimeStamp + strMessage);
		}
	}



	public String decodeMessage(ShortMessage message)
	{
		String	strMessage = null;
		switch (message.getCommand())
		{
		case 0x80:
			strMessage = "note Off " + MidiMessageExtractor.getKeyName(message.getData1()) + " velocity: " + message.getData2();
			noticeNoteOff(this, message.getChannel() + 1, message.getData1(), message.getData2());
			break;

		case 0x90:
			strMessage = "note On " + MidiMessageExtractor.getKeyName(message.getData1()) + " velocity: " + message.getData2();
			noticeNoteOn(this, message.getChannel() + 1, message.getData1(), message.getData2());
			break;

		case 0xa0:
			strMessage = "polyphonic key pressure " + MidiMessageExtractor.getKeyName(message.getData1()) + " pressure: " + message.getData2();
			break;

		case 0xb0:
			strMessage = "control change " + message.getData1() + " value: " + message.getData2();
			noticeControlChange(this, message.getChannel() + 1, message.getData1(), message.getData2());
			break;

		case 0xc0:
			strMessage = "program change " + message.getData1();
			break;

		case 0xd0:
			strMessage = "key pressure " + MidiMessageExtractor.getKeyName(message.getData1()) + " pressure: " + message.getData2();
			break;

		case 0xe0:
			strMessage = "pitch wheel change " + MidiMessageExtractor.get14bitValue(message.getData1(), message.getData2());
			break;

		case 0xF0:
			strMessage = MidiNameConstants.SYSTEM_MESSAGE_TEXT[message.getChannel()];
			switch (message.getChannel())
			{
			case 0x1:
				int	nQType = (message.getData1() & 0x70) >> 4;
				int	nQData = message.getData1() & 0x0F;
				if (nQType == 7)
				{
					nQData = nQData & 0x1;
				}
				strMessage += MidiNameConstants.QUARTER_FRAME_MESSAGE_TEXT[nQType] + nQData;
				if (nQType == 7)
				{
					int	nFrameType = (message.getData1() & 0x06) >> 1;
					strMessage += ", frame type: " + MidiNameConstants.FRAME_TYPE_TEXT[nFrameType];
				}
				break;

			case 0x2:
				strMessage += MidiMessageExtractor.get14bitValue(message.getData1(), message.getData2());
				break;

			case 0x3:
				strMessage += message.getData1();
				break;
			}
			break;

		default:
			strMessage = "unknown message: status = " + message.getStatus() + ", byte1 = " + message.getData1() + ", byte2 = " + message.getData2();
			break;
		}
		if (message.getCommand() != 0xF0)
		{
			int	nChannel = message.getChannel() + 1;
			String	strChannel = "channel " + nChannel + ": ";
			strMessage = strChannel + strMessage;
		}
		smCount++;
		return "["+MidiMessageExtractor.getHexString(message)+"] "+strMessage;
	}



	public String decodeMessage(SysexMessage message)
	{
		byte[]	abData = message.getData();
		String	strMessage = null;
		// System.out.println("sysex status: " + message.getStatus());
		if (message.getStatus() == SysexMessage.SYSTEM_EXCLUSIVE)
		{
			strMessage = "Sysex message: F0" + MidiMessageExtractor.getHexString(abData);
		}
		else if (message.getStatus() == SysexMessage.SPECIAL_SYSTEM_EXCLUSIVE)
		{
			strMessage = "Continued Sysex message F7" + MidiMessageExtractor.getHexString(abData);
			seByteCount--; // do not count the F7
		}
		seByteCount += abData.length + 1;
		seCount++; // for the status byte
		return strMessage;
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



/*** MidiReceiverDumpsAndNotifies.java ***/

