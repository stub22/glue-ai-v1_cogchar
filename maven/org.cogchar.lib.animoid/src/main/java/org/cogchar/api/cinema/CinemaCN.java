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

public class CinemaCN {
	public final static String CCRT = "urn:ftd:cogchar.org:2012:runtime#";
	
	public final static String PATHS_QUERY_URI = "ccrt:find_paths_99";
	public final static String TRACK_QUERY_URI = "ccrt:find_tracks_99";
	public final static String WAYPOINT_QUERY_URI = "ccrt:find_waypoints_99";
	public final static String ROTATION_QUERY_URI = "ccrt:find_rotations_99";
	public final static String THING_ANIM_QUERY_URI = "ccrt:find_thing_anims_99";
	
	public final static String WAYPOINTS_QUERY_TEMPLATE_URI = "ccrt:template_waypoint_99";
	public final static String ROTATIONS_QUERY_TEMPLATE_URI = "ccrt:template_rotation_99";
	public final static String KEYFRAMES_QUERY_TEMPLATE_URI = "ccrt:template_key_frames_99";
	
	public final static String PATH_VAR_NAME = "path";
	public final static String ANIM_VAR_NAME = "anim";
	public final static String DURATION_VAR_NAME = "duration";
	public final static String ATTACHED_ITEM_VAR_NAME = "attachedItem";
	public final static String ATTACHED_ITEM_TYPE_VAR_NAME = "attachedItemType";
	public final static String DIRECTION_TYPE_VAR_NAME = "directionType";
	public final static String[] DIRECTION_VAR_NAME = {"xDir", "yDir", "zDir"};
	public final static String TENSION_VAR_NAME = "tension";
	public final static String CYCLE_VAR_NAME = "cycle";
	public final static String LOOP_MODE_VAR_NAME = "loop";
	public final static String WAYPOINT_VAR_NAME = "waypoint";
	public final static String SEQUENCE_NUMBER_VAR_NAME = "order";
	public final static String[] POSITION_VAR_NAME = {"xPos", "yPos", "zPos"};
	public final static String ROTATION_VAR_NAME = "rotation";
	public final static String ROT_X_VAR_NAME = "rotX";
	public final static String ROT_Y_VAR_NAME = "rotY";
	public final static String ROT_Z_VAR_NAME = "rotZ";
	public final static String ROT_MAG_VAR_NAME = "rotMag";
	public final static String TIME_VAR_NAME = "time";
	public final static String LOCATION_CONFIG_VAR_NAME = "locationRef";
	public final static String ORIENTATION_CONFIG_VAR_NAME = "orientationRef";
	public final static String SCALE_VAR_NAME = "scale";
	
	public final static String PATH_INSTANCE_QUERY_VAR_NAME = "path";
	public final static String ANIM_INSTANCE_QUERY_VAR_NAME = "anim";
}
