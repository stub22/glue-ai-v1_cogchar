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

package org.cogchar.api.cinema;

/**
 *
 * @author Ryan Biggs
 */


public class LightsCameraQueryNames {
	public final static String CAMERA_QUERY_URI = "ccrt:find_cameras_99";
	public final static String LIGHT_QUERY_URI = "ccrt:find_lights_99";
	
	public final static String CAMERA_NAME_VAR_NAME = "camera";
	public final static String[] POSITION_VAR_NAME = {"xPos", "yPos", "zPos"};
	public final static String[] DIRECTION_VAR_NAME = {"xDir", "yDir", "zDir"};
	public final static String[] VIEWPORT_VAR_NAME = {"viewportXstart", "viewportXend", "viewportYstart", "viewportYend"};
	public final static String ATTACHED_ROBOT_VAR_NAME = "robot";
	public final static String ATTACHED_BONE_VAR_NAME = "bone";
	public final static String LIGHT_NAME_VAR_NAME = "light";
	public final static String LIGHT_TYPE_VAR_NAME = "lightType";
	public final static String[] COLOR_VAR_NAME = {"colorR", "colorG", "colorB", "colorAlpha"};
}
