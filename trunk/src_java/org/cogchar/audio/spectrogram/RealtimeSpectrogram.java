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

import org.cogchar.audio.input.MicrophoneBuffer;
import org.cogchar.audio.processing.FFTBuffer;
import org.cogchar.audio.processing.HammingWindow;
import org.cogchar.audio.processing.FFTWindow;
import org.cogchar.audio.processing.MeanCalculator;
import javax.sound.sampled.AudioFormat;
import javax.swing.SwingUtilities;

/**
 *
 * @author matt
 */
public class RealtimeSpectrogram implements Runnable{
	private MicrophoneBuffer myInput;
	private FFTWindow myWindow;
	private MeanCalculator myMean;
	private FFTBuffer myFFT;
	private Spectrogram mySpectrogram;
	private SpectrogramPanel myPanel;
	private boolean myStop;

	public RealtimeSpectrogram(AudioFormat format, int chan, int fftLen, SpectrogramPanel panel, double normalization){
		myInput = new MicrophoneBuffer(format, fftLen);
		myWindow = new HammingWindow(fftLen);
		myMean = new MeanCalculator(chan);
		myFFT = new FFTBuffer(chan, fftLen, myMean, myWindow);
		mySpectrogram = new Spectrogram(chan, fftLen/2, 1, normalization);
		myPanel = panel;
	}

	public void run(){
		myStop = false;
		myInput.start();
		while(!myStop){
			if(myInput.hasData()){
				myFFT.writeData(myInput.getData());
				mySpectrogram.addData(myFFT.getData());
				updatePanel();
				myInput.clear();
			}else{
				myInput.updateBuffer();
				try{Thread.sleep(5L);}
				catch(Throwable t){}
			}
		}
	}
	
	private void updatePanel(){
		try{
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					myPanel.addData(mySpectrogram.getPixels(0)[0]);
				}
			});
		}catch(Throwable t){}
		
	}

	public void stop(){
		myStop = true;
	}
}
