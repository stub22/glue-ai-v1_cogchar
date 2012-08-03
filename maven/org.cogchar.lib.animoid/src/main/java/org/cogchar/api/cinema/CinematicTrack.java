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
package org.cogchar.api.cinema;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.appdapter.core.item.*;
import org.appdapter.bind.rdf.jena.assembly.ItemAssemblyReader;
import org.appdapter.bind.rdf.jena.assembly.ItemAssemblyReaderImpl;
import org.appdapter.core.log.BasicDebugger;
import org.cogchar.blob.emit.Solution;
import org.cogchar.blob.emit.SolutionList;
import org.cogchar.blob.emit.QueryInterface;
import org.cogchar.blob.emit.QuerySheet;

/**
 *
 * @author Ryan Biggs
 */
public class CinematicTrack extends BasicDebugger {

	public String trackName;
	public String attachedItem;
	public AttachedItemType attachedItemType = AttachedItemType.NULLTYPE;
	public TrackType trackType = TrackType.NULLTYPE;
	public String directionType;
	public float[] direction = new float[3]; // For LookAt direction initally, but may be useful for other things
	public float tension;
	public boolean cycle;
	public String loopMode;
	public float startTime;
	public float trackDuration;
	public List<WaypointConfig> waypoints = new ArrayList<WaypointConfig>();
	public RotationConfig endRotation;
	private static final ItemAssemblyReader reader = new ItemAssemblyReaderImpl();
	
	private static QueryInterface queryEmitter = QuerySheet.getInterface();

	@Override
	public String toString() {
		return "CinematicTrack = " + trackName + ", type = " + trackType.name() + ", Attached Item = " + attachedItem + "]";
	}

	// This constructor is called from within CinematicTrackInstance to correspond to Turtle configured usages of "named" tracks within CinematicTrackInstances
	// The need for this results from the flexibility of initial Turtle definition: tracks could be defined inline or as separate
	// Named entities. (Same for waypoints and rotations.) We're moving away from this with the spreadsheet config, and can
	// simplify / clean up things if we decide we're permanently doing away with the inline definitions
	public CinematicTrack(Ident trackIdent) {
		trackName = trackIdent.getLocalName();
	}

	// Called from CinematicConfig, corresponds to a "named" track definition
	public CinematicTrack(Solution solution) {
		Ident myIdent = queryEmitter.getIdentFromSolution(solution, CinematicQueryNames.TRACK_VAR_NAME);
		trackName = myIdent.getLocalName();
		attachedItem = queryEmitter.getIdentFromSolution(solution, CinematicQueryNames.ATTACHED_ITEM_VAR_NAME).getLocalName();
		String typeString = queryEmitter.getIdentFromSolution(solution, CinematicQueryNames.ATTACHED_ITEM_TYPE_VAR_NAME).getLocalName().toUpperCase();
		for (AttachedItemType testType : AttachedItemType.values()) {
			if (testType.toString().equals(typeString)) {
				attachedItemType = testType;
			}
		}
		typeString = queryEmitter.getIdentFromSolution(solution, CinematicQueryNames.TRACK_TYPE_VAR_NAME).getLocalName();
		for (TrackType testType : TrackType.values()) {
			if (testType.toString().equals(typeString)) {
				trackType = testType;
			}
		}
		directionType = queryEmitter.getIdentFromSolution(solution, CinematicQueryNames.DIRECTION_TYPE_VAR_NAME).getLocalName();
		for (int index = 0; index < direction.length; index++) {
			direction[index] = queryEmitter.getFloatFromSolution(solution, CinematicQueryNames.DIRECTION_VAR_NAME[index], 0f);
		}
		tension = queryEmitter.getFloatFromSolution(solution, CinematicQueryNames.TENSION_VAR_NAME, 0f);
		cycle = queryEmitter.getBooleanFromSolution(solution, CinematicQueryNames.CYCLE_VAR_NAME);
		loopMode = queryEmitter.getIdentFromSolution(solution, CinematicQueryNames.LOOP_MODE_VAR_NAME).getLocalName();
		startTime = queryEmitter.getFloatFromSolution(solution, CinematicQueryNames.START_TIME_VAR_NAME, 0f);
		trackDuration = queryEmitter.getFloatFromSolution(solution, CinematicQueryNames.DURATION_VAR_NAME, 0f);
		String query = queryEmitter.getCompletedQueryFromTemplate(CinematicQueryNames.WAYPOINTS_QUERY_TEMPLATE_URI, CinematicQueryNames.TRACK_QUERY_VAR_NAME, myIdent);
		SolutionList solutionList = queryEmitter.getTextQueryResultList(query);
		List<Ident> waypointIdentList = queryEmitter.getIdentsFromSolutionAsJava(solutionList, CinematicQueryNames.WAYPOINT_VAR_NAME);
		for (Ident waypointIdent : waypointIdentList) {
			waypoints.add(new WaypointConfig(waypointIdent));
		}
		Ident rotationIdent = queryEmitter.getIdentFromSolution(solution, CinematicQueryNames.END_ROTATION_VAR_NAME);
		if (rotationIdent != null) {
			endRotation = new RotationConfig(rotationIdent);
		}
	}

	public CinematicTrack(Item configItem) {
		// If this track has no name, it's likely an unnamed track defined in-line with a cinematic definition...
		trackName = ItemFuncs.getString(configItem, CinematicConfigNames.P_trackName, CinematicConfigNames.unnamedTrackName);
		String trackLocalName = configItem.getIdent().getLocalName();
		// ... or a track with no name may be from a track resource not defined as part of a cinematic
		if (trackLocalName == null) {
			trackLocalName = "no dice"; // Keeps expression below from throwing an NPE if trackLocalName is null, which it is if track is defined within cinematic definition
		}
		if (trackLocalName.startsWith(CinematicConfigNames.P_namedTrack)) {
			//trackName = trackLocalName.replaceFirst(CinematicConfigNames.P_namedTrack, ""); // Strip the prefix and set trackName to this
			trackName = trackLocalName; // Actually may be best to just leave the prefix, then we reference this named track in cinematics with the prefix for clarity
		}
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
		trackDuration = ItemFuncs.getDouble(configItem, CinematicConfigNames.P_trackDuration, 0.0).floatValue();
		// Get waypoints
		List<Item> waypointItems = reader.readLinkedItemSeq(configItem, CinematicConfigNames.P_waypoint);
		for (Item wpt : waypointItems) {
			WaypointConfig oneWaypoint = new WaypointConfig(wpt);
			waypoints.add(oneWaypoint);
		}
		// Get rotation - there should only be one
		Set<Item> rotationItems = ItemFuncs.getLinkedItemSet(configItem, CinematicConfigNames.P_rotation);
		if (rotationItems.size() > 1) {
			logWarning("More than one endRotation detected in track " + trackName + "; ignoring all but one!");
		}
		for (Item rotation : rotationItems) {
			endRotation = new RotationConfig(rotation);
		}
	}

	public enum AttachedItemType {

		NULLTYPE, CAMERA
	} // Initially only supporting cameras, but most of structure is in place to add others

	public enum TrackType {

		NULLTYPE, MOTIONTRACK, POSITIONTRACK, ROTATIONTRACK
	}
}
