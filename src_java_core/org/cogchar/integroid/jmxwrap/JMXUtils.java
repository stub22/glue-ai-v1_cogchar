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

package org.cogchar.integroid.jmxwrap;

import java.util.Iterator;
import java.util.Set;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

/**
 *
 * @author Stu Baurmann
 */
public class JMXUtils {
	// Useful for debugging what's going on with 
	public  static void dumpMBeanServerInfo(MBeanServerConnection mbsc) throws Throwable {
		// Get domains from MBeanServer
		echo("\nDomains:");
		String domains[] = mbsc.getDomains();
		for (int i = 0; i < domains.length; i++) {
			echo("\tDomain[" + i + "] = " + domains[i]);
		}
		echo("\nMBean count = " + mbsc.getMBeanCount());

		echo("\nQuery MBeanServer MBeans:");
		Set names = mbsc.queryNames(null, null);
		for (Iterator i = names.iterator(); i.hasNext(); ) {
			echo("\tObjectName = " + (ObjectName) i.next());
		}		
	}
    private static void echo(String msg) {
		System.out.println(msg);
    }	
}
