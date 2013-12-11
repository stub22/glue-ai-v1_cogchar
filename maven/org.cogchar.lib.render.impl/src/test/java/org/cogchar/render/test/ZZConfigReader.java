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

package org.cogchar.render.test;

import com.hp.hpl.jena.rdf.model.Model;
import org.appdapter.core.log.BasicDebugger;
import org.appdapter.core.matdat.OnlineSheetRepoSpec;
import org.appdapter.core.matdat.RepoSpec;
import org.appdapter.core.name.Ident;
import org.appdapter.core.store.Repo;
import org.appdapter.help.repo.RepoClient;
import org.cogchar.name.dir.AssumedQueryDir;
import org.cogchar.name.dir.AssumedGraphDir;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class ZZConfigReader extends BasicDebugger {
	String TEST_REPO_SHEET_KEY = "0ArBjkBoH40tndDdsVEVHZXhVRHFETTB5MGhGcWFmeGc";
	int  DFLT_NAMESPACE_SHEET_NUM = 9;
	int   DFLT_DIRECTORY_SHEET_NUM = 8;

	String lightsQueryQN = AssumedQueryDir.LIGHT_QUERY_URI;
	String lightsGraphQN = AssumedGraphDir.lightsGraphQN; 
	
	public void readConf() {
		
		java.util.List<ClassLoader> fileResModelCLs = new java.util.ArrayList<ClassLoader>();
		RepoSpec rs = new OnlineSheetRepoSpec(TEST_REPO_SHEET_KEY, DFLT_NAMESPACE_SHEET_NUM, DFLT_DIRECTORY_SHEET_NUM,
							fileResModelCLs);
		
		Repo.WithDirectory dfltTestRepo = rs.makeRepo();
		RepoClient dfltTestRC = rs.makeRepoClient(dfltTestRepo);
		
		Ident lightsGraphID = dfltTestRC.makeIdentForQName(lightsGraphQN);
		Model lightsModelFromSheet = dfltTestRepo.getNamedModel(lightsGraphID);
		getLogger().info("Fetched lights model: {} ", lightsModelFromSheet);
	}


}
