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

package org.cogchar.api.humanoid;

/**
 *
 * @author Ryan Biggs <rbiggs@skyriversoftware.com>
 */


public class HumanoidQueryNames {
	final static String QUERY_SHEET = "ccrt:qry_sheet_22";
	final static String HUMANOID_QUERY = "ccrt:find_humanoids_99";

	final static String ROBOT_URI_VAR_NAME = "humInd";
	final static String ROBOT_ID_VAR_NAME = "rkRobotID";
	final static String MESH_PATH_VAR_NAME = "meshPath";
	final static String BONY_CONFIG_PATH_VAR_NAME = "bonyConfigPath";
	final static String JOINT_CONFIG_PATH_VAR_NAME = "jointConfigPath";
	final static String[] INITIAL_POSITION_VAR_NAMES = {"initX", "initY", "initZ"};
	final static String PHYSICS_FLAG_VAR_NAME = "physics";
}
