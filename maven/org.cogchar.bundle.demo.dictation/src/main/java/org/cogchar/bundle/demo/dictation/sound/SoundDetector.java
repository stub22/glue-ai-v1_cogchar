/*
 * Copyright 2012 by The Cogchar Project (www.cogchar.org).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cogchar.bundle.demo.dictation.sound;

import org.cogchar.bundle.demo.dictation.ui.DictationGrabber;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;

/***
 * 
 * @author Matthew Stevenson <www.cogchar.org>
 */
public class SoundDetector{
    private final static Logger theLogger = Logger.getLogger(SoundDetector.class.getName());
    
    private Long myFirstIterationTime;
    private Long myLastIterationTime;
    private Long mySoundBeginning;
    private Long myLastSound;
    private Long mySilenceBeginning;
    private Long myLastSilence;
    private boolean significantSoundHeard;
    private boolean veryLongSoundHeard;
    private boolean shortSilenceSent;
    private long myMaxSoundLength;
    private boolean myRunFlag;
    private boolean myInitializedFlag;
    private SoundDetectConfig myDetectConfig;
    private TargetDataLine myDataLine;
    private CaptureThread myCaptureThread;
    private DictationGrabber myDicationGrabber;
    
    public SoundDetector(DictationGrabber dg){
        this(dg, new SoundDetectConfig());
    }
    
    public SoundDetector(DictationGrabber dg, SoundDetectConfig config){
        if(dg == null || config == null){
            throw new NullPointerException();
        }
        myInitializedFlag = false;
        myDetectConfig = config;
        myDicationGrabber = dg;
        significantSoundHeard=false;
        veryLongSoundHeard=false;
        shortSilenceSent=false;
        myMaxSoundLength = 8000L;
    }
    
    public boolean initialize(){
        if(myInitializedFlag){
            return true;
        }else if(myDataLine != null){
            return false;
        }
        try{
            AudioFormat audioFormat = myDetectConfig.getAudioFormat();
            DataLine.Info dataLineInfo =  
                    new DataLine.Info(TargetDataLine.class, audioFormat);
            myDataLine = (TargetDataLine)AudioSystem.getLine(dataLineInfo);
            myDataLine.open(audioFormat);
            myDataLine.start();
            myInitializedFlag = true;
            return true;
        }catch(Exception ex){
            theLogger.log(Level.WARNING, "Unable to open microphone.", ex);
            return false;
        }
    }
    
    public boolean start(){
        if(!myInitializedFlag){
            if(!initialize()){
                theLogger.warning("Unable to initialize microphone.");
                return false;
            }
        }
        if(myCaptureThread != null){
            theLogger.info("CaptureThread already running.");
            return true;
        }else if(myDataLine == null || !myDataLine.isOpen()){
            theLogger.warning("Unable to start CaptureThread, "
                    + "DataLine is null or closed .");
            return false;
        }
        myCaptureThread = new CaptureThread();
        myCaptureThread.start();
        return true;
    }

    public void stop(){
        myRunFlag = false;
    }
    
    public void close(){
        if(myDataLine == null){
            return;
        }
        myDataLine.stop();
        myDataLine.close();
        myDataLine = null;
    }

    private double calculateDecibels(byte[] buffer, boolean signed, int len) {
        long bufferSum = 0;
        int bufferIndex = 0;
        while (bufferIndex < len) {
            bufferSum += (Math.abs(
                    signed ? buffer[bufferIndex] : buffer[bufferIndex]-128));
            bufferIndex += 2;
        }
        double averageVal = ((double) bufferSum / (len / 2.0));
		if(averageVal <= 1.0){ 
            return 0; 
        }
		double simplePercentage = (averageVal/Math.abs(Byte.MIN_VALUE))*100.0;
		return 20.0 * Math.log10(simplePercentage / 100.0)+ 48;
	}

    private synchronized void determineState(double vol, long timeStamp) {
        if(myLastIterationTime!=null && timeStamp<myLastIterationTime){
            theLogger.warning(
                    "Timestamp may not be less than the previous timestamp");
            return;
        }
        if(myFirstIterationTime==null){
            myFirstIterationTime=timeStamp;
        }
        myLastIterationTime=timeStamp;

        if (vol > myDetectConfig.getVolumeThreshold()) {
            handleSound();
        } else {
            handleSilence();
        }
    }

    private void handleSilence() {
        if (mySilenceBeginning == null){
            mySilenceBeginning = myLastIterationTime;
        }
        myLastSilence = myLastIterationTime;
        long silenceLength = getSilenceLength();
        if (significantSoundHeard && !shortSilenceSent
                && silenceLength > myDetectConfig.getShortSilenceLength()) {
            //theLogger.info("Short Silence");
            shortSilenceSent = true;               
        }
        if (silenceLength > myDetectConfig.getLongSilenceLength()) {
            mySoundBeginning = null;
            if (significantSoundHeard) {
                significantSoundHeard = false;
                if(veryLongSoundHeard){
                    veryLongSoundHeard = false;
                }
            }
            longSilenceDetected();
            mySilenceBeginning = null;
        }
    }

    private void handleSound() {
        if (mySoundBeginning == null){
            mySoundBeginning = myLastIterationTime;
        }
        myLastSound = myLastIterationTime;
        long soundLength = getSoundLength();
        if (soundLength > myMaxSoundLength) {
            mySoundBeginning = null;
            mySilenceBeginning = null;
            significantSoundHeard = false;
            veryLongSoundHeard = false;
            longSilenceDetected();
            shortSilenceSent = false;
            return;
        }
        if (!significantSoundHeard 
                && soundLength > myDetectConfig.getMinSoundLength()) {
            significantSoundHeard = true;
            //theLogger.info("Sound Start Detected");
        }
        if(!veryLongSoundHeard 
                && soundLength > myDetectConfig.getLongSoundLength()){
            veryLongSoundHeard = true;
            //theLogger.info("Long Sound Detected");
        }
        mySilenceBeginning = null;
        shortSilenceSent = false;
    }

    public SoundDetectConfig getSoundDetectConfig(){
        return myDetectConfig;
    }

    public Long getSoundLength() {
        if(mySoundBeginning==null || myLastSound==null) {
            return null;
        }
        return myLastSound-mySoundBeginning;
    }

    public Long getSilenceLength() {
        if(mySilenceBeginning==null || myLastSilence==null) {
            return null;
        }
        return myLastSilence-mySilenceBeginning;
    }

    public void longSilenceDetected()  {
        //theLogger.info("Long Silence");
        boolean update = false;
        while(myDicationGrabber.collectDictation()){
            update = true;
            try{Thread.sleep(myDetectConfig.getSendWaitLength());
            }catch(InterruptedException t){}
        }
        if(update){
            myDicationGrabber.handleDictation();
        }
    }
    
    private class CaptureThread extends Thread {

        public CaptureThread() {
            super("Microphone Volume Monitor");
        }
        
        @Override
        public void run() {
            if(myDataLine == null){
                return;
            }
            myRunFlag = true;
            byte tempBuffer[] = new byte[myDetectConfig.getBufferSize()];
            while(myRunFlag){
                try {
                    int cnt = myDataLine.read(tempBuffer,0,tempBuffer.length);
                    if(cnt<=0){
                        continue;
                    }
                    double vol = calculateDecibels(tempBuffer, true, cnt);
                    determineState(vol, System.currentTimeMillis());
                }catch(Throwable t){
                    theLogger.log(Level.WARNING, 
                            "Error in sound capture thread.", t);
                }
            }
            myCaptureThread = null;
        }
    }
}