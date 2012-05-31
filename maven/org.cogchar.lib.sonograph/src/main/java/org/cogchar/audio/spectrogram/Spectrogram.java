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

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math.complex.Complex;

/**
 *
 * @author Matthew Stevenson <matt@hansonrobokind.com>
 */
public class Spectrogram {
	private int mySize;
    private int myTime;
	private double myNormalize;
	private final List<int[][]> myData;
    private int[][] myDataBuffer;
    int myBufferIndex;
    private boolean myStore;


	public Spectrogram(int size, int time, double volNormalize, boolean store){
        myStore = store;
		mySize = size;
        myTime = 1;
		myData = new ArrayList<int[][]>(1000);
        //myDataBuffer = new int[myTime][mySize][3];
        myDataBuffer = new int[mySize][3];
        myBufferIndex = 0;
		myNormalize = volNormalize;
	}

	public void addData(Complex[] data){
        for(int i=0; i<mySize; i++){
            Complex cmplx = data[i];
            double im = cmplx.getImaginary();
            double rl = cmplx.getReal();
            double mag = Math.sqrt(im*im + rl*rl);
            mag = Math.log10(mag)/myNormalize;
            mag *= 1023.0;
            int db = (int)Math.min(Math.max(0, mag), 1023);
            if(db < 128){
                setColor(i, 0, 0, db);
            }else if(db < 384){
                setColor(i, db-128, 0, 128);
            }else if(db < 512){
                setColor(i, 255, 0, 512-db);
            }else if(db < 768){
                setColor(i, 255, db-512, 0);
            }else{
                setColor(i, 255, 255, db-768);
            }
        }
        if(myStore){
            myData.add(myDataBuffer);
            myDataBuffer = new int[mySize][3];
        }
	}

	private void setColor(int i, int r, int g, int b){
		myDataBuffer[i][0] = r;
		myDataBuffer[i][1] = g;
		myDataBuffer[i][2] = b;
        /*myDataBuffer[myBufferIndex][i][0] = r;
		myDataBuffer[myBufferIndex][i][1] = g;
		myDataBuffer[myBufferIndex][i][2] = b;
        myBufferIndex++;
        if(myBufferIndex >= myDataBuffer.length){
            copyBuffer();
        }*/
	}

    /*private void copyBuffer(){
        for(int i=0; i<myBufferIndex; i++){
            myData.add(myDataBuffer[i]);
            myDataBuffer[i] = new int[mySize][3];
        }
        myBufferIndex = 0;
    }

    public List<int[][]> getPixels(){
        if(myBufferIndex > 0){
            copyBuffer();
        }
        return myData;
    }*/

	public int[][] getPixels(int i){
        if(myStore){
            return myData.get(i);
        }else{
            return myDataBuffer;
        }

	}

    public int size(){
        return myData.size();
    }
}
