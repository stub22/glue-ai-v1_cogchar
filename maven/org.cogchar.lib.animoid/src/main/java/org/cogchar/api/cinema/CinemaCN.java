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
	
	public final static String CINEMATICS_QUERY_URI = "ccrt:find_cinematics_99";
	public final static String TRACK_QUERY_URI = "ccrt:find_tracks_99";
	public final static String WAYPOINT_QUERY_URI = "ccrt:find_waypoints_99";
	public final static String ROTATION_QUERY_URI = "ccrt:find_rotations_99";
	
	public final static String TRACKS_QUERY_TEMPLATE_URI = "ccrt:template_track_99";
	public final static String WAYPOINTS_QUERY_TEMPLATE_URI = "ccrt:template_waypoint_99";
	public final static String ROTATIONS_QUERY_TEMPLATE_URI = "ccrt:template_rotation_99";
	
	public final static String CINEMATIC_VAR_NAME = "cinematic";
	public final static String DURATION_VAR_NAME = "duration";
	public final static String TRACK_VAR_NAME = "track";
	public final static String ATTACHED_ITEM_VAR_NAME = "attachedItem";
	public final static String ATTACHED_ITEM_TYPE_VAR_NAME = "attachedItemType";
	public final static String TRACK_TYPE_VAR_NAME = "trackType";
	public final static String DIRECTION_TYPE_VAR_NAME = "directionType";
	public final static String[] DIRECTION_VAR_NAME = {"xDir", "yDir", "zDir"};
	public final static String TENSION_VAR_NAME = "tension";
	public final static String CYCLE_VAR_NAME = "cycle";
	public final static String LOOP_MODE_VAR_NAME = "loop";
	public final static String START_TIME_VAR_NAME = "startTime";
	public final static String WAYPOINT_VAR_NAME = "waypoint";
	public final static String END_ROTATION_VAR_NAME = "endRotation";
	public final static String[] POSITION_VAR_NAME = {"xPos", "yPos", "zPos"};
	public final static String ROTATION_VAR_NAME = "rotation";
	public final static String YAW_VAR_NAME = "yaw";
	public final static String PITCH_VAR_NAME = "pitch";
	public final static String ROLL_VAR_NAME = "roll";
	
	public final static String CINEMATIC_QUERY_VAR_NAME = "cinematicInstance";
	public final static String TRACK_QUERY_VAR_NAME = "track";
}
