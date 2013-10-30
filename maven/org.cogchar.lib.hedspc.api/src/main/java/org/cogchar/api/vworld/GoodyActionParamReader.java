/*
 *  Copyright 2013 by The Cogchar Project (www.cogchar.org).
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

package org.cogchar.api.vworld;

import org.appdapter.core.name.Ident;
import org.cogchar.api.thing.TypedValueMap;
import org.cogchar.name.goody.GoodyNames;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class GoodyActionParamReader {
	private		TypedValueMap	myTVM;
	public GoodyActionParamReader(TypedValueMap  tvm) {
		myTVM = tvm;
	}
	public Float[] getVec2D(Ident xName, Ident yName) {
		Float resultVec[] = new Float[2];
		try {
			// Read LocX, LocY, LocZ from some assumed properties.
			resultVec[0] = myTVM.getAsFloat(xName);
			resultVec[1] = myTVM.getAsFloat(yName);
		} catch (Exception e) {
			// Just leave resultVec cells empty 
		}
		return resultVec;
	}	
	public Float[] getVec3D(Ident xName, Ident yName, Ident zName) {
		Float resultVec[] = new Float[3];
		try {
			// Read LocX, LocY, LocZ from some assumed properties.
			resultVec[0] = myTVM.getAsFloat(xName);
			resultVec[1] = myTVM.getAsFloat(yName);
			resultVec[2] = myTVM.getAsFloat(zName);
		} catch (Exception e) {
			// Just leave resultVec cells empty 
		}
		return resultVec;
	}
	public Float[] getVec4D(Ident aName, Ident bName, Ident cName, Ident dName) {
		Float resultVec[] = new Float[4];
		try {
			// Read LocX, LocY, LocZ from some assumed properties.
			resultVec[0] = myTVM.getAsFloat(aName);
			resultVec[1] = myTVM.getAsFloat(bName);
			resultVec[2] = myTVM.getAsFloat(cName);
			resultVec[3] = myTVM.getAsFloat(dName);
		} catch (Exception e) {
			// Just leave resultVec cells empty 
		}
		return resultVec;
	}	
	public Float[] getLocationVec3D() {
		Float[] result = getVec3D(GoodyNames.LOCATION_X, GoodyNames.LOCATION_Y, GoodyNames.LOCATION_Z);
		// If X and Y are specified but not Z, we can assume Z=0:
		if ((result[2] == null)) {
			result[2] = 0f;
		}
		return result;
	}
	public Float[] getSizeVec3D() {
		return getVec3D(GoodyNames.SIZE_X, GoodyNames.SIZE_Y, GoodyNames.SIZE_Z);			
	}
	public Float[] getScaleVec3D() {
		return getVec3D(GoodyNames.SCALE_X, GoodyNames.SCALE_Y, GoodyNames.SCALE_Z);			
	}
	public Float[] getColorVec4D() {
		Float result[] = getVec4D(GoodyNames.COLOR_RED, GoodyNames.COLOR_GREEN, GoodyNames.COLOR_BLUE, 
					GoodyNames.COLOR_ALPHA);
			// Default to alpha=1 if not specified
		if (result[3] == null) {
			result[3] = 1f;
		}
		return result;
	}
	// Since we are supporting scale changes as a relatively fundamental feature of goody actions, it likely
	// makes sense to add this method as well.     "This replaces the text size method." <--- Hmmmm
	public Float getScaleUniform() {
		return myTVM.getAsFloat(GoodyNames.SCALE_UNIFORM);
	}
	
	public Float[] getRotAxisVec3D() {
		return getVec3D(GoodyNames.ROTATION_AXIS_X, GoodyNames.ROTATION_AXIS_Y, GoodyNames.ROTATION_AXIS_Z);		
	}
	public Float getRotMagRadians() {
        Float rot = myTVM.getAsFloat(GoodyNames.ROTATION_MAG_DEG);
        if(rot == null){
            return 0f;
        }
		return rot*(float)Math.PI/180f;		
	}
	public String getText() {
		return myTVM.getAsString(GoodyNames.TEXT);
	}
	
	// May not need its own method, but since a speed of action may be a common feature of GoodyActions, let's
	// assume for now it makes sense to get speed this way instead of with getSpecialString
	public Float getTravelTime() {
		return myTVM.getAsFloat(GoodyNames.TRAVEL_TIME);
	}
	

	// Could have more elaborate type handling here, but for now, since params in repo are natively strings
	// we'll provide a way to load those raw strings by param name
	public String getSpecialString(Ident paramIdent) {
		return myTVM.getAsString(paramIdent);
	}

	public Boolean getSpecialBoolean(Ident paramIdent) {
		return myTVM.getAsBoolean(paramIdent);
	}

	public Integer getSpecialInteger(Ident paramIdent) {
		return myTVM.getAsInteger(paramIdent);
	}
	
}
