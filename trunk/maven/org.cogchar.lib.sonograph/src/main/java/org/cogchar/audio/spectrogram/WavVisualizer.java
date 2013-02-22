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

import org.cogchar.audio.processing.WavProcessor;
import org.cogchar.audio.processing.FFTBuffer;
import org.cogchar.audio.processing.HammingWindow;
import org.cogchar.audio.processing.MeanCalculator;
import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.complex.Complex;

/**
 *
 * @author Matthew Stevenson <matt@hansonrobokind.com>
 */
public class WavVisualizer {
    private MeanCalculator myMean;
    private String myFileName;
    private List<Double[]>[] myAmplVizPoints;
    private FFTBuffer myFFT;
    private Spectrogram[] mySpectrogram;
    private int myFFTLen = 512;
    private int mySpecLen = myFFTLen/2;
    private boolean myDoViz;
    private boolean myDoSpect;
    private int myChannels=2;

    public WavVisualizer(String file){
        myAmplVizPoints = new List[myChannels];
        mySpectrogram = new Spectrogram[myChannels];
        myFileName = file;
        calcMean();
        myDoViz = true;
        myDoSpect = true;
        if(myDoViz){
            calcAmplitudeViz();
        }
        if(myDoSpect){
            calcFFT();
        }
        //print();
    }

    private void calcMean(){
        myMean = new MeanCalculator(myChannels);
        new WavProcessor(myFileName) {
            @Override protected void processSamples(double[][] samples) {
                myMean.addSamples(samples);
            }}.process();
    }

    private void calcFFT(){
        myFFT = new FFTBuffer(myChannels, myFFTLen, myMean, new HammingWindow(myFFTLen), false);
        for(int c=0; c<myChannels; c++){
            mySpectrogram[c] = new Spectrogram(mySpecLen, 1, 2.0, true);
        }
        WavProcessor proc = new WavProcessor(myFileName) {
            @Override protected void processSamples(double[][] samples) {
                myFFT.writeData(samples);
                Complex[][] data = myFFT.getData();
                for(int c=0; c<myChannels; c++){
                    mySpectrogram[c].addData(data[c]);
                }
        }};
        proc.setSamplesBufferSize(myFFTLen);
        proc.process();
    }

    private void calcAmplitudeViz(){
        for(int c=0; c<myChannels; c++){
            myAmplVizPoints[c] = new ArrayList();
        }
        WavProcessor proc = new WavProcessor(myFileName) {
            @Override protected void processSamples(double[][] samples) {
                for(int c=0; c<myChannels; c++){
                    Double max = Double.NEGATIVE_INFINITY;
                    Double min = Double.POSITIVE_INFINITY;
                    for(double s : samples[c]){
                        //s = myMean.normalize(s);
                        if(s > max){
                            max = s;
                        }
                        if(s < min){
                            min = s;
                        }
                    }
                    myAmplVizPoints[c].add(new Double[]{max,min});
                }
        }};
        proc.process();
    }

    public MeanCalculator getMean(){
        return myMean;
    }

    public void paint(Graphics g, int c, int h, int w){
        if(myDoViz && myDoSpect){
            int h2 = h/2;
            Graphics vizG = g.create(0, 0, w, h2);
            Graphics specG = g.create(0, h2, w, h2);
            paintResizedViz(vizG, c, h2, w);
            paintResizedSpec(specG, c, h2, w);
        }else if(myDoViz){
            paintResizedViz(g, c, h, w);
        }else if(myDoSpect){
            paintResizedSpec(g, c, h, w);
        }

    }

    private void paintResizedViz(Graphics g, int c, int h, int w){
        int len = myAmplVizPoints[c].size();
        int win = (int)Math.ceil((double)len/(double)w);
        paintViz(g, c, h, w, win);
    }

    private void paintResizedSpec(Graphics g, int c, int h, int w){
        int len = mySpectrogram[c].size();
        int win = (int)Math.ceil((double)len/(double)w);
        paintSpec(g, c, h, w, win);
    }

    public void paintViz(Graphics g, int c, int h, int w, int win){
        win = Math.max(win, 1);
        int k=0;
        for(int i=0; i<w; i++){
            Double max = Double.NEGATIVE_INFINITY;
            Double min = Double.POSITIVE_INFINITY;
            Double avgMax = 0.0;
            Double avgMin = 0.0;
            for(int j=0; j<win; j++){
                if(k >= myAmplVizPoints[c].size()){
                    break;
                }
                Double[] vals = myAmplVizPoints[c].get(k++);
                if(vals[0] > max){
                    max = vals[0];
                }
                if(vals[1] < min){
                    min = vals[1];
                }
                avgMax += vals[0];
                avgMin += vals[1];
            }
            avgMax /= win;
            avgMin /= win;
            if(max == Double.NEGATIVE_INFINITY || min == Double.POSITIVE_INFINITY){
                return;
            }
            line(g,h,i,max,min,Color.RED);
            line(g,h,i,avgMax,avgMin,Color.BLUE);
        }
    }

    private void line(Graphics g, int h, int x, double y1, double y2, Color col){
        double ma = ((y1+1.0)/2.0)*(double)h;
        double mi = ((y2+1.0)/2.0)*(double)h;
        g.setColor(col);
        g.drawLine(x, (int)ma, x, (int)mi);
    }
    
    public void paintSpec(Graphics g, int c, int h, int w, int win){
        int k=0;
        for(int i=0; i<w; i++){
            if(k >= mySpectrogram[c].size()){
                break;
            }
            int[][] data = new int[mySpecLen][3];
            for(int j=0; j<win; j++){
                if(k >= mySpectrogram[c].size()){
                    break;
                }
                int[][] data2 = mySpectrogram[c].getPixels(k++);
                for(int x=0; x<mySpecLen; x++){
                    for(int y=0; y<3; y++){
                        data[x][y] += data2[x][y];
                    }
                }
            }
            for(int x=0; x<mySpecLen; x++){
                for(int y=0; y<3; y++){
                    data[x][y] /=win;
                }
            }
            int bH = (int)Math.ceil((double)h/(double)mySpecLen);
            for(int j = 0; j<data.length; j++){
                g.setColor(new Color(data[j][0], data[j][1], data[j][2]));
                g.drawLine(i, (int)(h-j*bH), i, (int)(h-(j-1)*bH));
            }
        }
    }
}
