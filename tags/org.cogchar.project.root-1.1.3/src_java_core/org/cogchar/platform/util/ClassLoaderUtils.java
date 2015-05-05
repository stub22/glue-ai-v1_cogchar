/*
 *  Copyright 2012 by The Cogchar Project (www.cogchar.org).
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

package org.cogchar.platform.util;

import java.net.URL;
import java.util.List;

import org.osgi.framework.BundleContext;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class ClassLoaderUtils {

	public final static String RESOURCE_CLASSLOADER_TYPE = org.appdapter.core.boot.ClassLoaderUtils.RESOURCE_CLASSLOADER_TYPE;
	public final static String ALL_RESOURCE_CLASSLOADER_TYPES = org.appdapter.core.boot.ClassLoaderUtils.ALL_RESOURCE_CLASSLOADER_TYPES;

	public static URL findResourceURL(String path, List<ClassLoader> cLoaders) {
		return org.appdapter.core.boot.ClassLoaderUtils.findResourceURL(path, cLoaders);
	}

	public static ClassLoader findResourceClassLoader(String path, List<ClassLoader> cLoaders) {
		return org.appdapter.core.boot.ClassLoaderUtils.findResourceClassLoader(path, cLoaders);
	}

	public static void registerClassLoader(BundleContext context, ClassLoader loader, String resourceClassLoaderType) {
		org.appdapter.core.boot.ClassLoaderUtils.registerClassLoader(context, loader, resourceClassLoaderType);
	}

	public static List<ClassLoader> getFileResourceClassLoaders(BundleContext context, String resourceClassLoaderType) {
		return org.appdapter.core.boot.ClassLoaderUtils.getFileResourceClassLoaders(context, resourceClassLoaderType);
	}

}
