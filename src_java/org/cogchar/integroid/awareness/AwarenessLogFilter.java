/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cogchar.integroid.awareness;

import java.util.logging.Filter;
import java.util.logging.LogRecord;

/**
 *
 * @author Stu B. <www.texpedient.com>
 */
public class AwarenessLogFilter implements Filter {

	public boolean isLoggable(LogRecord record) {
		String loggerName = record.getLoggerName();
		String loggerMsg = record.getMessage();
		String loggerClass = record.getSourceClassName();
		// System.out.println("AwarenessLogFilter checking loggerName: " + loggerName);
		if (loggerName.equals(AwarenessHelpFuncs.class.getName())) {
			return true;
		}
		return false;
	}

}
