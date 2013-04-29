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
import org.robokind.api.common.position.NormalizableRange;
import org.robokind.api.common.position.NormalizedDouble;

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
			
			
            NormalizedDouble normPos = myRange.normalizeValue(point.getPosition()); // Returns null if out of range
			
			/* This section only needed for handling out-of-range animation channel Double values during conversion by substituting
			 * nearest limit (0 or 1) as appropriate. Perhaps the functionality
			 * to indicate which range is exceeded should be added to NormalizableRange if we need it in the long run, where we can
			 * generalize it to be effective for more than just ControlPoint<Double>.
			 */
			if ((normPos == null) && (point.getPosition() instanceof Double)){
				Double position = (Double)point.getPosition();
				if ((position == null) || (position.isNaN()))  {
					normPos = new NormalizedDouble(0.5);
				} else {
					boolean normalPolarity = ((Double)myRange.getMax() > (Double)myRange.getMin());
					boolean greaterThanMax = (position > (Double)myRange.getMax());
					normPos = (normalPolarity ^ greaterThanMax)? new NormalizedDouble(0.0) : new NormalizedDouble(1.0);
				}
			} 
			
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
