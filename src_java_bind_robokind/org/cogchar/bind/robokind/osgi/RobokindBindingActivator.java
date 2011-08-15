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

package org.cogchar.bind.robokind.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Stu B. <www.texpedient.com>
 */
public class RobokindBindingActivator implements BundleActivator {
	static Logger theLogger = LoggerFactory.getLogger(RobokindBindingActivator.class);
	@Override public void start(BundleContext context) throws Exception {
		theLogger.info(getClass().getCanonicalName() + ".start(ctx=" + context + ")BEGIN-[");
        // org.appdapter.gui.demo.DemoBrowser.main(null);
		theLogger.info(getClass().getCanonicalName() + ".start(ctx=" + context + ")]-END");
    }

    @Override public void stop(BundleContext context) throws Exception {
		theLogger.info(getClass().getCanonicalName() + ".stop(ctx=" + context + ")BEGIN-[");
          // TODO add deactivation code here
		theLogger.info(getClass().getCanonicalName() + ".stop(ctx=" + context + ")]-END");
    }
}
