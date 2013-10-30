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

package org.cogchar.render.app.entity;

import org.cogchar.name.goody.GoodyNames;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;

import org.appdapter.core.name.Ident;
import org.cogchar.api.thing.ThingActionSpec;
import org.cogchar.api.thing.TypedValueMap;
import org.cogchar.api.vworld.GoodyActionParamReader;


/**
 * This wrapper object knows how to unpack a GoodyAction.
 * 
 * It is typically used in our OpenGL server process to interpret an actionSpec received/found in a repo.
 *
 * An HTTP-client would not normally use this class directly.  
 * Instead, see the client side classes in the org.cogchar.api.vworld.
 * 
 * 
 * Soon this should be refactored as a subclass of the new generalized superclass, EntityAction.
 * 
 * All the accessor methods of this class can return null in the case that no data is found.  
 * 
 * @author Stu B. <www.texpedient.com> and Ryan B.
 */

public class GoodyActionExtractor extends GoodyActionParamReader {
	// We can optionallly play a game of equivalence between Java-enum-constant and URI, without an additional hashMap.
	// The price is that we must initialize the value in the enum constants.
	public enum Kind {
		CREATE(GoodyNames.ACTION_CREATE.getAbsUriString()),
		DELETE(GoodyNames.ACTION_DELETE.getAbsUriString()),
		MOVE(GoodyNames.ACTION_MOVE.getAbsUriString()),
		SET(GoodyNames.ACTION_SET.getAbsUriString());
		
		public	String myKindUriString;
		
		private Kind(String uriString) {
			myKindUriString = uriString;
		}
	}
	private		Kind					myKind;
	
	
	private		ThingActionSpec			mySpec;

	private		Ident					myGoodyID;
	private		Ident					myGoodyTypeID;
	
//	private		TypedValueMap			paramTVMap;
	
	public GoodyActionExtractor(ThingActionSpec actionSpec) {
		super(actionSpec.getParamTVM());
		mySpec = actionSpec;
		myGoodyID = actionSpec.getTargetThingID();
		myGoodyTypeID = actionSpec.getTargetThingTypeID();
		
//		paramTVMap = mySpec.getParamTVM();
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
	 * However, during creation or other "special" operation, our GoodyActionExtractor action may 
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
	private Vector3f makeVec3f(Float floatArr[]) {
		Vector3f resultVec = null;
		if (floatArr != null) {
			if ((floatArr[0] != null) && (floatArr[1] != null) && (floatArr[2] != null)) {
				// TODO: more null checks.
				resultVec = new Vector3f(floatArr[0], floatArr[1], floatArr[2]);	
			}
		}
		return resultVec;
	}
	/**
	 * Example of actual application data read from spec, into an application specific type.
	 * Will be generalized to use for "goal location", "direction", etc.
	 * Returns null if any of the components in the TVMap are unspecified
	 * @return 
	 */
	public Vector3f getLocationVec3f() {
		return makeVec3f(getLocationVec3D());
	}
	public Vector3f getScaleVec3f() {
		return makeVec3f(getScaleVec3D());
	}
	public Vector3f getSizeVec3f() {
		return makeVec3f(getSizeVec3D());
	}
	
	/**
	 * Here is a harder one.
	 * Read rotation axis AxisX, AxisY, AxisZ and magDegrees from some assumed properties,
	 * and produce a Quaternion.   Will also be generalized later.
	 * Returns null if any of the components in the TVMap are unspecified
	 * @return rotational operator quaternion object encoding
	 */
	public Quaternion getRotationQuaternion() {
		Quaternion resultQuat = null;
		Vector3f angleAxisVec3f = makeVec3f(getRotAxisVec3D());
		Float magRad = getRotMagRadians();
		if ((angleAxisVec3f != null) && (magRad != null)) {
			resultQuat = new Quaternion().fromAngleAxis(magRad, angleAxisVec3f);	
		}
		return resultQuat;
	}
	

	
	public ColorRGBA getColor() {
		Float cVals[] = getColorVec4D();
        for(Float f : cVals){
            if(f == null){
                return new ColorRGBA(0.5f, 0.5f, 0.5f, 1.0f);
            }
        }
		ColorRGBA resultColor =  new ColorRGBA(cVals[0], cVals[1], cVals[2], cVals[3]);
		return resultColor;
	}
	

}
