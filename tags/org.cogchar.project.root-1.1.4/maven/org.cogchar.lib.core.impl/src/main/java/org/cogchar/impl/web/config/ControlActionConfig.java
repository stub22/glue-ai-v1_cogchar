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

package org.cogchar.impl.web.config;

import org.appdapter.core.name.Ident;
import org.appdapter.fancy.rclient.RepoClient;
import org.appdapter.fancy.query.Solution;
import org.appdapter.fancy.query.SolutionHelper;
import org.appdapter.fancy.query.SolutionList;
import org.cogchar.name.lifter.LiftCN;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encloses repo-defined actions for displaying individual controls
 * 
 * @author Ryan Biggs <rbiggs@hansonrobokind.com>
 */


public class ControlActionConfig {
	
	private static Logger theLogger = LoggerFactory.getLogger(ControlActionConfig.class);
	
	public String myURI_Fragment;
	public Ident control; // For a Lifter control action
	public Ident config; // For a Lifter config (web screen) action
	public int slotNum;
	public Ident userClass;
	
	
	// A constructor to build ControlActionConfig from spreadsheet
	public ControlActionConfig(RepoClient qi, Solution solution) {
		SolutionHelper sh = new SolutionHelper();
		Ident myIdent = sh.pullIdent(solution, LiftCN.CONTROL_ACTION_VAR_NAME);
		if (myIdent != null) {
			myURI_Fragment = myIdent.getLocalName();
		}
		control = sh.pullIdent(solution, LiftCN.CONTROL_VAR_NAME);
		config = sh.pullIdent(solution, LiftCN.CONFIG_VAR_NAME);
		slotNum = Math.round(sh.pullFloat(solution, LiftCN.SLOTNUM_VAR_NAME, 0)); // We still need SolutionHelper.pullInteger for a single solution!
		userClass = sh.pullIdent(solution, LiftCN.USER_CLASS_VAR_NAME);
	}
	
	// A factory method to get a ControlConfig by URI alone
	// Very similar to ControlConfig.getControlConfigFromUri and likely should be refactored into common superclass
	public static ControlActionConfig getControlActionConfigFromUri(RepoClient qi, Ident graphIdent, Ident configActionUri) {
		ControlActionConfig newActionConfig = null;
		SolutionList solutionList = qi.queryIndirectForAllSolutions(LiftCN.FREE_CONTROL_ACTION_QUERY_TEMPLATE_URI, graphIdent, 
							LiftCN.CONTROL_ACTION_QUERY_VAR_NAME, configActionUri);
		switch (solutionList.javaList().size()) {
			case 0:	theLogger.warn("Could not find control action with URI {}", configActionUri); break;
			case 1: newActionConfig = new ControlActionConfig(qi, solutionList.javaList().get(0));
								newActionConfig.myURI_Fragment = configActionUri.getLocalName();
								break;
			default: theLogger.error("Found multiple controls with URI {}", configActionUri); break;
		}
		return newActionConfig;
	}
}
