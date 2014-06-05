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

import java.util.HashMap; // Not needed once javaMap is added to SolutionMap
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.appdapter.bind.rdf.jena.assembly.KnownComponentImpl;
import org.appdapter.core.name.Ident;
import org.appdapter.help.repo.*;
import org.cogchar.name.lifter.UserCN;

/**
 *
 * @author Ryan Biggs <rbiggs@hansonrobokind.com>
 */
public class UserAccessConfig extends KnownComponentImpl {

	// A Note: I think I'm not as big a fan of the "myXXX" naming convention for instance variables in the case (as here)
	// where the instance fields are accessed directly by consuming code and define inherent properties of an instance 
	// of a class which is primarily used for containing state.
	// Otherwise we have somewhat syntatically confusing constructs in the form aStateObject.myStateField 
	// (e.g. aBall.mySize, aBall.myColor, aBall.myType instead of aBall.size, aBall.color, and aBall.type)
	// A debatable area of style, but I do like the "myXXX" convention with the exception of this case. -Ryan
	public Ident loginPage;
	public Map<Ident, UserConfig> users = new HashMap<Ident, UserConfig>();

	public UserAccessConfig(RepoClient rc, Ident graphIdent) {
		SolutionHelper sh = new SolutionHelper();
		SolutionList loginPageSolList = rc.queryIndirectForAllSolutions(UserCN.LOGIN_PAGE_QUERY_URI, graphIdent);
		List<Ident> loginList = sh.pullIdentsAsJava(loginPageSolList, UserCN.LOGIN_PAGE_VAR_NAME);
		if (loginList.size() >= 1) {
			loginPage = loginList.get(0);
			if (loginList.size() > 1) {
				logWarning("Found more than one startup liftConfig; using " + loginPage + " and ignoring the rest");
			}
		}
		SolutionList userSolList = rc.queryIndirectForAllSolutions(UserCN.USER_QUERY_URI, graphIdent);
		SolutionMap userSolMap = userSolList.makeSolutionMap(UserCN.USER_VAR_NAME);
		
		Iterator userIterator = userSolMap.getJavaIterator();
		Map<Ident, Solution> javaSolutionMap = HelpRepoExtensions.convertToJavaMap(userSolMap.map()); // Not needed once javaMap is added to SolutionMap
		while (userIterator.hasNext()) {
			Ident user = (Ident) userIterator.next();
			// users.put(user, new UserConfig(qi, graphIdent, solutionMap.javaMap().get(user))); // We can use this form once javaMap is added to SolutionMap
			users.put(user, new UserConfig(rc, graphIdent, javaSolutionMap.get(user))); // A temporary way to do it until javaMap is added to SolutionMap
		}

	}

	public static class UserConfig {

		public String hashedPassword;
		public String salt;
		public Ident startConfig;
		public Ident userClass;

		public UserConfig(RepoClient qi, Ident graphIdent, Solution userSolution) {
			SolutionHelper sh = new SolutionHelper();
			hashedPassword = sh.pullString(userSolution, UserCN.PASSWORD_VAR_NAME);
			salt = sh.pullString(userSolution, UserCN.SALT_VAR_NAME);
			startConfig = sh.pullIdent(userSolution, UserCN.START_PAGE_VAR_NAME);
			userClass = sh.pullIdent(userSolution, UserCN.USER_CLASS_VAR_NAME);
		}
	}
}
