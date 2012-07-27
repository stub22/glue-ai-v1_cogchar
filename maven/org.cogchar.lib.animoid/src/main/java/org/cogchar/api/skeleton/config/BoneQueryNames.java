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
package org.cogchar.api.skeleton.config;

/**
 * @author Ryan Biggs
 */

public class BoneQueryNames {
	
	public static final String ROBOT_URI_VAR_NAME = "humInd";
	public static final String ROBOT_NAME_VAR_NAME = "robotName";
	public static final String BONE_JOINT_CONFIG_INSTANCE_VAR_NAME = "jointConfigInstance";
	public static final String JOINT_URI_VAR_NAME = "joint";
	public static final String JOINT_NUM_VAR_NAME = "jointNum";
	public static final String JOINT_NAME_VAR_NAME = "jointName";
	public static final String DEFAULT_POS_VAR_NAME = "defaultPos";
	public static final String BONE_NAME_VAR_NAME = "boneName";
	public static final String ROTATION_AXIS_VAR_NAME = "rotationAxis";
	public static final String MIN_ANGLE_VAR_NAME = "minAngle";
	public static final String MAX_ANGLE_VAR_NAME = "maxAngle";
	
	public static final String ROBOT_NAME_QUERY_URI = "ccrt:find_robotName_99";
	
	public static final String BONE_JOINT_CONFIG_QUERY_TEMPLATE_URI = "ccrt:template_boneJointConfigs_99";
	public static final String BASE_BONE_JOINT_PROPERTIES_QUERY_TEMPLATE_URI = "ccrt:template_basicJointProperties_99";
	public static final String BONEPROJECTION_QUERY_TEMPLATE_URI = "ccrt:template_boneProjectionRanges_99";
	public static final String BONE_NAMES_QUERY_TEMPLATE_URI = "ccrt:template_boneNames_99";
	
	public static final String BONE_JOINT_CONFIG_QUERY_VAR = "boneJointUri";
	public static final String ROBOT_IDENT_QUERY_VAR = "robotUri";
		
}
