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

package org.cogchar.ext.bundle.opengl.jmonkey;

import org.lwjgl.LWJGLUtil;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class NativeLibUtils {
	public static void testNativeLoad() { 
		System.out.println("NativeLibUtils: os.arch=" + System.getProperty("os.arch"));
		System.out.println("NativeLibUtils: org.lwjgl.librarypath=" + System.getProperty("org.lwjgl.librarypath"));
		System.out.println("NativeLibUtils: java.library.path=" + System.getProperty("java.library.path"));			
		// System.out.println("NativeLibUtils: All properties=" + System.getProperties());
	}
}
/*
 *   [java] java.lang.UnsatisfiedLinkError: Can't load library: E:\_mount\friendularity_trunk\maven\org.friendularity.bundle.demo.ccrk\lwjgl.dll
     [java] 	at java.lang.ClassLoader.loadLibrary(ClassLoader.java:1702)
     [java] 	at java.lang.Runtime.load0(Runtime.java:770)
     [java] 	at java.lang.System.load(System.java:1003)
     [java] 	at org.lwjgl.Sys$1.run(Sys.java:70)
     [java] 	at java.security.AccessController.doPrivileged(Native Method)
     [java] 	at org.lwjgl.Sys.doLoadLibrary(Sys.java:66)
     [java] 	at org.lwjgl.Sys.loadLibrary(Sys.java:95)
     [java] 	at org.lwjgl.Sys.<clinit>(Sys.java:112)
     [java] 	at com.jme3.system.lwjgl.LwjglAbstractDisplay.run(LwjglAbstractDisplay.java:204)
     [java] 	at java.lang.Thread.run(Thread.java:619)
	 * 
	 * 
	 * Source (of a reasonably close version) is here:
	 * 
	 * http://java-game-lib.svn.sourceforge.net/viewvc/java-game-lib/trunk/LWJGL/src/java/org/lwjgl/Sys.java?revision=3731&view=markup
	 * 
	 * 
	 * com.jme3.system.Natives is forcing in a value 
	 * in  extractNativeLibs(Platform platform, AppSettings settings) 
	 *  Line 216 :
	 * 
	 *         String libraryPath = getExtractionDir().toString();
		if (needLWJGL) //  true f (renderer.startsWith("LWJGL")) {{
            logger.log(Level.INFO, "Extraction Directory: {0}", getExtractionDir().toString());

            // LWJGL supports this feature where
            // it can load libraries from this path.
            System.setProperty("org.lwjgl.librarypath", libraryPath);
 * 
 * 
 * 
 *  isLowPermissions=true (via WorkaroundFuncs.makeAWTCanvas gives same result
 * 
 *      [java] 	at java.lang.System.load(System.java:1003)
     [java] 	at org.lwjgl.Sys$1.run(Sys.java:70)
     [java] 	at java.security.AccessController.doPrivileged(Native Method)
     [java] 	at org.lwjgl.Sys.doLoadLibrary(Sys.java:66)
     [java] 	at org.lwjgl.Sys.loadLibrary(Sys.java:95)
 */
