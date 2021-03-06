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
package org.cogchar.name.cinema;

import org.appdapter.api.trigger.BoxAssemblyNames;
import org.cogchar.name.dir.NamespaceDir;

/**
 * @author Ryan Biggs
 */
public class LightsCameraAN extends BoxAssemblyNames {
	private static	String		NS_CgcBC		= NamespaceDir.NS_CgcBC; // "http://www.cogchar.org  /bony/config#";
	
	private static	String		partial_P_camera = "camera";
	
	// public static 	String		P_camera		= NS_CgcBC + partial_P_camera;
	
        public static   String[]        P_position              = {NS_CgcBC + "xPos", NS_CgcBC + "yPos", NS_CgcBC + "zPos"};
        
        public static   String[]        P_direction             = {NS_CgcBC + "xDir", NS_CgcBC + "yDir", NS_CgcBC + "zDir"};
        
        public static   String[]        P_viewport              = {NS_CgcBC + "viewportXstart", NS_CgcBC + "viewportXend", NS_CgcBC + "viewportYstart", NS_CgcBC + "viewportYend"};
		
		public static	String			P_attachedRobot			= NS_CgcBC + "attachedToRobot";
		
		public static	String			P_attachedItem			= NS_CgcBC + "attachedToBone";
        
        public static 	String			P_light					= NS_CgcBC + "light";
        
        public static   String          P_lightName             = NS_CgcBC + "lightName";
        
        public static   String          P_lightType             = NS_CgcBC + "type";
	
        public static   String[]        P_color                 = {NS_CgcBC + "colorR", NS_CgcBC + "colorG", NS_CgcBC + "colorB", NS_CgcBC + "colorAlpha"};
		
		// public static	String			suffix_DEFAULT			= "default";
		public static	String			suffix_DEFAULT			= "DEFAULT";
		
		// public static	String			suffix_HEAD_CAM			= "head_cam";
		public static	String			suffix_HEAD_CAM			= "HEAD_CAM";
		
		// This is an individual, not a property
		public static	String			URI_defaultCam = NamespaceDir.CCRT_NS + partial_P_camera + "_" + suffix_DEFAULT;
		// public static	String			URI_headCam = P_camera + "_" + suffix_HEAD_CAM;
        
	
}
