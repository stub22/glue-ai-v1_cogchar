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


package org.cogchar.animoid.oldconfig;

import	java.io.FileReader;
import	java.io.LineNumberReader;

import java.util.List;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * @author Stu Baurmann
 */

public class StringMatrixFuncs {
	private static Logger	theLogger = LoggerFactory.getLogger(StringMatrixFuncs.class);

	// This works for both VSA export files, and for ServoConfig files
	public static String[][]	readDataIntoMatrix(LineNumberReader lnr) throws Throwable {
		String[][] result = null;
		
		List<String []>	rowList = new ArrayList<String []> ();
		String line;
		do {
			int lineNumber = lnr.getLineNumber();
			line = lnr.readLine();
			theLogger.debug("Got line # " + lineNumber + " : " + line);
			if (line != null) {
				if (!line.startsWith("#")) {
					// Split on commas, which may optionally be accompanied by space on either side.
					String tokens[] = line.split("\\s*,\\s*");
					theLogger.debug("Got " + tokens.length + " tokens");
					rowList.add(tokens);
				} else {
					theLogger.debug("Skipping comment line");
				}
			}
		} while (line != null);
		result = new String[rowList.size()][];
		rowList.toArray(result);
		return result;
	}
	
	public static boolean verifyMatrixWidth(String[][] matrix, int expectedColumnCount) {
		for (int i = 0; i < matrix.length; i++) {
			if (matrix[i].length != expectedColumnCount) {
				theLogger.error("Expected " + expectedColumnCount + " columns, but found " + matrix[i].length
							+ " columns in row " + i);
				return false;
			}
		}
		return true;
	}
	public static String[][] readAndVerifyMatrixFile(String filename) throws Throwable {
		FileReader	fr = new FileReader(filename);
		LineNumberReader lnr = new LineNumberReader(fr);
		String [][] matrix = StringMatrixFuncs.readDataIntoMatrix(lnr);
		lnr.close();
		fr.close();
		int numColumns = matrix[0].length;
		if (numColumns > 0) {
			boolean widthOK = StringMatrixFuncs.verifyMatrixWidth (matrix, numColumns);
			if (!widthOK) {
				throw new Exception("File " + filename + " contains rows not matching expected width: " + numColumns);
			}
		}
		theLogger.trace("Read String[][] matrix of " + matrix.length + " rows, and verified column width");
		return matrix;
	}
}
