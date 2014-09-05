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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.appdapter.help.repo.RepoClient;
import org.cogchar.bind.mio.behavior.ChannelBindingConfig;
import org.cogchar.bind.mio.behavior.ServiceChannelExtender;
// import org.cogchar.blob.emit.BehavMasterConfigTest;
import org.cogchar.impl.scene.read.SceneSpecReader;
import org.cogchar.impl.channel.FancyChannelSpec;
import org.osgi.framework.BundleContext;

import org.cogchar.name.behavior.MasterDemoNames;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class ChannelWiringDemo extends WiringDemo {
	
	public static String GROUP_KEY_CHAN_BIND = MasterDemoNames.GROUP_KEY_CHAN_BIND; // "ChannelBindingGroupId";
	
	public String myDefaultChanGroupQName = MasterDemoNames.CHAN_GROUP_QN; // "csi:demo_master_chan_group_22";
	public String CHAN_BIND_GRAPH_QN = MasterDemoNames.CHAN_BIND_GRAPH_QN;
	
	public ChannelWiringDemo(BundleContext bundleCtx, RepoClient demoRepoClient) {
		super(bundleCtx, demoRepoClient);
	}
	public void initialChannelLoad(BundleContext bundleCtx, RepoClient demoRepoClient, String chanGroupName)  { 
		Collection<FancyChannelSpec> chanSpecs = loadDemoChannelSpecs(demoRepoClient);
		setupChannelBindingConfigOSGiComps(bundleCtx, chanSpecs, chanGroupName);		
	}

	public Set<FancyChannelSpec> loadDemoChannelSpecs(RepoClient bmcRepoCli) {
		getLogger().info("************************ loadDemoChannelSpecs()");
		SceneSpecReader ssr = getSceneSpecReader();
		// Use an arbitrarily assumed name for the ChannelBinding Graph (as set in the "Dir" model of the source repo).
		Set<FancyChannelSpec> chanSpecs = ssr.readChannelSpecs(bmcRepoCli, CHAN_BIND_GRAPH_QN);

		getLogger().info("Loaded ChanSpecs: " + chanSpecs);
		return chanSpecs;
	}

	public List<Runnable> makeChannelBindingConfigRegRunnables(BundleContext bundleCtx, Collection<FancyChannelSpec> chanSpecs,
					String chanGroupQName) {
		List<Runnable> runnables = new ArrayList<Runnable>();
		for (FancyChannelSpec cs : chanSpecs) {
			ChannelBindingConfig cbc = new ChannelBindingConfig();
			cbc.initFromChannelSpec(cs);
			String key = GROUP_KEY_CHAN_BIND;
			Runnable cbcRegRunnable = makeChanBindConfRegRunnable(bundleCtx, cbc, GROUP_KEY_CHAN_BIND, chanGroupQName);
			getLogger().info("Registered channel-binding-config and made runnable for {} ", cbc);
			runnables.add(cbcRegRunnable);
		}
		return runnables;
	}

	public void setupChannelBindingConfigOSGiComps(BundleContext bundleCtx, Collection<FancyChannelSpec> chanSpecs, String chanGroupName) {
		getLogger().info("************************ setupChannelBindingConfigOSGiComps()");
		Collection<Runnable> chanBindConfRegRunnables = makeChannelBindingConfigRegRunnables(bundleCtx, chanSpecs, chanGroupName);
		for (Runnable r : chanBindConfRegRunnables) {
			r.run();
		}
	}

	@Override public void registerJFluxExtenders(BundleContext bundleCtx) {
		ServiceChannelExtender sce = new ServiceChannelExtender(bundleCtx, null, null);
		sce.start();
	}

	private Runnable makeChanBindConfRegRunnable(BundleContext context, ChannelBindingConfig cbc, 
				final String key, final String val) {
		return getRegistrationRunnable(context, ChannelBindingConfig.class, cbc, key, val);
	}

}
