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

import java.util.Map;
import java.util.HashMap;

import java.util.logging.Logger;

/**
 * All SubsystemImpls are singletons, which are automatically placed in a registry that
 * can only be accessed by other SubsystemImpls.
 * @author Stu B. <www.texpedient.com>
 */
public abstract class SubsystemImpl {
	private static Logger	theLogger = Logger.getLogger(SubsystemImpl.class.getName());
	private	static		Map<Class, SubsystemImpl>	theSubsystemRegistry;
	private ClassLoader					myClassLoader;
	
	protected void registerSubsystem(SubsystemImpl subImpl) {
		if (theSubsystemRegistry == null) {
			theSubsystemRegistry = new HashMap<Class, SubsystemImpl>();
		}
		Class subsystemClass = subImpl.getClass();
		SubsystemImpl oldSubsys = theSubsystemRegistry.get(subsystemClass);
		if (oldSubsys != null) {
			throw new java.lang.RuntimeException("Tried to register over existing subsystem " + oldSubsys);
		}
		theSubsystemRegistry.put(subsystemClass, subImpl);
	}
	protected SubsystemImpl lookupSubsystem(Class c) {
		return theSubsystemRegistry.get(c);
	}
	protected SubsystemImpl() {
		registerSubsystem(this);
		myClassLoader = Thread.currentThread().getContextClassLoader();	
	}
	protected void blessCurrentThread() {
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
	/*
	public SubsystemImpl lookupPublicSubsystem(Class c) {
		return theSubsystemRegistry.get(c);
	}
	*/
}
