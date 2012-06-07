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

import java.util.ArrayList;
import java.util.List;
import org.appdapter.core.item.*;
import org.appdapter.bind.rdf.jena.assembly.ItemAssemblyReader;
import org.appdapter.bind.rdf.jena.assembly.ItemAssemblyReaderImpl;

/**
 *
 * @author Ryan Biggs
 */
public class CinematicTrack {

	public String attachedItem;
	public AttachedItemType attachedItemType = AttachedItemType.NULLTYPE;
	public TrackType trackType = TrackType.NULLTYPE;
	public String directionType;
	public float[] direction = new float[3]; // For LookAt direction initally, but may be useful for other things
	public float tension;
	public boolean cycle;
	public String loopMode;
	public float startTime;
	public List<float[]> waypoints = new ArrayList<float[]>();
	private static final ItemAssemblyReader reader = new ItemAssemblyReaderImpl();

	public CinematicTrack(Item configItem) {
		attachedItem = ItemFuncs.getString(configItem, CinematicConfigNames.P_item, "none");
		String typeString;
		typeString = ItemFuncs.getString(configItem, CinematicConfigNames.P_itemType, null);
		for (AttachedItemType testType : AttachedItemType.values()) {
			if (testType.toString().equals(typeString)) {
				attachedItemType = testType;
			}
		}
		typeString = ItemFuncs.getString(configItem, CinematicConfigNames.P_trackType, null);
		for (TrackType testType : TrackType.values()) {
			if (testType.toString().equals(typeString)) {
				trackType = testType;
			}
		}
		directionType = ItemFuncs.getString(configItem, CinematicConfigNames.P_directionType, null);
		for (int index = 0; index < direction.length; index++) {
			direction[index] = ItemFuncs.getDouble(configItem, CinematicConfigNames.P_direction[index], 0.0).floatValue();
		}
		tension = ItemFuncs.getDouble(configItem, CinematicConfigNames.P_tension, 0.0).floatValue();
		typeString = ItemFuncs.getString(configItem, CinematicConfigNames.P_cycle, "false");
		if (typeString.equals("false")) {
			cycle = false;
		} else {
			cycle = true;
		}
		loopMode = ItemFuncs.getString(configItem, CinematicConfigNames.P_loop, null);
		startTime = ItemFuncs.getDouble(configItem, CinematicConfigNames.P_startTime, 0.0).floatValue();
		// Get waypoints
		List<Item> waypointItems = reader.readLinkedItemSeq(configItem, CinematicConfigNames.P_waypoint);
		for (Item wpt : waypointItems) {
			float[] oneWaypoint = new float[3];
			for (int index = 0; index < oneWaypoint.length; index++) {
				oneWaypoint[index] = ItemFuncs.getDouble(wpt, CinematicConfigNames.P_position[index], 0.0).floatValue();
			}
			waypoints.add(oneWaypoint);
		}
	}

	public enum AttachedItemType {

		NULLTYPE, CAMERA
	} // Initially only supporting cameras, but most of structure is in place to add others

	public enum TrackType {

		NULLTYPE, MOTIONTRACK
	} // Initially only supporting motion tracks
}
