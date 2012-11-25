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
import java.util.HashMap;
import java.util.Map;
import org.appdapter.core.name.Ident;
import org.cogchar.api.thing.TypedValueMap;
import org.cogchar.api.thing.ThingActionSpec;

/**
 * Typically used in the OpenGL server process to interpret an actionSpec found in a repo.
 * An HTTP-client would not normally use this class.
 * 
 * @author Stu B. <www.texpedient.com>
 */

public class GoodyAction  {
	/* We can optionallly play a game of equivalence between Java-enum-constant and URI, without an additional hashMap.
	// The price is that we must initialize the value in the enum constants.
	public enum Kind {
		CREATE,
		DELETE,
		MOVE,
		SET;
		
		public	String myKindUriString;
	
	}
	private		Kind					myKind;
	* 
	*/
	
	private		ThingActionSpec			mySpec;

	private		Ident					myGoodyID;
	
	public GoodyAction(ThingActionSpec actionSpec) {
		mySpec = actionSpec;
		myGoodyID = actionSpec.getTargetThingID();
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
	/**
	 * Example of actual application data read from spec, into an application specific type.
	 * Will be generalized to use for "goal location", "direction", etc.
	 * @return 
	 */
	public Vector3f getLocationVector() {
		TypedValueMap paramTVMap = mySpec.getParamTVM();
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
	 * and produce a Quartenion.   Will also be generalized later.
	 * @return rotational operator quaternion object encoding
	 */
	public Quaternion getRotationQuaternion() {
		// 
		return null;
	}


}
