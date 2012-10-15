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

package org.cogchar.bundle.app.puma;

import org.appdapter.help.repo.RepoClient;
import org.appdapter.impl.store.FancyRepo;
import org.cogchar.blob.emit.RepoSpec;
import org.cogchar.blob.emit.RepoClientTester;
import org.osgi.framework.BundleContext;

import org.appdapter.core.store.Repo;
import org.appdapter.help.repo.RepoClient;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class VanillaConfigManager extends PumaConfigManager {
	@Override protected void applyDefaultRepoClientAsMainConfig(PumaContextMediator mediator, BundleContext optBundCtxForLifecycle) {
		applyVanillaRepoClientAsMainConfig(mediator, optBundCtxForLifecycle);
	}
	protected void applyVanillaRepoClientAsMainConfig( PumaContextMediator mediator, BundleContext optBundCtxForLifecycle) {
		// TODO:  "turn off" any previous config's lifecycle
		RepoClient vanRC = makeVanillaRepoClient(mediator);
		if (vanRC != null) {
			setMainConfigRepoClient(vanRC);
			if (optBundCtxForLifecycle != null)  {
				myQueryComp = startRepoClientLifecyle(optBundCtxForLifecycle, vanRC);
			}
		}
	}		
	protected static RepoClient makeVanillaRepoClient(PumaContextMediator mediator) {
		RepoSpec rspec = mediator.getMainConfigRepoSpec();
				
		Repo.WithDirectory testRepo = rspec.makeRepo();
		RepoClient repoCli = rspec.makeRepoClient(testRepo);
		return repoCli;
	}
	

}
