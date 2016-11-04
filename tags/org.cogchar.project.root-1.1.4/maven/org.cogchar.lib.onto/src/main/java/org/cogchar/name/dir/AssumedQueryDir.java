/*
 *  Copyright 2013 by The Cogchar Project (www.cogchar.org).
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

package org.cogchar.name.dir;

/**
 * @author Stu B. <www.texpedient.com>
 * 
 * Collects assumed query URIs (names for queries stored in a repo).
 */

public class AssumedQueryDir {
	
	// ThingAct queries
	public  static String	ACTION_QUERY_URI = "ccrt:find_thing_actions_99";
	public  static String	UNSEEN_ACTION_QUERY_URI = "ccrt:find_unseen_thing_actions_99";
	public  static String	PARAM_QUERY_URI = "ccrt:find_thing_action_params_99";
	
	// Lifter Queries 
	public static final String TEMPLATE_QUERY_URI = "ccrt:find_lift_templates_99";
	public static final String START_CONFIG_QUERY_URI = "ccrt:find_lift_startConfig_99";	
	public static final String CONTROL_QUERY_TEMPLATE_URI = "ccrt:template_lift_control_99";
	public static final String FREE_CONTROL_QUERY_TEMPLATE_URI = "ccrt:template_find_free_control_99";
	public static final String FREE_CONTROL_ACTION_QUERY_TEMPLATE_URI = "ccrt:template_find_free_control_action_99";
	public static final String GENRAL_CONFIG_TEMPLATE_URI = "ccrt:template_general_items_99";
	public static final String LOGIN_PAGE_QUERY_URI = "ccrt:find_login_page_99";
	public static final String USER_QUERY_URI = "ccrt:find_users_99";
	
	//  Entity-Role ("GlobalMode")
	public static final String GLOBALMODE_QUERY_QN = "ccrt:template_globalmode_99" ;
	public static final String ENTITIES_QUERY_QN = "ccrt:template_global_entities_99";

	// V-World Lights/Camera queries
	public final static String CAMERA_QUERY_URI = "ccrt:find_cameras_99";
	public final static String LIGHT_QUERY_URI = "ccrt:find_lights_99";
	public final static String BACKGROUND_COLOR_QUERY_URI = "ccrt:find_background_color_99";
	
	// V-World "Cinema" + path queries
	
	public final static String PATHS_QUERY_URI = "ccrt:find_paths_99";
	public final static String TRACK_QUERY_URI = "ccrt:find_tracks_99";
	public final static String WAYPOINT_QUERY_URI = "ccrt:find_waypoints_99";
	public final static String ROTATION_QUERY_URI = "ccrt:find_rotations_99";
	public final static String THING_ANIM_QUERY_URI = "ccrt:find_thing_anims_99";
	
	public final static String WAYPOINTS_QUERY_TEMPLATE_URI = "ccrt:template_waypoint_99";
	public final static String ROTATIONS_QUERY_TEMPLATE_URI = "ccrt:template_rotation_99";
	public final static String VECTOR_SCALINGS_QUERY_URI = "ccrt:find_vector_scalings_99";
	public final static String KEYFRAMES_QUERY_TEMPLATE_URI = "ccrt:template_key_frames_99";	
	
	// AnimConv - Maya queries
	
	public final static String MAYA_CHANNEL_QUERY_URI = "ccrt:find_maya_channel_mappings_99";
	public final static String MAYA_MAP_QUERY_URI = "ccrt:find_maya_channel_maps_99";
}
