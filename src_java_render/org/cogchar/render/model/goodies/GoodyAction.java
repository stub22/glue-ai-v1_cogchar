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

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import org.appdapter.core.name.Ident;
import org.cogchar.api.thing.ThingActionSpec;
import org.cogchar.api.thing.TypedValueMap;

/**
 * Typically used in the OpenGL server process to interpret an actionSpec found in a repo.
 * An HTTP-client would not normally use this class.
 * 
 * @author Stu B. <www.texpedient.com>
 */

public class GoodyAction  {
	// We can optionallly play a game of equivalence between Java-enum-constant and URI, without an additional hashMap.
	// The price is that we must initialize the value in the enum constants.
	public enum Kind {
		CREATE(GoodyNames.CREATE_URI.getAbsUriString()),
		DELETE(GoodyNames.DELETE_URI.getAbsUriString()),
		MOVE(GoodyNames.MOVE_URI.getAbsUriString()),
		SET(GoodyNames.SET_URI.getAbsUriString());
		
		public	String myKindUriString;
		
		private Kind(String uriString) {
			myKindUriString = uriString;
		}
	}
	private		Kind					myKind;
	
	
	private		ThingActionSpec			mySpec;

	private		Ident					myGoodyID;
	private		Ident					myGoodyTypeID;
	
	private		TypedValueMap			paramTVMap;
	
	public GoodyAction(ThingActionSpec actionSpec) {
		mySpec = actionSpec;
		myGoodyID = actionSpec.getTargetThingID();
		myGoodyTypeID = actionSpec.getTargetThingTypeID();
		
		paramTVMap = mySpec.getParamTVM();
		String kindIdentString = actionSpec.getVerbID().getAbsUriString();
		for (Kind kindToCheck : Kind.values()) {
			if (kindToCheck.myKindUriString.equals(kindIdentString)) {
				myKind = kindToCheck;
				break;
			}
		}
	}
	
	/**
	 * If our ThingActionSpec supplied a targetThingID, we use that by default.
	 * However, during creation or other "special" operation, our GoodyAction action may 
	 * set its GoodyID differently.
	 * @return 
	 */
	public Ident getGoodyID() {
		return myGoodyID;
	}
	// Is this something we want to expose publically? Seems we may need to...
	public Kind getKind() {
		return myKind;
	}
	
	public Ident getType() {
		return myGoodyTypeID;
		// return paramTVMap.getAsIdent(GoodyNames.THING_TYPE);
	}
	/**
	 * Example of actual application data read from spec, into an application specific type.
	 * Will be generalized to use for "goal location", "direction", etc.
	 * @return 
	 */
	public Vector3f getLocationVector() {
		float locX = paramTVMap.getAsFloat(GoodyNames.LOCATION_X);
		float locY = paramTVMap.getAsFloat(GoodyNames.LOCATION_Y);
		float locZ = paramTVMap.getAsFloat(GoodyNames.LOCATION_Z);
		Vector3f resultVec = new Vector3f(locX, locY, locZ);
		// Read LocX, LocY, LocZ from some assumed properties.
		return resultVec;
	}
	/**
	 * Here is a harder one.
	 * Read rotation axis AxisX, AxisY, AxisZ and magDegrees from some assumed properties,
	 * and produce a Quaternion.   Will also be generalized later.
	 * @return rotational operator quaternion object encoding
	 */
	public Quaternion getRotationQuaternion() {
		float rotX = paramTVMap.getAsFloat(GoodyNames.ROTATION_AXIS_X);
		float rotY = paramTVMap.getAsFloat(GoodyNames.ROTATION_AXIS_Y);
		float rotZ = paramTVMap.getAsFloat(GoodyNames.ROTATION_AXIS_Z);
		float rotMag = paramTVMap.getAsFloat(GoodyNames.ROTATION_MAG_DEG)*(float)Math.PI/180f;
		Quaternion resultQuat = new Quaternion().fromAngleAxis(rotMag, new Vector3f(rotX, rotY, rotZ));
		return resultQuat;
	}
	
	// Still figuring this one out; right now assuming size may have up to three components, but sometimes fewer
	public float[] getSize() {
		float sizeX = paramTVMap.getAsFloat(GoodyNames.SIZE_X);
		float sizeY = paramTVMap.getAsFloat(GoodyNames.SIZE_Y);
		float sizeZ = paramTVMap.getAsFloat(GoodyNames.SIZE_Z);
		float[] sizes = {sizeX, sizeY, sizeZ};
		return sizes;
	}
	
	// With the advent of 2D goodies, this seems like it will be needed often enough to warrant its own method:
	public float getTextSize() {
		return paramTVMap.getAsFloat(GoodyNames.TEXT_SIZE);
	}
	
	// May not need its own method, but since a speed of action may be a common feature of GoodyActions, let's
	// assume for now it makes sense to get speed this way instead of with getSpecialString
	public float getTravelTime() {
		return paramTVMap.getAsFloat(GoodyNames.TRAVEL_TIME);
	}
	
	// Could have more elaborate type handling here, but for now, since params in repo are natively strings
	// we'll provide a way to load those raw strings by param name
	public String getSpecialString(Ident paramIdent) {
		return paramTVMap.getAsString(paramIdent);
	}

}
