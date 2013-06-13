/*
 *  Copyright 2012 by The Friendularity Project (www.friendularity.org).
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

package org.cogchar.joswrap;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Resource;
import org.cogchar.app.puma.web.PumaWebMapper;
import org.cogchar.bundle.bind.joseki.Activator;

import org.cogchar.app.puma.registry.PumaRegistryClient;
import org.cogchar.app.puma.registry.PumaRegistryClientFinder;
/**
 * This class is the lynchpin of our customization of Joseki.
 * 
 * Each use of the dset in the config (e.g. 1 for query, 1 for update)
 * results in a separate instance of this class, and hence potentially
 * a separate call to findPumaMainDataset().
 * 
 * @author Stu B. <www.texpedient.com>
 */

public class RepoJosDsetDesc extends ModJosDatasetDesc {
	public RepoJosDsetDesc(Resource configRootRes) {
		super(configRootRes);
	}
	/**
	 * Called from super.initialize()
	 * 
	 * ...and present ugly reality is that we need this to happen *after* PUMA has initialized the "main" repo.
	 * 
	 * @return 
	 */
	@Override protected Dataset newDataset() { 
		String uriFrag = datasetRoot.getLocalName();
		System.out.println("newDataset() invoked for [" + datasetRoot  + "] - checking frag [" + uriFrag + "]");

		if (uriFrag.toLowerCase().contains("repo")) {
			Dataset rcd = findPumaMainDataset();
			System.out.println("newDataset() Returning special Friendu-Repo MainConfigDataset (from PUMA): " + rcd);
			return rcd;
		} else {
			System.out.println("newDataset() returning default Joseki implementation");
			return super.newDataset();
		}
	}
	protected Dataset findPumaMainDataset() {
		Dataset pumaDataset = null;
		PumaRegistryClientFinder prcFinder = new PumaRegistryClientFinder();
		PumaRegistryClient pumaRegClient = prcFinder.getPumaRegClientOrNull(null, PumaRegistryClient.class);
		PumaWebMapper pwm = pumaRegClient.getWebMapper(null);
		pumaDataset = pwm.getMainSparqlDataset();
		return pumaDataset;
	}
}
