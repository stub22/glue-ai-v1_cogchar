/*
 *  Copyright 2013 by The Cogchar Project (www.cogchar.org).
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

import java.util.ArrayList;
import java.util.List;
import org.appdapter.core.log.BasicDebugger;
import org.appdapter.core.name.Ident;
import org.appdapter.help.repo.RepoClient;
import org.appdapter.help.repo.Solution;
import org.appdapter.help.repo.SolutionHelper;
import org.appdapter.help.repo.SolutionList;
import org.cogchar.name.thing.ThingCN;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class ThingActionQResAdapter extends BasicDebugger {
	
	
	protected List<ThingActionSpec> reapActionSpecList(SolutionList actionsList, RepoClient rc, Ident srcGraphID, Ident srcAgentID) {
		List<ThingActionSpec> actionSpecList = new ArrayList<ThingActionSpec>();
		SolutionHelper sh = new SolutionHelper();
		for (Solution actionSoln: actionsList.javaList()) {
			Ident actionID = sh.pullIdent(actionSoln, ThingCN.ACTION_URI_VAR_NAME);
			Ident verbID = sh.pullIdent(actionSoln, ThingCN.VERB_VAR_NAME);
			Ident targetID = sh.pullIdent(actionSoln, ThingCN.TARGET_VAR_NAME);
			Ident targetTypeID = sh.pullIdent(actionSoln, ThingCN.TARGET_TYPE_VAR_NAME);
			
			TypedValueMap actionParams = buildActionParameterValueMap(rc, srcGraphID, sh, actionID);
			ThingActionSpec spec = new BasicThingActionSpec(actionID, targetID, targetTypeID, verbID, srcAgentID, actionParams);
			getLogger().debug("Found new ThingAction: {}", spec);
			actionSpecList.add(spec);
		}		
		return actionSpecList;
	}	
	protected TypedValueMap buildActionParameterValueMap(RepoClient rc, Ident srcGraphID, SolutionHelper sh, Ident actionIdent) {
		BasicTypedValueMap paramMap = new BasicTypedValueMapTemporaryImpl();
		SolutionList paramList = rc.queryIndirectForAllSolutions(ThingCN.PARAM_QUERY_URI, srcGraphID,
				ThingCN.ACTION_QUERY_VAR_NAME, actionIdent);
		for (Solution paramSoln: paramList.javaList()) {
			Ident paramIdent = sh.pullIdent(paramSoln, ThingCN.PARAM_IDENT_VAR_NAME);
			String paramValue = sh.pullString(paramSoln, ThingCN.PARAM_VALUE_VAR_NAME);
			getLogger().debug("Adding new param for Thing action {}: ident: {}, value: {}",
					new Object[]{actionIdent, paramIdent, paramValue});
			paramMap.putValueAtName(paramIdent, paramValue);
		}
		return paramMap;
	}
}
