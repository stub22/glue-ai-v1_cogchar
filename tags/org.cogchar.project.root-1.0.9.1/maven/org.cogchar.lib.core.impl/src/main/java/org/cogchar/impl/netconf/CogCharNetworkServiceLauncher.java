/*
 *  Copyright 2014 by The Friendularity Project (www.friendularity.org).
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

package org.cogchar.impl.netconf;

import org.cogchar.impl.netconf.CogCharNetworkConfigAmbassador;

// Note these specific implementation imports.

import org.jflux.impl.services.rk.lifecycle.ServiceLifecycleProvider;
import org.jflux.impl.services.rk.lifecycle.utils.SimpleLifecycle;
import org.jflux.impl.services.rk.osgi.lifecycle.OSGiComponent;
import org.osgi.framework.BundleContext;

import org.cogchar.impl.web.config.LiftAmbassador;

/**
 * Pulled from the Activator of the old prototype: o.f.b.netconfig
 * @author Stu B. <www.texpedient.com>
 */

public class CogCharNetworkServiceLauncher {
	
	
	public static void startCogCharAmbassadors(BundleContext context) {
		// Connect a CogCharNetworkConfigAmbassador as a (no lifecycle) managed service, so lifter can use it
		ServiceLifecycleProvider ambassadorCycle =
				new SimpleLifecycle(new CogCharNetworkConfigAmbassador(), LiftAmbassador.LiftNetworkConfigInterface.class);
		OSGiComponent cncaComp = new OSGiComponent(context, ambassadorCycle);
		cncaComp.start();
	}	
}
