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
package org.cogchar.bind.rk.aniconv;

import org.cogchar.name.aniconv.MayaCN;
import java.util.HashMap;
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


public class MayaModelMap extends HashMap<String,MayaChannelMapping> {
	
	//public Map<String,MayaChannelMapping> myMCMs = new HashMap<String,MayaChannelMapping>();
	
	private final static Logger theLogger = LoggerFactory.getLogger(MayaModelMap.class.getName());
	
	public Ident myUri;
	
	// A new constructor to build BoneRobotConfig from spreadsheet
	public MayaModelMap(RepoClient rc, Ident graphIdent, Ident uri, SolutionList mappingSL) {
		myUri = uri;
		SolutionHelper sh = new SolutionHelper();
		MayaCN mcn = new MayaCN();
		theLogger.info("Building MayaModelConfig via queries using graph {} ", graphIdent);
		for (Solution channelMapping: mappingSL.javaList()) {
			MayaChannelMapping newMapping = new MayaChannelMapping(channelMapping, sh, mcn);
			theLogger.info("Adding new Maya channel mapping: " + newMapping.toString()); // TEST ONLY
			this.put(newMapping.channelName, newMapping);
		}
	}	
	
}
