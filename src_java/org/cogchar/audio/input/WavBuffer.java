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

import java.io.File;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

/**
 *
 * @author matt
 */
public class WavBuffer implements AudioBuffer {
    private String myFileName;
    private int myBytesPerFrame;
    private int myBufferLength;
    private AudioInputStream myAudioStream;

    public WavBuffer(String fileName){
        myFileName = fileName;
    }

    public void initialize(){
        File fileIn = new File(myFileName);
        try {
            myAudioStream = AudioSystem.getAudioInputStream(fileIn);
            myBytesPerFrame = myAudioStream.getFormat().getFrameSize();
            if (myBytesPerFrame == AudioSystem.NOT_SPECIFIED) {
                myBytesPerFrame = 1;
            }
            myBufferLength = 1024 * myBytesPerFrame;
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void reset(){
        
    }

	public double[][] getData() {
        int totalFramesRead = 0;
        byte[] audioBytes = new byte[myBufferLength];
        try {
            int numBytesRead = 0;
            int numFramesRead = 0;
            // Try to read numBytes bytes from the file.
            while ((numBytesRead = myAudioStream.read(audioBytes, 0, myBufferLength)) != -1) {
                numFramesRead = numBytesRead / myBytesPerFrame;
                totalFramesRead += numFramesRead;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
	}

	public boolean hasData() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

}
