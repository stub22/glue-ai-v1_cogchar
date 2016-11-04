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

/**
 *
 * @author Matthew Stevenson <matt@hansonrobokind.com>
 */
public class MeanCalculator {
	private Double[][] myVals;
    private Double[] myMean;
    private Double[] myStd;
    private int myChannels;

	public MeanCalculator(int channels){
        myChannels = channels;
		myVals = new Double[channels][];
        for(int c=0; c<myChannels; c++){
            myVals[c] = new Double[]{0.0,0.0,0.0};
        }
        myMean = new Double[myChannels];
        myStd = new Double[myChannels];
	}

	public void addSamples(double[][] samples) {
		for(int c=0; c<samples.length; c++){
			for(double s : samples[c]){
				myVals[c][1] += s;
				myVals[c][2] += s*s;
			}
			myVals[c][0] += samples[c].length;
            myMean[c] = null;
            myStd[c] = null;
		}
	}

	public double getMean(int c) {
        if(myMean[c] == null){
            myMean[c] = myVals[c][1]/myVals[c][0];
        }
        return myMean[c];
	}

	public double getStd(int c) {
        if(myStd[c] == null){
            myStd[c] = (1.0/myVals[c][0])*Math.sqrt(myVals[c][0]*myVals[c][2] - (myVals[c][1]*myVals[c][1]));
        }
        return myStd[c];
	}

    public double normalize(int c, double x){
        return (x-getMean(c))/getStd(c);
    }
}
