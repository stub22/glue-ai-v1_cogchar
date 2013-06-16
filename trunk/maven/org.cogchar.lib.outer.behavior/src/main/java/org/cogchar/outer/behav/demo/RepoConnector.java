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


package org.cogchar.outer.behav.demo;

import org.appdapter.core.store.Repo;
import org.appdapter.help.repo.RepoClient;
import org.appdapter.help.repo.RepoClientImpl;
// normally we dont use * in imports but this way we can work/test with both appdapter 1.1.0 or 1.1.1 and the respective version of cogchars
import org.appdapter.core.matdat.*;
import org.cogchar.blob.emit.*;
import org.osgi.framework.BundleContext;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class RepoConnector {
	
	public RepoSpec makeDefaultOnlineSheetRepoSpec(BundleContext bundleCtx) {
		OnlineSheetRepoSpec bmcRepoSpec = BehavMasterConfigTest.makeBMC_RepoSpec(bundleCtx);
		return bmcRepoSpec;
	}

	public RepoSpec makeDefaultLocalSheetRepoSpec(BundleContext bundleCtxOrNull) {
		// @TODO (Doug) :  Setup a RepoSpec that refers to the local sheets.
		RepoSpec localSheetRepoSpec = BehavMasterConfigTest.makeBMC_OfflineRepoSpec(bundleCtxOrNull);
		return localSheetRepoSpec;
	}

	public EnhancedRepoClient connectDemoRepoClient(RepoSpec repoSpec) {
		Repo.WithDirectory bmcMemoryRepoHandle = repoSpec.makeRepo();
		// Before we enhanced the repoCli, this was just:  repoSpec.makeRepoClient(bmcMemoryRepoHandle);
		EnhancedRepoClient bmcRepoCli = new EnhancedRepoClient(repoSpec, bmcMemoryRepoHandle, 
					BehavMasterConfigTest.TGT_GRAPH_SPARQL_VAR(), BehavMasterConfigTest.QUERY_SOURCE_GRAPH_QN());
		return bmcRepoCli;
	}
	
	public EnhancedRepoClient makeRepoClientForDefaultOnlineSheet(BundleContext bundleCtx) { 
		RepoSpec demoRepoSpec = makeDefaultOnlineSheetRepoSpec(bundleCtx);
		EnhancedRepoClient demoRepoClient = connectDemoRepoClient(demoRepoSpec);
		return demoRepoClient;
	}	
}
