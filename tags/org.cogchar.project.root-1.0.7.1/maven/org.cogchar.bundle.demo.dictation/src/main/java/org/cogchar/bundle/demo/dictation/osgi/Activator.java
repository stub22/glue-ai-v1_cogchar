/*
 * Copyright 2012 by The Cogchar Project (www.cogchar.org).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cogchar.bundle.demo.dictation.osgi;

import org.cogchar.bundle.demo.dictation.ui.DictationFrame;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/***
 * 
 * @author Matthew Stevenson <www.robokind.org>
 */
public class Activator implements BundleActivator {

    @Override
    public void start(BundleContext context) throws Exception {
        //DictationFrame.main(null);
    }
    
    @Override
    public void stop(BundleContext context) throws Exception {
        // TODO add deactivation code here
    }

}
