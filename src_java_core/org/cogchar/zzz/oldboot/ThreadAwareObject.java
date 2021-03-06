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

package org.cogchar.zzz.oldboot;

/**
 *
 * @author Stu B. <www.texpedient.com>
 */
public class ThreadAwareObject {
	private ClassLoader					myClassLoader;
	public ThreadAwareObject() {
		myClassLoader = Thread.currentThread().getContextClassLoader();	
	}
	public void blessCurrentThread() {
		/*
		// Callback from JNI does not have a contextClassLoader.
		// This is an example of why our JNI interface needs to be narrower - so
		// we can enforce aspects at the boundary.
		// Without this classLoader, an exception will be thrown in the
		// BeanAdapterFactory when it tries to update some columns of the Jobs JTable
		// (on the thalamus tab).
		*/
		Thread.currentThread().setContextClassLoader(myClassLoader);
	}
}
