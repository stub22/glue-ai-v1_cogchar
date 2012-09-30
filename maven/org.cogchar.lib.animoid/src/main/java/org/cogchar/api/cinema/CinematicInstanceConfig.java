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
import org.appdapter.core.name.Ident;
import org.appdapter.help.repo.Solution;
import org.appdapter.help.repo.SolutionList;
import org.appdapter.help.repo.RepoClient;
import org.appdapter.help.repo.SolutionHelper;

/**
 * @author Ryan Biggs
 */
public class CinematicInstanceConfig extends QueryBackedConfigBase {

	public String myURI_Fragment;
	public float duration;
	public List<CinematicTrack> myTracks = new ArrayList<CinematicTrack>();
	// private static final ItemAssemblyReader reader = new ItemAssemblyReaderImpl();
	
	

	@Override
	public String toString() {
		return "CinematicInstanceConfig[uriFrag = " + myURI_Fragment + ", duration = " + Float.toString(duration) + ", Number of tracks = " + Integer.toString(myTracks.size());
	}

	// TODO
	
	// A new constructor to build CinematicConfig from query results
	public CinematicInstanceConfig(RepoClient qi, Solution querySolution, Ident qGraph) {
		super(qi);
		SolutionHelper sh = new SolutionHelper();
		Ident myIdent = sh.pullIdent(querySolution, CinemaCN.CINEMATIC_VAR_NAME);
		myURI_Fragment = myIdent.getLocalName();
		duration = sh.pullFloat(querySolution, CinemaCN.DURATION_VAR_NAME, Float.NaN);
		SolutionList solutionList = qi.queryIndirectForAllSolutions(CinemaCN.TRACKS_QUERY_TEMPLATE_URI, qGraph, 
							CinemaCN.CINEMATIC_QUERY_VAR_NAME, myIdent);

		List<Ident> trackIdentList = sh.pullIdentsAsJava(solutionList, CinemaCN.TRACK_VAR_NAME);
		for (Ident trackIdent : trackIdentList) {
			myTracks.add(new CinematicTrack(trackIdent));
		}
	}
	public CinematicInstanceConfig(ItemAssemblyReader iaReader, Item configItem) {
		super(null);
		myURI_Fragment = configItem.getIdent().getLocalName();
		duration = ItemFuncs.getDouble(configItem, CinemaAN.P_duration, null).floatValue();
		List<Item> trackItems = iaReader.readLinkedItemSeq(configItem, CinemaAN.P_track);
		logInfo("Number of tracks found: " + trackItems.size());
		for (Item ti : trackItems) {
			CinematicTrack ct = new CinematicTrack(iaReader, ti);
			myTracks.add(ct);
		}
	}
}
