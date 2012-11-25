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

import org.appdapter.core.name.FreeIdent;
import org.appdapter.core.name.Ident;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class GoodyNames {
	
	public  static String	GOODY_NS = "urn:ftd:cogchar.org:2012:goody#";
	
	public static Ident makeID (String nameTail) {
		return new FreeIdent(GOODY_NS + nameTail);
	}
	public	static Ident 	LOCATION_X = makeID("locX");
	public	static Ident	LOCATION_Y = makeID("locY");
	public	static Ident	LOCATION_Z = makeID("locZ");
	
	public	static Ident	ROTATION_AXIS_X = makeID("rotAxisX");
	public	static Ident	ROTATION_AXIS_Y = makeID("rotAxisY");
	public	static Ident	ROTATION_AXIS_Z = makeID("rotAxisZ");
	public	static Ident	ROTATION_MAG_DEG = makeID("rotMagDeg");

}
