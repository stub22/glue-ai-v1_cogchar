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
package org.cogchar.bundle.render.resources;
import com.jme3.asset.AssetManager;
import java.net.URL;
import org.cogchar.render.opengl.bony.sys.JmonkeyAssetLocation;

import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * @author Stu B. <www.texpedient.com>
 */
public class ResourceLoader extends JmonkeyAssetLocation {
	static Logger theLogger = LoggerFactory.getLogger(ResourceLoader.class);
	public ResourceLoader() {
		super(ResourceBundleActivator.class);
	}
	@Override public void resolve() { 
		setHackyRootURL(ResourceBundleActivator.theBundleRootURL);
		theLogger.info("******************** ResourceLoader resolved with URL: " + getHackyRootURL());
		Bundle hackyBundle = ResourceBundleActivator.theInstance.getBundle();
		setHackyBundle(hackyBundle);
	}
}
