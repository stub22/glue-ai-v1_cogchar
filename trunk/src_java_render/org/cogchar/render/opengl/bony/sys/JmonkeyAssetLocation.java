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
package org.cogchar.render.opengl.bony.sys;
import com.jme3.asset.AssetManager;
import com.jme3.scene.Spatial;
import com.jme3.font.BitmapFont;
import com.jme3.asset.plugins.UrlLocator;
import java.net.URL;

import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * @author Stu B. <www.texpedient.com>
 */
public class JmonkeyAssetLocation {
	static Logger theLogger = LoggerFactory.getLogger(JmonkeyAssetLocation.class);
	private AssetManager	myAssetMgr;
	private ClassLoader		mySavedClassLoader;
	private	Class			myResourceMarkerClass;
	private URL				myHackyRootURL;
	private	Bundle			myHackyBundle;
	
	public JmonkeyAssetLocation(Class resourceMarkerClass) {
		myResourceMarkerClass = resourceMarkerClass;
	}

	public void setAssetManager(AssetManager assetMgr) {
		myAssetMgr = assetMgr;
	}
	public AssetManager getAssetManager() { 
		return myAssetMgr;
	}
	/*
	public Spatial	loadModel(String modelName) {
		return myAssetMgr.loadModel(modelName);
	}
	public BitmapFont loadFont(String fontPath) {
		return myAssetMgr.loadFont(fontPath);
	}
	 * 
	 */
	public ClassLoader getClassLoader() { 
		return myResourceMarkerClass.getClassLoader();
	}
	public void resolve() { 

	}
	public void registerLocators(AssetManager assetMgr) { 
		URL hackyRootURL = getHackyRootURL();
		if (hackyRootURL != null) {
			String hackyRootUrlPath = hackyRootURL.toExternalForm();
			theLogger.info("Registering UrlLocator for path: " + hackyRootUrlPath);
			assetMgr.registerLocator(hackyRootUrlPath, UrlLocator.class);
		} else {
			theLogger.warn("Cannot find URL for resources of: " + this);
		}
		
	}
	public void setHackyRootURL(URL url) { 
		myHackyRootURL = url;
	}
	public URL getHackyRootURL() {
		String resourceRootPath = "/";
		if (myHackyRootURL == null) {
			ClassLoader cl = getClassLoader();
			
			URL resURL = cl.getResource(resourceRootPath);
			theLogger.info("ClassLoader[" + cl + "]" + " lookup for resourcePath[" + resourceRootPath + "] returned: " + resURL);
			/*
			if (resURL == null) {
				theLogger.info("Specific lookup failed, trying for systemResource");
				resURL = ClassLoader.getSystemResource("/");
			}
			 * 
			 */
			myHackyRootURL = resURL;
		}
		return myHackyRootURL;
	}
	public Bundle getHackyBundle() { 
		return myHackyBundle;
	}
	public void setHackyBundle(Bundle b) { 
		myHackyBundle = b;
	}

	public void installClassLoader(boolean verbose)  {
		mySavedClassLoader = Thread.currentThread().getContextClassLoader();
		if (verbose) {
			theLogger.info("Saved old class loader: " + mySavedClassLoader);
		}
		try {
			ClassLoader localLoader = myResourceMarkerClass.getClassLoader();
			if (verbose) {
				theLogger.info("Setting thread class loader to local loader: " + localLoader);
			}
			Thread.currentThread().setContextClassLoader(localLoader);
		} catch (Throwable t) {
			theLogger.error("problem in installClassLoader", t);
		}
	}
	public void restoreClassLoader()  {
		Thread.currentThread().setContextClassLoader(mySavedClassLoader); 
	}
	/*  This doesn't work unless we are sure there are no "Common/..." resources
	 * on another classLoader, which may be needed by our model.  Don't trust
	 * jME3 warnings about "can't locate X" <- this may mean "can't locate
	 * something that inferred (possibly hardcoded)  sub-reqt of X", e.g.
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
	@Override public String toString() {
		String desc = "";
		if (myResourceMarkerClass != null) {
			desc = myResourceMarkerClass.getName();
		}
		return "JmonkeyAssetLocation[" + desc + "]";
	}
}
