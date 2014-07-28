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

package org.cogchar.bind.mio.repo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.appdapter.help.repo.RepoClient;
import org.appdapter.help.repo.RepoClientImpl;
import org.appdapter.impl.store.FancyRepo;
import org.cogchar.api.perform.PerfChannel;
import org.cogchar.impl.scene.SceneSpec;
import org.jflux.impl.services.rk.lifecycle.AbstractLifecycleProvider;
import org.jflux.impl.services.rk.lifecycle.DependencyDescriptor;
import org.jflux.impl.services.rk.osgi.OSGiUtils;

/**
 * @author Stu B. <www.texpedient.com>
 * 
 * Provides a RepoClient based on some Repo which is either local or remote.
 */

public class RepoClientLifecycle extends AbstractLifecycleProvider <RepoClient, RepoClient> {
	private		String myQueryTargetVarName;
	private		String myQuerySheetQN;
	
	public RepoClientLifecycle(		String queryTargetVarName, String querySheetQN)	 {
		super(buildDescriptorList(queryTargetVarName, querySheetQN));
		myQueryTargetVarName = queryTargetVarName;
		myQuerySheetQN = querySheetQN;
	}
    private static List<DependencyDescriptor> buildDescriptorList(String queryTargetVarName, String querySheetQN){

        List<DependencyDescriptor> descriptors = new ArrayList<DependencyDescriptor>();
		/*        for(String uri : chanURIs){
            descriptors.add(new DependencyDescriptor(uri, Channel.class, 
                    OSGiUtils.createFilter("URI", uri), DependencyDescriptor.DependencyType.REQUIRED));		*/ 
		return descriptors;
	}
	@Override protected RepoClient create(Map<String, Object> map) {
		FancyRepo	fancyRepo = null;
		RepoClient rc = new RepoClientImpl(fancyRepo, myQueryTargetVarName, myQuerySheetQN)	;
		return rc;	
	}

	@Override protected void handleChange(String string, Object o, Map<String, Object> map) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override protected Class<RepoClient> getServiceClass() {
		return RepoClient.class;
	}

}
