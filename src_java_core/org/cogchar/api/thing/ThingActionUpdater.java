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


class ThingActionUpdater {
	
	// This is just a temporary definition of the sourceAgentID until it becomes clear where this best comes from
	public static final Ident sourceAgentID = new FreeIdent(ThingCN.THING_NS + "RepoThingAction"); 
	
	private static Logger theLogger = LoggerFactory.getLogger(ThingActionUpdater.class);
	
	/**
	 * Fetches pending ThingActions from model, and deletes them from model.
	 * @param rc
	 * @param graphIdent
	 * @return 
	 */
	public List<ThingActionSpec> takeThingActions(RepoClient rc, Ident graphIdent) {
		List<ThingActionSpec> actionSpecList = new ArrayList<ThingActionSpec>();
		SolutionHelper sh = new SolutionHelper();
		SolutionList actionsList = rc.queryIndirectForAllSolutions(ThingCN.ACTION_QUERY_URI, graphIdent);
		for (Solution actionSoln: actionsList.javaList()) {
			Ident actionID = sh.pullIdent(actionSoln, ThingCN.ACTION_URI_VAR_NAME);
			Ident verbID = sh.pullIdent(actionSoln, ThingCN.VERB_VAR_NAME);
			Ident targetID = sh.pullIdent(actionSoln, ThingCN.TARGET_VAR_NAME);
			Ident targetTypeID = sh.pullIdent(actionSoln, ThingCN.TARGET_TYPE_VAR_NAME);
			
			TypedValueMap actionParams = buildActionParameterValueMap(rc, graphIdent, sh, actionID);
			ThingActionSpec spec = new BasicThingActionSpec(actionID, targetID, targetTypeID, verbID, sourceAgentID, actionParams);
			theLogger.debug("Found new ThingAction: {}", spec);
			actionSpecList.add(spec);
		}
		// Delete the actions from graph, so they are not returned on next call to this method.

		for (ThingActionSpec tas : actionSpecList) {
			deleteThingAction(rc, graphIdent, tas);
		}
		theLogger.info("Returning ThingAction list of length {} from graph {}",  actionSpecList.size(), graphIdent);
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
	
	private TypedValueMap buildActionParameterValueMap(RepoClient rc, Ident graphIdent, SolutionHelper sh, Ident actionIdent) {
		BasicTypedValueMap paramMap = new BasicTypedValueMapTemporaryImpl();
		SolutionList paramList = rc.queryIndirectForAllSolutions(ThingCN.PARAM_QUERY_URI, graphIdent,
				ThingCN.ACTION_QUERY_VAR_NAME, actionIdent);
		for (Solution paramSoln: paramList.javaList()) {
			Ident paramIdent = sh.pullIdent(paramSoln, ThingCN.PARAM_IDENT_VAR_NAME);
			String paramValue = sh.pullString(paramSoln, ThingCN.PARAM_VALUE_VAR_NAME);
			theLogger.debug("Adding new param for Thing action {}: ident: {}, value: {}",
					new Object[]{actionIdent, paramIdent, paramValue});
			paramMap.putValueAtName(paramIdent, paramValue);
		}
		return paramMap;
	}
}
