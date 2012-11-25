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
 * @author Stu B. <www.texpedient.com>
 */

public class GoodyAction  {
	public enum Kind {
		CREATE,
		DELETE,
		MOVE,
		SET;
		
		public	String myKindUriString;
	}
	
	private		ThingActionSpec			mySpec;
	private		Kind					myKind;
	private		Ident					myGoodyID;

	
	public GoodyAction(ThingActionSpec actionSpec) {
		mySpec = actionSpec;
		myGoodyID = actionSpec.getTargetThingID();
	}
	public Ident getGoodyID() {
		return myGoodyID;
	}
	public Vector3f getLocationVector() {
		TypedValueMap paramTVMap = mySpec.getParamTVM();
		float locX = paramTVMap.getAsFloat(GoodyNames.LOCATION_X);
		float locY = paramTVMap.getAsFloat(GoodyNames.LOCATION_Y);
		float locZ = paramTVMap.getAsFloat(GoodyNames.LOCATION_Z);
		Vector3f resultVec = new Vector3f(locX, locY, locZ);
		// Read LocX, LocY, LocZ from some assumed properties.
		return resultVec;
	}
	public Quaternion getRotationQuaternion() {
		// Read rotation axis AxisX, AxisY, AxisZ and magDegrees from some assumed properties.
		return null;
	}

}
