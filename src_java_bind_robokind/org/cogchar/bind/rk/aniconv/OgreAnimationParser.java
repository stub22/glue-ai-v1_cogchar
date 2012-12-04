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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.robokind.api.animation.ControlPoint;

/**
 *
 * @author Jason G. Pallack <jgpallack@gmail.com>
 */

// Many modifications to this class to support conversion from legacy anim files created with the A07 aka A04 model
// Will be cleaned up and buttoned down as we converge on final avatar model conventions
// Ryan Biggs Nov-Dec 2012
public class OgreAnimationParser{

    private final static Logger theLogger = LoggerFactory.getLogger(OgreAnimationParser.class.getName());
	
	private final static double MAX_ALLOWED_TIME = 1000.0; // seconds

    public static AnimationData parseAnimation(String animName, StreamTokenizer st){
        st.wordChars(0x21, 0x7E);
        int id = 0;
        
		AnimationData animTable = new AnimationData(animName);
		 
        try{            
            while(StreamTokenizer.TT_WORD == st.nextToken()){
				if(!"anim".equals(st.sval)){ // TEST ONLY
                    continue;
                }
                //System.out.println("Adding channel, found " + st.toString()); // TEST ONLY
                ChannelData<Double> chanData = parseChannelData(st, id);
                animTable.addChannel(chanData);
                id++;
				
            }
        }catch(Exception e){
            theLogger.warn("Exception while parsing animation source: ", e);
        }
        return animTable;
    }

    private static ChannelData<Double> parseChannelData(StreamTokenizer st,
            int id) throws IOException{
		
        if(StreamTokenizer.TT_WORD != st.nextToken()){
            throw new IllegalArgumentException();
        }

        String animName = st.sval;
		// Temporary ugly way to remove the following tag found in A07 .anim channel names:
		String strippedA04PrefixChanName = animName.replaceAll("AZR50New_Rig_FINAL:", "");
        ChannelData<Double> chanData = new ChannelData<Double>(
                id, strippedA04PrefixChanName);

        if(StreamTokenizer.TT_WORD != st.nextToken()){
            throw new IllegalArgumentException(st.sval);
        }

        if(st.sval.equals("{")){
			st.nextToken();
			st.nextToken(); // Step past Time and Value headers - ugly temporary method
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
		
		// Handle scientific notation if we find it
		// This does occur often in A07 anims
		int nextThingy = st.nextToken();
		if ((nextThingy == -3) && (st.sval.startsWith("e"))) {
			int exponent = Integer.valueOf(st.sval.replaceAll("e", ""));
			position = position * Math.pow(10.0, exponent);
		} else {
			st.pushBack();
		}
		
		// This was added due to "extreme" time points found in many A07 .anim files, which confuse downstream software
		if ((time < 0) || (time > MAX_ALLOWED_TIME)) {
			theLogger.warn("Found time out of allowed range in anim file ({}), ignoring", String.valueOf(time)); 
            return null;
        }

        ControlPoint<Double> point = new ControlPoint(time, position);

        return point;
    }
}
