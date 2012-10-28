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

/**
 * @author Stu B. <www.texpedient.com>
 */

public class ClassLoaderUtils {
	public static URL findResourceURL (String path, List<ClassLoader> cLoaders) {
		for (ClassLoader cl : cLoaders) {
			// This method will first search the parent class loader for the resource; if the parent is null the path of
			// the class loader built-in to the virtual machine is searched. That failing, this method will invoke 
			// findResource(String) to find the resource.
			URL res = cl.getResource(path);
			if (res != null) {
				return res;
			}
		}
		return null;
	}
	public static ClassLoader findResourceClassLoader (String path, List<ClassLoader> cLoaders) {
		for (ClassLoader cl : cLoaders) {
			// This method will first search the parent class loader for the resource; if the parent is null the path of
			// the class loader built-in to the virtual machine is searched. That failing, this method will invoke 
			// findResource(String) to find the resource.
			URL res = cl.getResource(path);
			if (res != null) {
				return cl;
			}
		}
		return null;
	}	
}
