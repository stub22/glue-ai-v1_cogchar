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

import java.util.ArrayList;
import java.util.List;
import org.robokind.api.animation.ControlPoint;
import org.robokind.api.common.position.*;

/**
 *
 * @author Jason G. Pallack <jgpallack@gmail.com>
 */


public class ChannelData<T> {
    private List<ControlPoint<T>> myPairs;
    private int myId;
    private String myName;
    private NormalizableRange<T> myRange;
    
    public ChannelData(int id, String name, NormalizableRange<T> range) {
        if(name == null || range == null) {
            throw new NullPointerException();
        }
        
        myId = id;
        myName = name;
        myRange = range;
        myPairs = new ArrayList();
    }
    
    public ChannelData(int id, String name) {
        if(name == null) {
            throw new NullPointerException();
        }
        
        myId = id;
        myName = name;
        myPairs = new ArrayList();
    }
    
    public int getID() {
        return myId;
    }
    
    public String getName() {
        return myName;
    }
    
    public List<ControlPoint<T>> getPoints() {
        return myPairs;
    }
    
    public void addPoint(ControlPoint<T> point) {
        myPairs.add(point);
    }
    
    public List<ControlPoint<NormalizedDouble>> normalizePoints() {
        if(myRange == null) {
            throw new NullPointerException();
        }
        
        List<ControlPoint<NormalizedDouble>> normPoints = new ArrayList();
        
        for(ControlPoint<T> point: myPairs) {
            NormalizedDouble normPos = myRange.normalizeValue(point.getPosition());
            ControlPoint<NormalizedDouble> normPoint = new ControlPoint(point.getTime(), normPos);
            normPoints.add(normPoint);
        }
        
        return normPoints;
    }
    
    public void setRange(NormalizableRange<T> range) {
        if(range == null) {
            throw new NullPointerException();
        }
        
        myRange = range;
    }
    
    public NormalizableRange<T> getRange() {
        return myRange;
    }
}
