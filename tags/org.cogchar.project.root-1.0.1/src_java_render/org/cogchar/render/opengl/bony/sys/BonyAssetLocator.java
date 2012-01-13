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
import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetLocator;
import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetManager;
import java.io.IOException;

import com.jme3.asset.plugins.ClasspathLocator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * @author Stu B. <www.texpedient.com>
 */
public class BonyAssetLocator implements AssetLocator {
	static Logger theLogger = LoggerFactory.getLogger(BonyAssetLocator.class);
	ClasspathLocator  myDelegate = new ClasspathLocator();
	public BonyAssetLocator() { 
		theLogger.info("*************** BonyAssetLocator constructing");
	}
    @Override public void setRootPath(String rootPath) {
		theLogger.info("*************** setRootPath=" + rootPath);
		myDelegate.setRootPath(rootPath);
	}
    @Override public AssetInfo locate(AssetManager manager, AssetKey key) {
		theLogger.info("************** looking for key: " + key + ", TCCL=" + Thread.currentThread().getContextClassLoader());
		AssetInfo result = myDelegate.locate(manager, key);
		theLogger.info("************** result: " + result);
		return result;
		//  return UrlAssetInfo.create(manager, key, url);
	}
}
