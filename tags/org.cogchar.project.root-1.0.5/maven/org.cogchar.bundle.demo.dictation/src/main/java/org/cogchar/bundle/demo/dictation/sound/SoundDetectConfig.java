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

import javax.sound.sampled.AudioFormat;

/**
 *
 * @author Matthew Stevenson <www.robokind.org>
 */
public class SoundDetectConfig {
    public static final int DEFAULT_BUFFER_SIZE=64;
    public static final long DEFAULT_MIN_SOUND_LENGTH=124L;
    public static final long DEFAULT_VERY_LONG_SOUND_LENGTH=1200L;
    public static final long DEFAULT_SHORT_SILENCE_LENGTH=100L;
    public static final long DEFAULT_LONG_SILENCE_LENGTH = 700L;
    public static final long DEFAULT_SEND_WAIT_LENGTH = 50L;
    public static final double DEFAULT_VOLUME_THRESHOLD=10.0;
    
    private int myBufferSize;
    private long myMinSoundLength;
    private long myLongSoundLength;
    private long myShortSilenceLength;
    private long myLongSilenceLength;
    private long mySendWaitLength;
    private double myVolumeThreshold;
    private AudioFormat myAudioFormat;

    public SoundDetectConfig(){
        myVolumeThreshold = DEFAULT_VOLUME_THRESHOLD;
        myMinSoundLength = DEFAULT_MIN_SOUND_LENGTH;
        myLongSoundLength = DEFAULT_VERY_LONG_SOUND_LENGTH;
        myShortSilenceLength = DEFAULT_SHORT_SILENCE_LENGTH;
        myLongSilenceLength = DEFAULT_LONG_SILENCE_LENGTH;
        mySendWaitLength = DEFAULT_SEND_WAIT_LENGTH;
        myBufferSize = DEFAULT_BUFFER_SIZE;
        myAudioFormat = getDefaultAudioFormat();
    }
    
    public SoundDetectConfig(
            int bufferSize, long minSoundLength, 
            long longSoundLength, long shortSilenceLength, 
            long longSilenceLength, long sendWaitLength,
            double volumeThreshold, AudioFormat audioFormat) {
        myBufferSize = bufferSize;
        myMinSoundLength = minSoundLength;
        myLongSoundLength = longSoundLength;
        myShortSilenceLength = shortSilenceLength;
        myLongSilenceLength = longSilenceLength;
        mySendWaitLength = sendWaitLength;
        myVolumeThreshold = volumeThreshold;
        myAudioFormat = audioFormat;
    }

    public AudioFormat getAudioFormat() {
        return myAudioFormat;
    }

    public int getBufferSize() {
        return myBufferSize;
    }

    public long getLongSilenceLength() {
        return myLongSilenceLength;
    }

    public long getLongSoundLength() {
        return myLongSoundLength;
    }

    public long getMinSoundLength() {
        return myMinSoundLength;
    }

    public long getShortSilenceLength() {
        return myShortSilenceLength;
    }

    public long getSendWaitLength() {
        return mySendWaitLength;
    }

    public double getVolumeThreshold() {
        return myVolumeThreshold;
    }
    
    private static AudioFormat getDefaultAudioFormat(){
        return new AudioFormat(8000.0F, 8, 1, true, true);
    }
}
