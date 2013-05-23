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

package org.cogchar.api.thing;

import org.cogchar.name.thing.ThingCN;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import java.util.ArrayList;
import java.util.List;
import org.appdapter.core.name.FreeIdent;
import org.appdapter.core.name.Ident;
import org.appdapter.core.store.Repo;
import org.appdapter.help.repo.RepoClient;
import org.appdapter.help.repo.Solution;
import org.appdapter.help.repo.SolutionHelper;
import org.appdapter.help.repo.SolutionList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Ryan Biggs <rbiggs@hansonrobokind.com>
 */


public class ThingActionUpdater {
	
	// This is just a temporary definition of the sourceAgentID until it becomes clear where this best comes from
	public static final Ident SOURCE_AGENT_ID = new FreeIdent(ThingCN.TA_NS + "RepoThingAction"); 
	
	private static Logger theLogger = LoggerFactory.getLogger(ThingActionUpdater.class);
	
	/**
	 * Fetches pending ThingActions from model, and physically deletes them (or at least the part of them
	 * that makes them matchable) from source model.
	 * @param rc
	 * @param srcGraphID
	 * @return 
	 */
	@Deprecated protected List<ThingActionSpec> takeThingActions(RepoClient rc, Ident srcGraphID) {
		SolutionList actionsSolList = rc.queryIndirectForAllSolutions(ThingCN.ACTION_QUERY_URI, srcGraphID);
		ThingActionQResAdapter taqra = new ThingActionQResAdapter();
		List<ThingActionSpec> actionSpecList = taqra.reapActionSpecList(actionsSolList, rc, srcGraphID, SOURCE_AGENT_ID);
		// Delete the actions from graph, so they are not returned on next call to this method.
		for (ThingActionSpec tas : actionSpecList) {
			deleteThingAction(rc, srcGraphID, tas);
		}
		theLogger.info("Returning ThingAction list of length {} from graph {}",  actionSpecList.size(), srcGraphID);
		return actionSpecList;
	}
	/**
	 * Finds actions not yet seen by a particular reading agent, pulls their data, and marks them seen for that agent.
	 * @param rc
	 * @param srcGraphID
	 * @param seeingAgentID
	 * @return 
	 */
	public List<ThingActionSpec> seeActions(RepoClient rc, Ident srcGraphID, Ident seeingAgentID) {
		String seeingAgentVarName = "";
		SolutionList actionsSolList = rc.queryIndirectForAllSolutions(ThingCN.UNSEEN_ACTION_QUERY_URI, srcGraphID, 
						seeingAgentVarName, seeingAgentID);
		ThingActionQResAdapter taqra = new ThingActionQResAdapter();
		List<ThingActionSpec> actionSpecList = taqra.reapActionSpecList(actionsSolList, rc, srcGraphID, SOURCE_AGENT_ID);
		// Delete the actions from graph, so they are not returned on next call to this method.
		for (ThingActionSpec tas : actionSpecList) {
			markThingActionSeen(rc, srcGraphID, tas, seeingAgentID);
		}
		theLogger.info("Returning ThingAction list of length {} from graph {}",  actionSpecList.size(), srcGraphID);
		return actionSpecList;
	}	

			
	/**
	 *  Q:  Under what conditions are we allowed to do this directly through Dataset.getNamedModel() actions?
	 *  A:  Not sure - the clean-est way is to generate SPARQL-UPDATE and apply.
     * If we are allowed to modify the model directly using Jena API, then it will be sufficient (for immediate
	 *	practical purposes) to delete all triples with actionIdent as SUBJECT.
	 * @param tas 
	 */
	private void deleteThingAction(RepoClient rc, Ident graphID, ThingActionSpec tas) { 
		Ident actionID = tas.getActionSpecID();
		Resource actionRes = rc.makeResourceForIdent(actionID);
		Repo.WithDirectory repo = rc.getRepo();
		Model gm = repo.getNamedModel(graphID);
		theLogger.info("Prior to removal from {}, graph size is {}", graphID, gm.size());
		gm.removeAll(actionRes, null, null);
		theLogger.info("After remova from {}, graph size is {}", graphID, gm.size());
	}
	
	private void markThingActionSeen(RepoClient rc, Ident graphID, ThingActionSpec tas,  Ident seeingAgentID) { 
		
	}

}
