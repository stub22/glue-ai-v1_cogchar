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
package org.cogchar.api.vworld;

import org.appdapter.core.name.Ident;
// import org.cogchar.impl.thing.basic.BasicTypedValueMap;
import org.cogchar.name.goody.GoodyNames;
import org.cogchar.api.thing.ActionParamWriter;
import org.cogchar.api.thing.TypedValueMap;

/**
 * Typically used from a remote client to capture values for encoding in SPARQL-Update.
 *
 * @author Stu B. <www.texpedient.com>
 */
public class GoodyActionParamWriter extends ActionParamWriter {


	public GoodyActionParamWriter(TypedValueMap  tvm) {
		super(tvm);
	}

	public void putLocation(float locX, float locY, float locZ) {
		TypedValueMap tvm = getValueMap();
		tvm.putValueAtName(GoodyNames.LOCATION_X, locX);
		tvm.putValueAtName(GoodyNames.LOCATION_Y, locY);
		tvm.putValueAtName(GoodyNames.LOCATION_Z, locZ);
	}

	public void putRotation(float rotAxisX, float rotAxisY, float rotAxisZ, float magDeg) {
		TypedValueMap tvm = getValueMap();
		tvm.putValueAtName(GoodyNames.ROTATION_AXIS_X, rotAxisX);
		tvm.putValueAtName(GoodyNames.ROTATION_AXIS_Y, rotAxisY);
		tvm.putValueAtName(GoodyNames.ROTATION_AXIS_Z, rotAxisZ);
		tvm.putValueAtName(GoodyNames.ROTATION_MAG_DEG, magDeg);
	}

	public void putSize(float sizeX, float sizeY, float sizeZ) {
		TypedValueMap tvm = getValueMap();
		tvm.putValueAtName(GoodyNames.SIZE_X, sizeX);
		tvm.putValueAtName(GoodyNames.SIZE_Y, sizeY);
		tvm.putValueAtName(GoodyNames.SIZE_Z, sizeZ);
	}
	
	public void putScale(float scalarScale) {
		TypedValueMap tvm = getValueMap();
		putScale(scalarScale, scalarScale, scalarScale);
	}
	
	public void putScale(float scaleX, float scaleY, float scaleZ) {
		TypedValueMap tvm = getValueMap();
		tvm.putValueAtName(GoodyNames.SCALE_X, scaleX);
		tvm.putValueAtName(GoodyNames.SCALE_Y, scaleY);
		tvm.putValueAtName(GoodyNames.SCALE_Z, scaleZ);
	}
	
	// Not sure if this is how we want this to look or how we want it named, but good for starters...
	public void putDuration(float duration) {
		TypedValueMap tvm = getValueMap();
		tvm.putValueAtName(GoodyNames.TRAVEL_TIME, duration);
	}
	
	public void putColor(float colorR, float colorG, float colorB, float colorAlpha) {
		TypedValueMap tvm = getValueMap();
		tvm.putValueAtName(GoodyNames.COLOR_RED, colorR);
		tvm.putValueAtName(GoodyNames.COLOR_GREEN, colorG);
		tvm.putValueAtName(GoodyNames.COLOR_BLUE, colorB);
		tvm.putValueAtName(GoodyNames.COLOR_ALPHA, colorAlpha);
	}
	
	public void putObjectAtName(Ident pName, Object pVal) {
		TypedValueMap tvm = getValueMap();
		tvm.putValueAtName(pName, pVal);
	}
	public void putNameAtName(Ident pName, Ident pVal) {
		TypedValueMap tvm = getValueMap();
		tvm.putNameAtName(pName, pVal);
	}
	
	/**  type is *not* a param.  So, we don't want to have code like this:
	void putType(Ident someTypeID) {tvm.putNameAtName(GoodyNames.RDF_TYPE, someTypeID);}
	*/

}
