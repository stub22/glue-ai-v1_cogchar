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

package org.cogchar.bind.rk.aniconv;

import java.util.ArrayList;
import java.util.List;
import org.appdapter.core.name.Ident;
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


public class MayaModelMapManager implements MayaModelMapManagerInterface {
	
	private final static Logger theLogger = LoggerFactory.getLogger(MayaModelMapManager.class.getName());
	
	public List<MayaModelMap> myMMMs = new ArrayList<MayaModelMap>();
	
	// A constructor to build BoneRobotConfig from spreadsheet
	public MayaModelMapManager(RepoClient rc, Ident graphIdent) {
		addMaps(rc, graphIdent);
	}
	
	// We can also add maps to an existing manager:
	public void addMaps(RepoClient rc, Ident graphIdent) {
		SolutionHelper sh = new SolutionHelper();
		MayaCN mcn = new MayaCN();
		theLogger.info("Building Maya Model maps via queries using graph {} ", graphIdent);
		SolutionList mapSL = rc.queryIndirectForAllSolutions(mcn.MAYA_MAP_QUERY_URI, graphIdent);
		for (Solution modelMapSoln: mapSL.javaList()) {
			Ident mapUri = sh.pullIdent(modelMapSoln, mcn.MAP_VAR_NAME);
			SolutionList mappingSL = rc.queryIndirectForAllSolutions(mcn.MAYA_CHANNEL_QUERY_URI, graphIdent,
				mcn.MAP_INSTANCE_QUERY_VAR_NAME, mapUri);
			myMMMs.add(new MayaModelMap(rc, graphIdent, mapUri, mappingSL));
		}
	}
}

interface MayaModelMapManagerInterface {
	// Needed for LifeCycle, needs to be revisited
	// The main reason this is empty is that the Aniconv stuff doesn't need to access the MayaModelMapManager per se;
	// it sees the maps on the registry after they are added in addMaps
}
