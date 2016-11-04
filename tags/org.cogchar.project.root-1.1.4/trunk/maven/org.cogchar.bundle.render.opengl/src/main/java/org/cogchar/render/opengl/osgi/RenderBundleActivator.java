/*
 *  Copyright 2011 by The Cogchar Project (www.cogchar.org).
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
package org.cogchar.render.opengl.osgi;

import org.appdapter.osgi.core.BundleActivatorBase;

import org.osgi.framework.BundleContext;


/**
 * @author Stu B. <www.texpedient.com>
 */
public class RenderBundleActivator extends BundleActivatorBase {


	@Override public void start(BundleContext bundleCtx) throws Exception {
		
		super.start(bundleCtx);
		
		// Now done later by PUMA or other.
		// String panelKind = "SLIM";
		// RenderBundleUtils.buildBonyRenderContextInOSGi(bundleCtx, panelKind);
	
		logInfo("******************* start() is DONE!");
	}

	@Override public void stop(BundleContext bundleCtx) throws Exception {
		// Perhaps this should be done last, via a "finally" clause.
		super.stop(bundleCtx);
		RenderBundleUtils.shutdownBonyRenderContextInOSGi(bundleCtx);
		logInfo("stop() is DONE!");
	}
}
