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
package org.cogchar.bind.lift;

import java.util.ArrayList;
import java.util.List;
import org.appdapter.core.component.KnownComponentImpl;
import org.appdapter.core.name.Ident;
import org.appdapter.help.repo.*;
import org.cogchar.name.lifter.LiftCN;

/**
 * Used to enclose data from RDF Lift web app "screen" configurations
 *
 * @author Ryan Biggs
 */
public class LiftConfig extends KnownComponentImpl {

	private static final String DEFAULT_TEMPLATE = "12slots";
	
	public List<ControlConfig> myCCs = new ArrayList<ControlConfig>();
	//public List<ControlActionConfig> myCACs = new ArrayList<ControlActionConfig>();
	public String template = DEFAULT_TEMPLATE;
	
	// A contructor for use by PageCommander
	public LiftConfig(String templateToUse) {
		template = templateToUse;
	}
	
	// A new constructor to build CinematicConfig from spreadsheet
	public LiftConfig(RepoClient qi, Ident graphIdent, Ident configUri) {
		SolutionHelper sh = new SolutionHelper();
		SolutionList liftTemplatesList = qi.queryIndirectForAllSolutions(LiftCN.TEMPLATE_QUERY_URI, graphIdent);
		SolutionMap solutionMap = liftTemplatesList.makeSolutionMap(LiftCN.CONFIG_VAR_NAME);
		String foundTemplate = sh.pullString(solutionMap, configUri, LiftCN.TEMPLATE_VAR_NAME);
		if (foundTemplate != null) {
			template = foundTemplate;
		}
		// Read in controls
		SolutionList solutionList = qi.queryIndirectForAllSolutions(LiftCN.CONTROL_QUERY_TEMPLATE_URI, graphIdent, 
							LiftCN.CONFIG_QUERY_VAR_NAME, configUri);
		for (Solution solution : solutionList.javaList()) {
			myCCs.add(new ControlConfig(qi, solution));
		}
		/* Not too sure we want these in here...
		// Read in control actions
		solutionList = qi.queryIndirectForAllSolutions(LiftCN.CONTROL_ACTION_QUERY_TEMPLATE_URI, graphIdent, 
							LiftCN.CONFIG_QUERY_VAR_NAME, configUri);
		for (Solution solution : solutionList.javaList()) {
			myCACs.add(new ControlActionConfig(qi, solution));
		}
		*/ 
	}

	
}
