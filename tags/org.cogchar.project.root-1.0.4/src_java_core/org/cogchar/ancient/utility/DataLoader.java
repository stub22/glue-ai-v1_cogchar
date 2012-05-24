/*
 * DataLoader.java
 * 
 * Created on Nov 26, 2007, 11:30:02 AM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cogchar.ancient.utility;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import java.util.List;
import java.util.Set;

/**
 *
 * @author josh
 */
public class DataLoader {
	//
	// Remove quotes and trim
	//

	private static String cleanupString(String aString) {
		String cleaned = aString;
		if (cleaned != null) {
			cleaned = cleaned.trim();
			if (cleaned.length() > 0 && (cleaned.charAt(0) == '\"' || cleaned.charAt(0) == '\'')) {
				cleaned = cleaned.substring(1).trim();
			}
			if (cleaned.length() > 0 && (cleaned.charAt(cleaned.length() - 1) == '\"' || cleaned.charAt(cleaned.length() - 1) == '\'')) {
				cleaned = cleaned.substring(0, cleaned.length() - 1).trim();
			}
		}
		return cleaned;
	}
	public static List<String> loadStringsFromOneColumnOfTextFile(int columnNum,
		String filename,
		char delimiter,
		boolean toLowerCase) throws FileNotFoundException, IOException {
		
		List<String> resultList = new ArrayList<String>();
		
		BufferedReader file = new BufferedReader(new FileReader(filename));

		while (file.ready()) {
			String line = file.readLine().trim();
			String columnData = null;
			int columnStart = 0;
			for (int i = 0; i < columnNum; i++) {
				int delimIndex = line.indexOf(delimiter, columnStart);
				if (delimIndex >= 0) {
					if (delimIndex > columnStart) {
						columnData = line.substring(columnStart, delimIndex - 1).trim();
					} else {
						columnData = new String();
					}
					columnStart = delimIndex + 1;
				} else {
					columnData = line.substring(columnStart).trim();
					columnStart = line.length();
				}
			}
			if (columnData != null && columnData.length() > 0) {
				// Set new current utterance and save response
				columnData = cleanupString(columnData);
				if (toLowerCase) {
					columnData = columnData.toLowerCase();
				} 
				resultList.add(columnData);
			}
		}
		return resultList;
	}
	public static Set<String> loadSetOfStringsFromOneColumnOfTextFile(int columnNum,
		String filename,
		char delimiter,
		boolean toLowerCase) throws FileNotFoundException, IOException {
		
		List<String> sList = loadStringsFromOneColumnOfTextFile(columnNum, filename, delimiter, toLowerCase);
		Set<String> resultSet = new HashSet<String>(sList);
		return resultSet;
	}	

	public static boolean loadSimpleListFile(List<String> responseSet, String filename) 
				throws FileNotFoundException, IOException {
		BufferedReader file = new BufferedReader(new FileReader(filename));

		while (file.ready()) {
			String line = file.readLine().trim();
			if (line.length() > 0) // Set new current utterance and save response
			{
				responseSet.add(line);
			}
		}
		return true;
	}

	public static List<List<String>> loadTablularDataFile(String filename, char delimiter) 
				throws FileNotFoundException, IOException {
		
		List<List<String>> rows = new ArrayList<List<String>>();
		
		BufferedReader file = new BufferedReader(new FileReader(filename));

		while (file.ready()) {
			String line = file.readLine().trim();
			if (line.isEmpty()) {
				continue;
			}
			List<String> row = null;
			try {
				row = parseTabularLine(line, delimiter);
			} catch (Throwable t) {
				System.out.println("************ UNPARSABLE:  " + line);
				continue;
			}

			rows.add(row);
		}
		return rows;
	}

	/*** Umm, let alone XML, did we not know about StringTokenizer? */
	public static List<String> parseTabularLine(String line, char delimiter) throws Throwable {
		List<String> result = new ArrayList<String>();
		String cell;
		int delimIndex = 0;
		int columnStart = 0;
		while (delimIndex >= 0) {
			delimIndex = line.indexOf(delimiter, columnStart);
			if (delimIndex >= 0) {
				cell = line.substring(columnStart, delimIndex - columnStart);
				columnStart = delimIndex + 1;
			} else {
				cell = line.substring(columnStart);
				columnStart = line.length();
			}
			result.add(cell.trim());
		}
		return result;
	}
}
