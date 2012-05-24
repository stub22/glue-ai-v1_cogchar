/*
 *  Copyright 2011 by The Cogchar Project (www.cogchar.org).
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
package org.cogchar.render.sys.core;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class JmonkeyMathObjFactory {
	public static Vector3f makeVector(float xyz[]) {
		if (xyz.length != 3) {
			throw new RuntimeException("Cannot make 3F vector from vector of length " + xyz.length);
		}
		return new Vector3f(xyz[0], xyz[1], xyz[2]);
	}
}
