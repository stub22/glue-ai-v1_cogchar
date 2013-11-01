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

import java.lang.management.ManagementFactory;
import java.util.logging.Logger;
import javax.management.AttributeChangeNotification;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;
import org.cogchar.platform.util.TimeUtils;

/**
 *
 * @author Stu Baurmann
 */
public class NotifyingBeanImpl  extends NotificationBroadcasterSupport   {
	private static Logger	theLogger = Logger.getLogger(NotifyingBeanImpl.class.getName());

	protected		ObjectName		myObjectName;
	private			long			mySequenceNumber = 1;
	
	public NotifyingBeanImpl(ObjectName on) {
		myObjectName = on;
	}
	protected void register() throws Throwable {
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		mbs.registerMBean(this, myObjectName);
	}
	protected synchronized void sendAttributeChangeNotification(String message, 
				String attribName, Object oldAttribVal, Object newAttribVal) {
		
		String attribTypeName = null;
		if (newAttribVal != null) {
			attribTypeName = newAttribVal.getClass().getName();
		}
		long currentTimeMillis = TimeUtils.currentTimeMillis();
		theLogger.finer("sendAttributeChangeNotification starting at " + currentTimeMillis);
		
		Notification n = new AttributeChangeNotification(myObjectName,
					    mySequenceNumber ++,
						currentTimeMillis,
						message, attribName, attribTypeName,
						oldAttribVal, newAttribVal);
		// This will execute synch (blocking this thread!) or async, depending on the
		// constructor params to the base class.  Default is synch.
		// But...when we say "execute", we're actually delegating to a connector to
		// deliver the socket messages? Izzat right?
		sendNotification(n);	
		currentTimeMillis = TimeUtils.currentTimeMillis();
		theLogger.finer("sendAttributeChangeNotification finished at " + currentTimeMillis);
	}
    @Override
	public MBeanNotificationInfo[] getNotificationInfo() {
		String[] types = new String[]{
			AttributeChangeNotification.ATTRIBUTE_CHANGE
		};

		String name = AttributeChangeNotification.class.getName();
		String description = "A SNAZZY attribute of this AWESOME  MBean has changed";
		MBeanNotificationInfo info =
			new MBeanNotificationInfo(types, name, description);
		return new MBeanNotificationInfo[]{info};
    }
	
}
