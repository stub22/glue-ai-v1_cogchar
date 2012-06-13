/*
 * Copyright 2011 The Cogchar Project (www.cogchar.org).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.cogchar.api.scene;

import org.appdapter.core.item.*;

/**
 *
 * @author Ryan Biggs
 */
public class RotationConfig {

	public String rotationName;
	public float yaw;
	public float roll;
	public float pitch;

	@Override
	public String toString() {
		return "RotationConfig = " + rotationName + ", yaw = " + yaw + ", roll = " + roll + ", pitch = " + pitch;
	}

	public RotationConfig(Item configItem) {
		// If this rotation has no name, it's likely an unnamed rotation defined in-line with a track definition...
		rotationName = ItemFuncs.getString(configItem, CinematicConfigNames.P_rotationName, CinematicConfigNames.unnamedRotationName);
		String rotationLocalName = configItem.getIdent().getLocalName();
		// ... or a rotation with no name may be from a rotation resource not defined as part of a track
		if (rotationLocalName == null) {
			rotationLocalName = "no dice"; // Keeps expression below from throwing an NPE if rotationLocalName is null, which it is if rotation is defined within track definition
		}
		if (rotationLocalName.startsWith(CinematicConfigNames.P_namedRotation)) {
			rotationName = rotationLocalName;
		}
		yaw = ItemFuncs.getDouble(configItem, CinematicConfigNames.P_yaw, Double.NaN).floatValue();
		roll = ItemFuncs.getDouble(configItem, CinematicConfigNames.P_roll, Double.NaN).floatValue();
		pitch = ItemFuncs.getDouble(configItem, CinematicConfigNames.P_pitch, Double.NaN).floatValue();

	}
}
