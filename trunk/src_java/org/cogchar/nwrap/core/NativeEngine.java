/**
 * Copyright 2008 Hanson Robotics Inc.
 * All Rights Reserved.
 */
package org.cogchar.nwrap.core;

/**
 *
 * @author humankind
 */
public abstract class NativeEngine {

	static {
		System.loadLibrary("RobotControlJNIWrapper");
	}

	public NativeEngine() {
	}

	public abstract void shutdown();

	public abstract void startup();
}
