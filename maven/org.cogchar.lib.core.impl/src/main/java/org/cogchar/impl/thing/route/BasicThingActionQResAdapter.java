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

package org.cogchar.impl.thing.route;

import com.hp.hpl.jena.rdf.model.Literal;
import java.util.ArrayList;
import java.util.List;
import org.appdapter.core.log.BasicDebugger;
import org.appdapter.core.name.Ident;
import org.appdapter.fancy.rclient.RepoClient;
import org.appdapter.fancy.query.Solution;
import org.appdapter.fancy.query.SolutionHelper;
import org.appdapter.fancy.query.SolutionList;
import org.cogchar.api.thing.ThingActionSpec;
import org.cogchar.api.thing.SerTypedValueMap;
import org.cogchar.impl.thing.basic.BasicThingActionSpec;
import org.cogchar.impl.thing.basic.BasicTypedValueMap;
import org.cogchar.impl.thing.basic.BasicTypedValueMapWithConversion;
import org.cogchar.name.thing.ThingCN;
import org.slf4j.Logger;

/**
 * @author Stu B. <www.texpedient.com>
 * 
 * This class knows how to pull ThingAction(Spec) data from a SolutionList, which is inefficient memwise for 
 * large solution lists that might be filtered in-code, so, it is really SolutionList that needs improving upon.
 */

public class BasicThingActionQResAdapter extends BasicDebugger {

	protected List<ThingActionSpec> reapActionSpecList(SolutionList actionsList, RepoClient rc, Ident srcGraphID, Ident srcAgentID) {
		return reapActionSpecList(actionsList, rc, srcGraphID, srcAgentID, getLogger());
	}

	static public List<ThingActionSpec> reapActionSpecList(SolutionList actionsList, RepoClient rc, Ident srcGraphID, Ident srcAgentID, Logger theLogger) {
		List<ThingActionSpec> actionSpecList = new ArrayList<ThingActionSpec>();
		SolutionHelper sh = new SolutionHelper();
		for (Object actionSoln0 : actionsList.javaList()) {
			Solution actionSoln = (Solution) actionSoln0;
			Ident actionID = sh.pullIdent(actionSoln, ThingCN.V_actionID);
			Ident verbID = sh.pullIdent(actionSoln, ThingCN.V_verbID);
			Ident targetID = sh.pullIdent(actionSoln, ThingCN.V_targetThingID);
			Ident targetTypeID = sh.pullIdent(actionSoln, ThingCN.V_targetThingTypeID);
			// calls rc.queryIndirectForAllSolutions(ThingCN.PARAM_QUERY_URI, srcGraphID, ThingCN.V_attachedActionID, actionIdent);
			// which we want to be transactionally consistent with whatever generated the actionsList.
			SerTypedValueMap actionParams = buildActionParameterValueMap(rc, srcGraphID, sh, actionID, theLogger);
			Literal tstampLiteral = actionSoln.getLiteralResultVar(ThingCN.V_postedTStampMsec);
			Long actionPostedTStampMsec = (tstampLiteral != null) ? tstampLiteral.getLong() : null;
			ThingActionSpec spec = new BasicThingActionSpec(actionID, targetID, targetTypeID, verbID, srcAgentID, actionParams, actionPostedTStampMsec);
			theLogger.debug("Found new ThingAction: {}", spec);
			actionSpecList.add(spec);
		}
		return actionSpecList;
	}

	protected SerTypedValueMap buildActionParameterValueMap(RepoClient rc, Ident srcGraphID, SolutionHelper sh, Ident actionIdent) {
		return buildActionParameterValueMap(rc, srcGraphID, sh, actionIdent, getLogger());
	}

	static public SerTypedValueMap buildActionParameterValueMap(RepoClient rc, Ident srcGraphID, SolutionHelper sh, Ident actionIdent, Logger theLogger) {
		BasicTypedValueMap paramMap = new BasicTypedValueMapWithConversion();
		SolutionList paramList = rc.queryIndirectForAllSolutions(ThingCN.PARAM_QUERY_URI, srcGraphID, ThingCN.V_attachedActionID, actionIdent);
		for (Object paramSoln0 : paramList.javaList()) {
			Solution paramSoln = (Solution) paramSoln0;
			Ident paramIdent = sh.pullIdent(paramSoln, ThingCN.V_actParamID);
            String paramValue = null;
            try {
                Ident paramValueIdent = sh.pullIdent(paramSoln, ThingCN.V_actParamVal);
                paramValue = paramValueIdent.getAbsUriString();
            }
            catch(ClassCastException ex) {
                paramValue = sh.pullString(paramSoln, ThingCN.V_actParamVal);
            }
			theLogger.debug("Adding new param for Thing action {}: ident: {}, value: {}", new Object[] { actionIdent, paramIdent, paramValue });
			paramMap.putValueAtName(paramIdent, paramValue);
		}
		return paramMap;
	}
}
