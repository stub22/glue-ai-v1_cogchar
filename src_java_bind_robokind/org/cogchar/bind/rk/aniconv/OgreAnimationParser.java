/*
 *  Copyright 2012 by The Cogchar Project (www.cogchar.org).
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
package org.cogchar.bind.rk.aniconv;

import java.io.*;
import java.util.logging.*;
import org.robokind.api.animation.ControlPoint;

/**
 *
 * @author Jason G. Pallack <jgpallack@gmail.com>
 */
public class OgreAnimationParser{

    private final static Logger theLogger = Logger.getLogger(OgreAnimationParser.class.getName());

    public static AnimationData parseAnimation(String animName, StreamTokenizer st){
        int id = 0;
         AnimationData animTable = new AnimationData(animName);

        try{            
            while(StreamTokenizer.TT_WORD == st.nextToken()){
                if(!st.sval.equals("anim")){
                    continue;
                }
                
                ChannelData<Double> chanData = parseChannelData(st, id);
                animTable.addChannel(chanData);
                id++;
            }
        }catch(Exception e){
            theLogger.log(Level.WARNING,
                    "Exception while parsing animation source: ", e);
        }
        return animTable;
    }

    private static ChannelData<Double> parseChannelData(StreamTokenizer st,
            int id) throws IOException{
        if(StreamTokenizer.TT_WORD != st.nextToken()){
            throw new IllegalArgumentException();
        }

        String animName = st.sval;
        ChannelData<Double> chanData = new ChannelData<Double>(
                id, animName);

        if(StreamTokenizer.TT_WORD != st.nextToken()){
            throw new IllegalArgumentException();
        }

        if(st.sval.equals("{")){
            parseChannelPoints(st, chanData);
        }

        return chanData;
    }

    private static void parseChannelPoints(StreamTokenizer st,
            ChannelData<Double> chanData) throws IOException{
        ControlPoint<Double> point;

        while(true){
            point = parseControlPoint(st);
            if(point == null){
                if(StreamTokenizer.TT_WORD == st.ttype && "}".equals(st.sval)){
                    return;
                }else if(StreamTokenizer.TT_EOF == st.ttype){
                    throw new IllegalArgumentException();
                }else{
                    continue;
                }
            }

            chanData.addPoint(point);
        }
    }

    private static ControlPoint<Double> parseControlPoint(StreamTokenizer st) throws IOException{
        if(StreamTokenizer.TT_NUMBER != st.nextToken()){
            return null;
        }

        double time = st.nval;

        if(StreamTokenizer.TT_NUMBER != st.nextToken()){
            throw new IllegalArgumentException();
        }

        double position = st.nval;

        ControlPoint<Double> point = new ControlPoint(time, position);

        return point;
    }
}
