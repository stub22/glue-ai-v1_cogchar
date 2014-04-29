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

package org.cogchar.app.puma.web;

import org.appdapter.core.log.BasicDebugger;

import com.hp.hpl.jena.query.Dataset;
import org.appdapter.core.name.FreeIdent;
import org.appdapter.core.name.Ident;


import org.cogchar.app.puma.boot.PumaAppContext;
import org.cogchar.app.puma.config.PumaContextMediator;
import org.cogchar.app.puma.web.PumaWebMapper;
import org.appdapter.core.matdat.OnlineSheetRepoSpec;
import org.appdapter.core.matdat.RepoSpec;
import org.cogchar.name.entity.EntityRoleCN;

import org.osgi.framework.BundleContext;

// import net.liftweb.http.LiftRules;


/**
 * @author Stu B. <www.texpedient.com>
 */

public class MockWebAppLauncher extends BasicDebugger {

	public void initWebapps(BundleContext context) {
		// Since we are not running PumaBooter, we must at least start the query service to get sheet-based config going
		PumaContextMediator mediator = new RepoPumaMediator();
		String roleShortName = "pumaCtx_FrienduRepo";
		Ident ctxID = new FreeIdent(EntityRoleCN.RKRT_NS_PREFIX + roleShortName, roleShortName);		
		PumaAppContext pac = new PumaAppContext(context, mediator, ctxID);
		pac.startRepositoryConfigServices();
		// ... and set our app context with PumaWebMapper, so lift can issue repo update requests
		PumaWebMapper pwm = pac.getOrMakeWebMapper();	
		pwm.connectLiftInterface(context);
		// Tell the lifter lifecycle to start, once its OSGi dependencies are satisfied
		pwm.startLifterLifecycle(context);
	}
	
	private static class RepoPumaMediator extends PumaContextMediator {
		// Override base class methods to customize the way that PUMA boots + runs, and
		// to receive notifications of progress during the boot / re-boot process.
	
		String TEST_REPO_SHEET_KEY = "0AmvzRRq-Hhz7dFVpSDFaaHhMWmVPRFl4RllXSHVxb2c";   	// GluePuma_HRKR25_TestFull
		// String TEST_REPO_SHEET_KEY = "0ArBjkBoH40tndDdsVEVHZXhVRHFETTB5MGhGcWFmeGc"; // GluePuma_HRKR50_TestFull
		int  DFLT_NAMESPACE_SHEET_NUM = 9;
		int   DFLT_DIRECTORY_SHEET_NUM = 8;
	
			
		
		@Override public RepoSpec getMainConfigRepoSpec() {
			java.util.List<ClassLoader> fileResModelCLs = new java.util.ArrayList<ClassLoader>();
			return new OnlineSheetRepoSpec(TEST_REPO_SHEET_KEY, DFLT_NAMESPACE_SHEET_NUM, DFLT_DIRECTORY_SHEET_NUM,
							fileResModelCLs);
		}
	}
}
