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
public class VisualizationConverter implements AudioConverter {
    private int myChannelCount;
    private int mySampleBytes;
    private int myFrameBytes;
    private boolean myIsSigned;
    private int myRange;
    private int myMax;
    private boolean myIsBigEndian;
    
    public VisualizationConverter(int channelCount, int sampleByte, boolean signed, boolean bigEndian){
        if(sampleByte > 3){
            throw new IllegalArgumentException("Cannot convert greater than 24-bit samples.");
        }
        myChannelCount = channelCount;
        mySampleBytes = sampleByte;
        myIsSigned = signed;
        myRange = 1 << sampleByte*8;
        myMax = 1 << (sampleByte*8 - 1);
        myFrameBytes = myChannelCount*mySampleBytes;
        myIsBigEndian = bigEndian;
    }

    public double[][] convert(byte[] data) {
        return convert(data,0,data.length);
    }

    public double[][] convert(byte[] data, int offset, int len) {
        if(len%myFrameBytes != 0){
            throw new IllegalArgumentException("Bad conversion length.  Len " + len + " is not a multiple of the frame size " + myFrameBytes);
        }
        int frames = len/myFrameBytes;
        double[][] samples = new double[myChannelCount][frames];
        for(int i=0; i<frames; i++, offset+=myFrameBytes){
            Double[] frame = getFrame(data, offset);
            for(int j=0; j<myChannelCount; j++){
                samples[j][i] = frame[j];
            }
        }
        return samples;
    }

    private Double[] getFrame(byte[] data, int offset) {
        Double[] frame = new Double[myChannelCount];
        for(int i=0; i<myChannelCount; i++){
            frame[i] = getSample(data, offset+i*mySampleBytes, myIsBigEndian);
        }
        return frame;
    }

    private double getSample(byte[] data, int offset, boolean bigEndian){
        int sample = bigEndian ? getBESample(data, offset) : getLESample(data, offset);
        if(!myIsSigned){
            if(sample < 0){
                sample += myRange;
            }
            sample -= myMax;
        }
        if(sample > 0){
            return ((double)sample)/(double)(myMax-1);
        }else{
            return ((double)sample)/(double)myMax;
        }
    }

    private int getBESample(byte[] data, int offset){
        int sample = data[offset];
        for(int i=1; i<mySampleBytes; i++){
            sample = (sample << 8) + (data[offset+i] & 0xFF);
        }
        return sample;
    }

    private int getLESample(byte[] data, int offset){
        int sample = data[offset+mySampleBytes-1];
        for(int i=mySampleBytes-2; i>=0; i--){
            sample = (sample << 8) + (data[offset+i] & 0xFF);
        }
        return sample;
    }
}
