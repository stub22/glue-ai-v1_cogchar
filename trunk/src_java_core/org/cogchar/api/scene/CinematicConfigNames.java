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
package org.cogchar.api.scene;

import org.appdapter.api.trigger.BoxAssemblyNames;

/**
 * @author Ryan Biggs
 */
public class CinematicConfigNames extends BoxAssemblyNames {
	public static	String		NS_CgcCC		= "http://www.cogchar.org/schema/cinematic#";
	
	public static	String		P_namedTrack	= "track_";
	
	public static	String		P_namedWaypoint	= "waypoint_";
	
	public static	String		P_namedRotation	= "rotation_";
	
	public static	String		suffix_unnamed	= "Unnamed";
	
	public static	String		unnamedTrackName = P_namedTrack + suffix_unnamed;
	
	public static	String		unnamedWaypointName = P_namedWaypoint + suffix_unnamed;
	
	public static	String		unnamedRotationName = P_namedRotation + suffix_unnamed;
	
	public static 	String		P_cinematic		= NS_CgcCC + "cinematicList";
	
	public static 	String		P_trackList		= NS_CgcCC + "trackList";
	
	public static 	String		P_waypointList	= NS_CgcCC + "waypointList";
	
	public static 	String		P_rotationList	= NS_CgcCC + "rotationList";
        
        public static   String          P_duration				= NS_CgcCC + "duration";
		
		public static   String          P_track		            = NS_CgcCC + "tracks";
		
		public static   String          P_trackName		        = NS_CgcCC + "trackName";
		
		public static   String          P_item		            = NS_CgcCC + "attachedItem";
		
		public static   String          P_itemType				= NS_CgcCC + "attachedItemType";
		
		public static   String          P_trackType             = NS_CgcCC + "trackType";
		
		public static   String          P_directionType         = NS_CgcCC + "directionType";
	
        public static   String[]        P_position              = {NS_CgcCC + "xPos", NS_CgcCC + "yPos", NS_CgcCC + "zPos"};
        
        public static   String[]        P_direction             = {NS_CgcCC + "xDir", NS_CgcCC + "yDir", NS_CgcCC + "zDir"};
		
		public static   String          P_tension				= NS_CgcCC + "tension";
		
		public static   String          P_cycle					= NS_CgcCC + "cycle";
		
		public static   String          P_loop					= NS_CgcCC + "loop";
		
		public static	String			P_startTime				= NS_CgcCC + "startTime";
		
		public static	String			P_trackDuration			= NS_CgcCC + "trackDuration";
		
		public static	String			P_yaw					= NS_CgcCC + "yaw";
		
		public static	String			P_roll					= NS_CgcCC + "roll";
		
		public static	String			P_pitch					= NS_CgcCC + "pitch";
		
		public static   String          P_waypoint				= NS_CgcCC + "waypoints";
		
		public static   String          P_waypointName			= NS_CgcCC + "waypointName";
		
		public static   String          P_rotation				= NS_CgcCC + "endRotation";
		
		public static   String          P_rotationName			= NS_CgcCC + "rotationName";
  	
}
