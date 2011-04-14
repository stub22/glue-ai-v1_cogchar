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
 *
 * If we export the first 32 channels of a VSA file,  in "sparse" format, WITH initial positions, 
 * the data looks like this:
 * 
83, 130, 170, 127, 68, 115, 130, 73, 129, 131, 115, 202, 125, 182, 135, 110, 125, 103, 70, 56, 125, 170, 122, 119, 125, 114, 103, 130, 127, 97, 87, 114, 
-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 110, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 107, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 104, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 

 * 
 * 
 * @author Stu Baurmann
 */
public class IntMatrixFuncs {
	private static Logger	theLogger = LoggerFactory.getLogger(IntMatrixFuncs.class.getName());

	// This works for both VSA export files, and for ServoConfig files
	public static int[][]	readDataIntoMatrix(LineNumberReader lnr) throws Throwable {
		int[][] result = null;
		
		List<int []>	rowList = new ArrayList<int []> ();
		String line;
		do {
			int lineNumber = lnr.getLineNumber();
			line = lnr.readLine();
			theLogger.debug("Got line # " + lineNumber + " : " + line);
			if ((line != null) && (line.trim().length() > 0)) {
				if (!line.startsWith("#")) {
					String tokens[] = line.split("\\s*,\\s*");
					theLogger.debug("Got " + tokens.length + " tokens");
					int[] row = new int[tokens.length];
					for (int i = 0; i < tokens.length; i++) {
						int	parsed = Integer.parseInt(tokens[i]);
						row[i] = parsed;
					}
					rowList.add(row);
				} else {
					theLogger.debug("Skipping comment line");
				}
			}
		} while (line != null);
		result = new int[rowList.size()][];
		rowList.toArray(result);
		return result;
	}
	
	public static boolean verifyMatrixWidth(int[][] matrix, int expectedColumnCount) {
		for (int i = 0; i < matrix.length; i++) {
			if (matrix[i].length != expectedColumnCount) {
				theLogger.error("Expected " + expectedColumnCount + " columns, but found " + matrix[i].length
							+ " columns in row " + i);
				return false;
			}
		}
		return true;
	}
	public static int[][] readAndVerifyMatrixFile(String filename, int numColumns) throws Throwable {
		FileReader	fr = new FileReader(filename);
		LineNumberReader lnr = new LineNumberReader(fr);
		int [][] matrix = IntMatrixFuncs.readDataIntoMatrix(lnr);
		lnr.close();
		fr.close();
		if (numColumns > 0) {
			boolean widthOK = IntMatrixFuncs.verifyMatrixWidth (matrix, numColumns);
			if (!widthOK) {
				throw new Exception("File " + filename + " contains rows not matching expected width: " + numColumns);
			}
		}
		theLogger.trace("Read int[][] matrix of " + matrix.length + " rows, and verified column width");
		return matrix;
	}
}
