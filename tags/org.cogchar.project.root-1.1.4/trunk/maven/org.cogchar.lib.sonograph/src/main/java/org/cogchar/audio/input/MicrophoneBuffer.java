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

package org.cogchar.audio.input;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

/**
 *
 * @author Matthew Stevenson <matt@hansonrobokind.com>
 */
public class MicrophoneBuffer implements AudioBuffer{
	private AudioFormat myFormat;
	private TargetDataLine myDataLine;
	private Long myFramePosition;
	private int myOffset;
	private int mySize;
	private double[][] mySamples;
	private byte[] myRawBytes;
	private int myByteCount;
	
	public MicrophoneBuffer(AudioFormat format, int size){
		myFormat = format;
		mySize = size;
		myByteCount = (format.getSampleSizeInBits()/8);
		myRawBytes = new byte[myFormat.getChannels()*mySize*myByteCount];
		mySamples = new double[myFormat.getChannels()][mySize];
		myOffset = 0;
		initialize();
	}
	
    public void initialize(){
		DataLine.Info dataLineInfo =  new DataLine.Info(TargetDataLine.class, myFormat);
		try{
			myDataLine = (TargetDataLine)AudioSystem.getLine(dataLineInfo);
			myDataLine.open(myFormat);
		}catch(LineUnavailableException ex){
			ex.printStackTrace();
		}
    }

	public void start(){
		if(!myDataLine.isOpen() || myDataLine.isRunning()){
			return;
		}
		myFramePosition = 0L;
		myDataLine.start();
	}

	public boolean hasData(){
		return myOffset > 0;
	}

	public int frameCount(){
		return myOffset;
	}

	public int updateBuffer(){
		//Assuming 8-bit sound right now
		int cnt = myDataLine.read(myRawBytes, 0, myRawBytes.length);
		int k=-1;
		for(int i=0; i<cnt; i++){
			int chan = i%myFormat.getChannels();
			if(chan == 0){
				k++;
			}
			mySamples[chan][k] = getVal(i);
		}
		myOffset += k;
		return myOffset;
	}

	public void clear(){
		myOffset = 0;
	}

	private double getVal(int offset){
		int x = myRawBytes[offset];
		if(x < 0){
			x += 256;
		}
		return (double)(x-128)/128.0;
	}

	public double[][] getData(){
		return mySamples;
	}


}
