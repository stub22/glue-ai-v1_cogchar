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
 * @author matt
 */
public class FFTBuffer {
	private MeanCalculator myMean;
	private FFTWindow myWindow;
	private Complex[][] myFFTData;
	private int mySize;
	private int myChannels;

	public FFTBuffer(int chan, int size, MeanCalculator mean, FFTWindow win){
		myMean = mean;
		myChannels = chan;
		myWindow = win;
		mySize = size;
		myFFTData = new Complex[myChannels][mySize];
	}

	public void writeData(double[][] data){
		myMean.addSamples(data);
		double mean = 0, std = 0;
		boolean normalize =  myMean != null;
		boolean window = myWindow != null;
		for(int c=0; c<myChannels; c++){
			if(normalize){
				mean = myMean.getMean(c);
				std = myMean.getStd(c);
			}
			int i=0;
			FastFourierTransformer fft = new FastFourierTransformer();
			double[] temp = new double[mySize];
			for(double x : data[c]){
				if(normalize){
					x = (x-mean)/std;
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
