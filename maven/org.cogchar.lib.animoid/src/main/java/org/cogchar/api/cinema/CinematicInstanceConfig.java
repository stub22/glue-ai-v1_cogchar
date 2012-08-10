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

import java.util.ArrayList;
import java.util.List;
import org.appdapter.core.item.Item;
import org.appdapter.core.item.ItemFuncs;
import org.appdapter.bind.rdf.jena.assembly.ItemAssemblyReader;
import org.appdapter.bind.rdf.jena.assembly.ItemAssemblyReaderImpl;
import org.appdapter.core.item.Ident;
import org.cogchar.blob.emit.Solution;
import org.cogchar.blob.emit.SolutionList;
import org.cogchar.blob.emit.QueryInterface;
import org.cogchar.blob.emit.QuerySheet;

import org.appdapter.core.log.BasicDebugger;

/**
 * @author Ryan Biggs
 */
public class CinematicInstanceConfig extends BasicDebugger {

	public String myURI_Fragment;
	public float duration;
	public List<CinematicTrack> myTracks = new ArrayList<CinematicTrack>();
	private static final ItemAssemblyReader reader = new ItemAssemblyReaderImpl();
	
	private static QueryInterface queryEmitter = QuerySheet.getInterface();

	@Override
	public String toString() {
		return "CinematicInstanceConfig[uriFrag = " + myURI_Fragment + ", duration = " + Float.toString(duration) + ", Number of tracks = " + Integer.toString(myTracks.size());
	}

	// A new constructor to build CinematicConfig from spreadsheet
	public CinematicInstanceConfig(Solution querySolution, Ident qGraph) {
		Ident myIdent = queryEmitter.getIdentFromSolution(querySolution, CinematicQueryNames.CINEMATIC_VAR_NAME);
		myURI_Fragment = myIdent.getLocalName();
		duration = queryEmitter.getFloatFromSolution(querySolution, CinematicQueryNames.DURATION_VAR_NAME, Float.NaN);
		String query = queryEmitter.getCompletedQueryFromTemplate(CinematicQueryNames.TRACKS_QUERY_TEMPLATE_URI, CinematicQueryNames.CINEMATIC_QUERY_VAR_NAME, myIdent);
		SolutionList solutionList = queryEmitter.getTextQueryResultList(query, qGraph);
		List<Ident> trackIdentList = queryEmitter.getIdentsFromSolutionAsJava(solutionList, CinematicQueryNames.TRACK_VAR_NAME);
		for (Ident trackIdent : trackIdentList) {
			myTracks.add(new CinematicTrack(trackIdent));
		}
	}

	public CinematicInstanceConfig(Item configItem) {
		myURI_Fragment = configItem.getIdent().getLocalName();
		duration = ItemFuncs.getDouble(configItem, CinematicConfigNames.P_duration, null).floatValue();
		List<Item> trackItems = reader.readLinkedItemSeq(configItem, CinematicConfigNames.P_track);
		logInfo("Number of tracks found: " + trackItems.size());
		for (Item ti : trackItems) {
			CinematicTrack ct = new CinematicTrack(ti);
			myTracks.add(ct);
		}
	}
}
