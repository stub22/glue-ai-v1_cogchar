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
package org.cogchar.impl.web.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.appdapter.core.name.Ident;
import org.appdapter.help.repo.*;

/**
 *
 * @author Ryan Biggs <rbiggs@hansonrobokind.com>
 */


public class LiftQueryEnvoy {
	
	
	List<NameAndAction> getNamesAndActionsFromQuery(RepoClient rc, Ident myQGraph, 
			Ident queryIdent, String actionVarName, String nameVarName) {
		List<NameAndAction> namesAndActions = new ArrayList<NameAndAction>();
		SolutionHelper sh = new SolutionHelper();
		SolutionList actionsList = rc.queryIndirectForAllSolutions(queryIdent.getAbsUriString(), myQGraph);
		for (Solution solution : actionsList.javaList()) {
			String name = sh.pullString(solution, nameVarName);
			//System.out.println("In getNamesAndActionsFromQuery, found name " + name); // TEST ONLY
			Ident action = sh.pullIdent(solution, actionVarName);
			namesAndActions.add(new NameAndAction(name, action));
		}
		Collections.sort(namesAndActions); // Alphabetize by name
		return namesAndActions;
	}
}
