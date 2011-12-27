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

import java.net.URL;

import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * @author Stu B. <www.texpedient.com>
 */
public class JmonkeyAssetLoader {
	static Logger theLogger = LoggerFactory.getLogger(JmonkeyAssetLoader.class);
	private AssetManager	myAssetMgr;
	private ClassLoader		mySavedClassLoader;
	private	Class			myResourceMarkerClass;
	private URL				myHackyRootURL;
	private	Bundle			myHackyBundle;
	
	public JmonkeyAssetLoader(Class resourceMarkerClass) {
		myResourceMarkerClass = resourceMarkerClass;
	}

	public void setAssetManager(AssetManager assetMgr) {
		myAssetMgr = assetMgr;
	}
	public AssetManager getAssetManager() { 
		return myAssetMgr;
	}
	public Spatial	loadModel(String modelName) {
		return myAssetMgr.loadModel(modelName);
	}
	public BitmapFont loadFont(String fontPath) {
		return myAssetMgr.loadFont(fontPath);
	}
	public ClassLoader getClassLoader() { 
		return myResourceMarkerClass.getClassLoader();
	}
	public void resolve() { 
		
	}
	public void setHackyRootURL(URL url) { 
		myHackyRootURL = url;
	}
	public URL getHackyRootURL() { 
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
}
