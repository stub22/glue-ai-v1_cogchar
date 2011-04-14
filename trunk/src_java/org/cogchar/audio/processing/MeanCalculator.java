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

import java.util.List;

/**
 *
 * @author matt
 */
public class MeanCalculator {
	private int myChannels;
	private double[][] myVals;

	public MeanCalculator(int chan){
		myChannels = chan;
		myVals = new double[myChannels][3];
	}

	public void addSamples(double[][] samples) {
		for(int c=0; c<samples.length; c++){
			for(double s : samples[c]){
				myVals[c][1] += s;
				myVals[c][2] += s*s;
			}
			myVals[c][0] += samples[c].length;
		}
	}

	public double getMean(int chan) {
		return myVals[chan][1]/myVals[chan][0];
	}

	public double getStd(int chan) {
		return (1.0/myVals[chan][0])*Math.sqrt(myVals[chan][0]*myVals[chan][2] - (myVals[chan][1]*myVals[chan][1]));
	}
}
