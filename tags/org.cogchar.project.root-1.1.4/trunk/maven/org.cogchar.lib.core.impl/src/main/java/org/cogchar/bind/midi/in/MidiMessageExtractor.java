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

import javax.sound.midi.ShortMessage;
import org.cogchar.bind.midi.general.MidiNameConstants;


/**
 * @author Stu B. <www.texpedient.com>
 * Includes code from Jsresources.org.  See copyright notices at bottom.
 */

public class MidiMessageExtractor {
	public static String getKeyName(int nKeyNumber)
	{
		if (nKeyNumber > 127)
		{
			return "illegal value";
		}
		else
		{
			int	nNote = nKeyNumber % 12;
			int	nOctave = nKeyNumber / 12;
			return MidiNameConstants.sm_astrKeyNames[nNote] + (nOctave - 1);
		}
	}


	public static int get14bitValue(int nLowerPart, int nHigherPart)
	{
		return (nLowerPart & 0x7F) | ((nHigherPart & 0x7F) << 7);
	}



	private static int signedByteToUnsigned(byte b)
	{
		return b & 0xFF;
	}

	// convert from microseconds per quarter note to beats per minute and vice versa
	public static float convertTempo(float value) {
		if (value <= 0) {
			value = 0.1f;
		}
		return 60000000.0f / value;
	}



	private static char hexDigits[] = 
	   {'0', '1', '2', '3', 
	    '4', '5', '6', '7', 
	    '8', '9', 'A', 'B', 
	    'C', 'D', 'E', 'F'};

	public static String getHexString(byte[] aByte)
	{
		StringBuffer	sbuf = new StringBuffer(aByte.length * 3 + 2);
		for (int i = 0; i < aByte.length; i++)
		{
			sbuf.append(' ');
			sbuf.append(hexDigits[(aByte[i] & 0xF0) >> 4]);
			sbuf.append(hexDigits[aByte[i] & 0x0F]);
			/*byte	bhigh = (byte) ((aByte[i] &  0xf0) >> 4);
			sbuf.append((char) (bhigh > 9 ? bhigh + 'A' - 10: bhigh + '0'));
			byte	blow = (byte) (aByte[i] & 0x0f);
			sbuf.append((char) (blow > 9 ? blow + 'A' - 10: blow + '0'));*/
		}
		return new String(sbuf);
	}
	
	private static String intToHex(int i) {
		return ""+hexDigits[(i & 0xF0) >> 4]
		         +hexDigits[i & 0x0F];
	}

	public static String getHexString(ShortMessage sm)
	{
		// bug in J2SDK 1.4.1
		// return getHexString(sm.getMessage());
		int status = sm.getStatus();
		String res = intToHex(sm.getStatus());
		// if one-byte message, return
		switch (status) {
			case 0xF6:			// Tune Request
			case 0xF7:			// EOX
	    		// System real-time messages
			case 0xF8:			// Timing Clock
			case 0xF9:			// Undefined
			case 0xFA:			// Start
			case 0xFB:			// Continue
			case 0xFC:			// Stop
			case 0xFD:			// Undefined
			case 0xFE:			// Active Sensing
			case 0xFF: return res;
		}
		res += ' '+intToHex(sm.getData1());
		// if 2-byte message, return
		switch (status) {
			case 0xF1:			// MTC Quarter Frame
			case 0xF3:			// Song Select
					return res;
		}
		switch (sm.getCommand()) {
			case 0xC0:
			case 0xD0:
					return res;
		}
		// 3-byte messages left
		res += ' '+intToHex(sm.getData2());
		return res;
	}
}
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