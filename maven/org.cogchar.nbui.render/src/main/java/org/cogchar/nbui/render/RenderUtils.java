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

package org.cogchar.nbui.render;

import org.cogchar.render.opengl.bony.app.BonyStickFigureApp;
import org.cogchar.render.opengl.bony.app.BonyVirtualCharApp;
import org.cogchar.render.opengl.bony.sys.BonyContext;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Stu B. <www.texpedient.org>
 * @author Matthew Stevenson <www.robokind.org>
 */
public class RenderUtils {
	static Logger theLogger = LoggerFactory.getLogger(RenderUtils.class);
    
    public static synchronized void initOpenGLCanvas(BonyContext bc){
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        try {
            // Must set context classloader so that JMonkey can find goodies
            // on the classpath, currently presumed to be in same class space
            // as the BonyContext class.  (Could generalize this and make
            // it use the loader of a configured bundle).
            ClassLoader bonyLoader = bc.getClass().getClassLoader();
            theLogger.info("Setting thread class loader to bony loader: " + bonyLoader);
            Thread.currentThread().setContextClassLoader(bonyLoader);
            
            BonyVirtualCharApp app = bc.getApp();
            if(app.isCanvasStarted()){
                return;
            }
            app.startJMonkeyCanvas();
            ((BonyStickFigureApp) app).setScoringFlag(true);
        } finally {
            theLogger.info("Restoring old class loader: " + tccl);
            Thread.currentThread().setContextClassLoader(tccl);
        }
    }
	
	public static BonyContext getBonyContext(BundleContext bundleCtx) {
		ServiceReference ref = bundleCtx.getServiceReference(BonyContext.class.getName());
		if(ref == null){
			return null;
		}
		return (BonyContext) bundleCtx.getService(ref);
	}
}
