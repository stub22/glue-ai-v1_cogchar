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

package org.cogchar.audio.processing;

import org.apache.commons.math.complex.Complex;
import org.apache.commons.math.transform.FastFourierTransformer;

/**
 *
 * @author Matthew Stevenson <matt@hansonrobokind.com>
 */
public class FFTBuffer {
	private MeanCalculator myMean;
	private FFTWindow myWindow;
	private Complex[][] myFFTData;
	private int mySize;
    private int myChannels;
    private boolean myRealitime;

	public FFTBuffer(int channels, int size, MeanCalculator mean, FFTWindow win, boolean realtime){
        myRealitime = realtime;
        myChannels = channels;
		myMean = mean;
		myWindow = win;
		mySize = size;
		myFFTData = new Complex[myChannels][mySize];
	}

	public void writeData(double[][] data){
		boolean normalize =  myMean != null;
		boolean window = myWindow != null;
        if(myRealitime && window){
            myMean.addSamples(data);
        }
		for(int c=0; c<myChannels; c++){
			int i=0;
			FastFourierTransformer fft = new FastFourierTransformer();
			double[] temp = new double[mySize];
			for(double x : data[c]){
				if(normalize){
					x = myMean.normalize(c, x);
				}
				if(window){
					x = myWindow.applyWindow(i, x);
				}
				temp[i] = x;
				i++;
			}
			myFFTData[c] = fft.transform(temp);
		}
	}

	public Complex[][] getData(){
		return myFFTData;
	}
}
