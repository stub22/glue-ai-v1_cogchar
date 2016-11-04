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
package org.cogchar.name.skeleton;

import org.appdapter.api.trigger.BoxAssemblyNames;
import org.cogchar.name.dir.NamespaceDir;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class BoneAN extends BoxAssemblyNames {
	public static	String		NS_CgcBC		= NamespaceDir.NS_CgcBC; // "http://www.cogchar.org /bony/config#";
	
	public static 	String		P_robotName			= NS_CgcBC + "robotName";
	
	public static 	String		P_joint				= NS_CgcBC + "joint";	
	
	public static 	String		P_jointNum			= NS_CgcBC + "jointNum";
	public static 	String		P_jointName			= NS_CgcBC + "jointName";	
	public static 	String		P_defaultPosNorm	= NS_CgcBC + "defaultPosNorm";
	
	public static 	String		P_projectionRange	= NS_CgcBC + "projectionRange";
	
	public static 	String		P_boneName			= NS_CgcBC + "boneName";
	public static 	String		P_rotationAxis		= NS_CgcBC + "rotationAxis";
	public static 	String		P_minAngleDeg		= NS_CgcBC + "minAngleDeg";
	public static 	String		P_maxAngleDeg		= NS_CgcBC + "maxAngleDeg";
	
}
