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
package org.cogchar.render.sys.asset;
import com.jme3.asset.AssetManager;
import com.jme3.scene.Spatial;
import com.jme3.font.BitmapFont;
import com.jme3.asset.plugins.UrlLocator;
import java.net.URL;

import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Represents a classloader asset source that we want to access through the JME3 
 *  UrlLocator mechanism, usually in an OSGi context.  Pass any well known class
 *  from that ClassLoader to the constructor of this object, and then register
 *  this JmonkeyAssetLocation with an AssetContext.  That registration should be
 *  done before CogcharRenderContext.completeInit(), which turns this object 
 *  into an actual registered JME3 UrlLocator.
 * 
 * @author Stu B. <www.texpedient.com>
 */
public class JmonkeyAssetLocation {
	static Logger theLogger = LoggerFactory.getLogger(JmonkeyAssetLocation.class);

	private	Class			myResourceMarkerClass;
	private URL				myHackyRootURL;
	// TODO:  Turn into a set of AssetManagers we are known to be registred with.
	private	boolean			myRegisteredFlag = false;
	
	public JmonkeyAssetLocation(Class resourceMarkerClass) {
		myResourceMarkerClass = resourceMarkerClass;
	}

	public ClassLoader getClassLoader() { 
		return myResourceMarkerClass.getClassLoader();
	}
	public void resolve() { 
		theLogger.info("resolve() by default does nothing.");
	}
	public void ensurerLocatorsReged(AssetManager assetMgr) {
		// TODO : Check THIS assetMgr for an equiv locator instead of relying on this single flag.
		if (!myRegisteredFlag) {
			URL hackyRootURL = getHackyRootURL();
			if (hackyRootURL != null) {
				String hackyRootUrlPath = hackyRootURL.toExternalForm();
				theLogger.info("Registering UrlLocator for path: {}", hackyRootUrlPath);
				assetMgr.registerLocator(hackyRootUrlPath, UrlLocator.class);
				myRegisteredFlag = true;
			} else {
				theLogger.warn("Cannot find URL for resources of: {}", this);
			}
		} else {
			theLogger.warn("Ignoring registration request for locator already registered: {}", this);
		}
		
	}

	public URL getHackyRootURL() {
		
		if (myHackyRootURL == null) {
			String resourceRootPath = "/";
			ClassLoader cl = getClassLoader();
			
			URL resURL = cl.getResource(resourceRootPath);
			theLogger.info("ClassLoader[{}] lookup for resourcePath[{}] returned: {}", new Object[]{cl, resourceRootPath , resURL});
			myHackyRootURL = resURL;
		}
		return myHackyRootURL;
	}
	@Override public String toString() {
		String desc = "";
		if (myResourceMarkerClass != null) {
			desc = myResourceMarkerClass.getName();
		}
		return "JmonkeyAssetLocation[" + desc + "]";
	}	

	/* Old technique, kinda works, BUT, only when there are no foreign (i.e.
	 * satisfied by a different classloader) resource* references embedded 
	 * in the loaded assets, e.g. "Common/..." resources.    Don't trust
	 * jME3 warnings about "can't locate X"! <- This may mean "I found X,
	 * but I can't find inferred (possibly hardcoded)  sub-reqt of X", e.g.
	 * Common/MatDefs/Light/Lighting.j3md
	 * See:
		 http://jmonkeyengine.org/groups/general-2/forum/topic/successes-and-challenges-using-jme3-with-osgi-classpaths-and-native-libraries#post-156958
	 
	public Spatial safelyLoadModel(String modelName, boolean verbose) {
		Spatial result = null;
		installClassLoader(verbose);
		try {
			result = loadModel(modelName);
		} finally {
			restoreClassLoader();
			if (verbose) {
				theLogger.info("********** Finished loading model, restored classLoader to: " 
								+ Thread.currentThread().getContextClassLoader());
			}
		}
		return result;
	}
	 * 
	 */

}
