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

package org.cogchar.audio.spectrogram;

import org.apache.commons.math.complex.Complex;

/**
 *
 * @author matt
 */
public class Spectrogram {
	private int mySize;
	private int myWrite;
	private int myTime;
	private double myNormalize;
	private int[][][][] myData;
	private int myChannels;


	public Spectrogram(int chan, int size, int timeLen, double volNormalize){
		myChannels = chan;
		mySize = size;
		myTime = timeLen;
		myWrite = 0;
		myData = new int[myTime][chan][mySize][3];
		myNormalize = volNormalize;
	}

	public void addData(Complex[][] data){
		for(int c=0; c<myChannels; c++){
			for(int i=0; i<mySize; i++){
				Complex cmplx = data[c][i];
				double im = cmplx.getImaginary();
				double rl = cmplx.getReal();
				double mag = Math.sqrt(im*im + rl*rl);
				mag = Math.log10(mag)/myNormalize;
				mag *= 1023.0;
				int db = (int)Math.min(Math.max(0, mag), 1023);
				if(db < 128){
					setColor(c, myWrite, i, 0, 0, db);
				}else if(db < 384){
					setColor(c, myWrite, i, db-128, 0, 128);
				}else if(db < 512){
					setColor(c, myWrite, i, 255, 0, 512-db);
				}else if(db < 768){
					setColor(c, myWrite, i, 255, db-512, 0);
				}else{
					setColor(c, myWrite, i, 255, 255, db-768);
				}
			}
		}
		myWrite++;
	}

	private void setColor(int chan, int t, int i, int r, int g, int b){
		myData[0][chan][i][0] = r;
		myData[0][chan][i][1] = g;
		myData[0][chan][i][2] = b;
	}

	public int[][][] getPixels(int t){
		return myData[0];
	}
}
