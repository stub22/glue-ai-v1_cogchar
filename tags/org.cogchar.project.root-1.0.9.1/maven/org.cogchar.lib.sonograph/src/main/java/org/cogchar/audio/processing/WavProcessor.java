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

import java.io.File;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

/**
 *
 * @author Matthew Stevenson <matt@hansonrobokind.com>
 */
public abstract class WavProcessor {
    protected String myFileName;
    private AudioConverter myConverter;
    private AudioInputStream myAudioStream;
    private int myBufferSize;
    private int myBufferSamples;

    public WavProcessor(String fileName){
        myFileName = fileName;
        myBufferSamples = 256;
    }
    
    public void setSamplesBufferSize(int size){
        myBufferSamples = size;
        int frameBytes = 4;
        if(myAudioStream  != null && myAudioStream.getFormat() != null){
            frameBytes = myAudioStream.getFormat().getFrameSize();
        }
        myBufferSize = myBufferSamples*frameBytes;
    }

    public void process(){
        File fileIn = new File(myFileName);
        try {
            myAudioStream = AudioSystem.getAudioInputStream(fileIn);
            AudioFormat format = myAudioStream.getFormat();
            int bytesPerSample = format.getSampleSizeInBits()/8;
            boolean signed = format.getEncoding() == AudioFormat.Encoding.PCM_SIGNED;
            myConverter = new VisualizationConverter(format.getChannels(), bytesPerSample, signed, format.isBigEndian());
            myBufferSize = myBufferSamples*bytesPerSample*format.getChannels();
            processStream();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void processStream(){
        byte[] buffer = new byte[myBufferSize];
        while(processBuffer(buffer) == myBufferSize){}
    }
    
    private int processBuffer(byte[] buffer){
        int numBytesRead = 0;
        try {
            numBytesRead = myAudioStream.read(buffer, 0, buffer.length);
            double[][] samples = myConverter.convert(buffer);
            processSamples(samples);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return numBytesRead;
    }

    protected abstract void processSamples(double[][] samples);
}
