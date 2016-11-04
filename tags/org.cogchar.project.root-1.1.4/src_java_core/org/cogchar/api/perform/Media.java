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

package org.cogchar.api.perform;

/**
 * Interface to tag the type and format of a piece of data.
 * A "cursor" is a potentially-mutable position/pointer type for the media.
 * @author Stu B. <www.texpedient.com>
 */
public interface Media<Cursor> {
	
	public Cursor	getCursorBeforeStart();
	public Cursor	getCursorAfterEnd();
    /**
     * Text data in the form of a standard Java String
     */
	public interface Text<Cursor> extends Media<Cursor> {
        /**
         * Returns the Text data as a String.
         * @return Text data as a String
         */
		public	String	getFullText();
	}
    
    /**
     * Holds frames from a data stream.  Examples would be image frames from a
     * video stream, or audio data from an audio stream.
     * @param <F> the type of Frame used by this Framed Media
     */
	public interface Framed<F, Cursor> extends Media<Cursor> {
        /**
         * Returns the number of available frames.
         * @return number of available frames
         */
		public long	getFrameCount();
        /**
         * Returns the frame at the given index.
         * @param idx index of the frame to retrieve
         * @return frame at the given index
         */
		public F getFrameAtIndex (long idx);
	}
	public class ImmutableTextPosition {
		private int myPos;
		public ImmutableTextPosition(int pos) {
			myPos = pos;
		}
		int getTextPosition() { 
			return myPos;
		}
	}
	/**
     * Simple implementation of Text Media which wraps a String.
     */
	public class BasicText  implements Text<ImmutableTextPosition> {
		String	myTextString;
        
        /**
         * Creates a new BasicText from the given String
         * @param contents text to wrap as a BasicText
         */
		public BasicText(String contents) {
			myTextString = contents;
		}
        
        @Override
		public String getFullText() {
			return myTextString;
		}

		public ImmutableTextPosition getCursorBeforeStart() {
			return new ImmutableTextPosition(0);
		}

		public ImmutableTextPosition getCursorAfterEnd() {
			return new ImmutableTextPosition(myTextString.length());
		}
		
	}
}
