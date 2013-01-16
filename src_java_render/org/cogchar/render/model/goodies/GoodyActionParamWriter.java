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
package org.cogchar.render.model.goodies;

import org.cogchar.api.thing.BasicTypedValueMap;
import org.cogchar.api.thing.TypedValueMap;

/**
 * Typically used from a remote client to capture values for encoding in SPARQL-Update.
 *
 * @author Stu B. <www.texpedient.com>
 */
public class GoodyActionParamWriter {

	private BasicTypedValueMap myBTVMap;

	public GoodyActionParamWriter(BasicTypedValueMap btvMap) {
		myBTVMap = btvMap;
	}

	public void putLocation(float locX, float locY, float locZ) {
		myBTVMap.putValueAtName(GoodyNames.LOCATION_X, locX);
		myBTVMap.putValueAtName(GoodyNames.LOCATION_Y, locY);
		myBTVMap.putValueAtName(GoodyNames.LOCATION_Z, locZ);
	}

	public void putRotation(float rotAxisX, float rotAxisY, float rotAxisZ, float magDeg) {
		myBTVMap.putValueAtName(GoodyNames.ROTATION_AXIS_X, rotAxisX);
		myBTVMap.putValueAtName(GoodyNames.ROTATION_AXIS_Y, rotAxisY);
		myBTVMap.putValueAtName(GoodyNames.ROTATION_AXIS_Z, rotAxisZ);
		myBTVMap.putValueAtName(GoodyNames.ROTATION_MAG_DEG, magDeg);
	}

	public void putSize(float sizeX, float sizeY, float sizeZ) {
		myBTVMap.putValueAtName(GoodyNames.SIZE_X, sizeX);
		myBTVMap.putValueAtName(GoodyNames.SIZE_Y, sizeY);
		myBTVMap.putValueAtName(GoodyNames.SIZE_Z, sizeZ);
	}
	
	public void putScale(float scalarScale) {
		putScale(scalarScale, scalarScale, scalarScale);
	}
	
	public void putScale(float scaleX, float scaleY, float scaleZ) {
		myBTVMap.putValueAtName(GoodyNames.SCALE_X, scaleX);
		myBTVMap.putValueAtName(GoodyNames.SCALE_Y, scaleY);
		myBTVMap.putValueAtName(GoodyNames.SCALE_Z, scaleZ);
	}
	
	// Not sure if this is how we want this to look or how we want it named, but good for starters...
	public void putDuration(float duration) {
		myBTVMap.putValueAtName(GoodyNames.TRAVEL_TIME, duration);
	}
	
	public void putColor(float colorR, float colorG, float colorB, float colorAlpha) {
		myBTVMap.putValueAtName(GoodyNames.COLOR_RED, colorR);
		myBTVMap.putValueAtName(GoodyNames.COLOR_GREEN, colorG);
		myBTVMap.putValueAtName(GoodyNames.COLOR_BLUE, colorB);
		myBTVMap.putValueAtName(GoodyNames.COLOR_ALPHA, colorAlpha);
	}
	
	public TypedValueMap	getValueMap() { 
		return myBTVMap;
	}
	/**  type is not a param.
	public void putType(Ident someTypeID) {
		myBTVMap.putNameAtName(GoodyNames.RDF_TYPE, someTypeID);
	}
	*/

}
