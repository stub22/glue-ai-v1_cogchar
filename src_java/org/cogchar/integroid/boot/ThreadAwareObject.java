/*
 *  Copyright 2008 Hanson Robotics Inc.
 *  All Rights Reserved.
 */

package org.cogchar.integroid.boot;

/**
 *
 * @author Stu Baurmann
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
