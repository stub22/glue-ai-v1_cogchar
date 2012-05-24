/*
 * StringParser.java
 * 
 * Created on Aug 29, 2007, 12:24:56 PM
 * 
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.cogchar.ancient.utility;

/**
 *
 * @author Stu B.  <www.texpedient.com>
 */
public class StringParser {

	private String m_remainder;
	private char m_delimiter;

	public StringParser(char delimiter) {
		m_delimiter = delimiter;
	}

	//
	// Return the first trimmed substring of line that is delimited by delimiter.
	// Empty strings are ignored and return null.
	// In the absence of the delimiter return the remainder of the line
	// Side effect: Modify line to be the remaining trimmed text following the delimiter
	//
	public String popDelimitedStringChunk(String line) {
		String segment;
		m_remainder = line;
		int delimiterIndex = line.indexOf(m_delimiter);

		if (delimiterIndex < 0) // If no delimiter then return
		{
			segment = line.trim();
			if (segment.length() == 0) {
				segment = null;
			}
			m_remainder = "";
			return segment;
		}
		segment = line.substring(0, delimiterIndex).trim();
		if (segment.length() == 0) {
			segment = null;

		//
		// Side effect. Consume the segment and delimiter from the line
		//
		}
		m_remainder = line.substring(delimiterIndex + 1).trim();
		return segment;
	}

	public String getRemainder() {
		return m_remainder;
	}
}
