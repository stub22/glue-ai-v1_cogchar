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
import org.cogchar.api.event.Notifier;
import org.cogchar.api.event.Event;
/**
 * A Performance performs some Media, over some Time-space.  
 * It can be scheduled to start, pause, resume, and stop at given times.
 * It is controlled through Actions, and reports on itself through State.
 * Performance does not itself promise to be a Notifier, but BasicPerformance does.
 * @param MediaType Type of Media the Performance uses
 * @param WorldTime Time unit used within this Performance
 * @author Stu B. <www.texpedient.com>
 * 
 * See Also:  Appdapter Module.State.
 */
public interface Performance<Cursor, MediaType extends Media<Cursor>, WorldTime> // ,  C extends Channel<M, Time, C>> //, P extends Performance<M, Time, C, P>> //, E extends Event<Performance<M, Time, C, E>, Time>> 
								//extends Notifier<Performance<M, Time, C, E>, Time, E> {
{
	public  class Instruction<Cur> {
		public	Kind	myKind;
		public	Cur		myCursor;
		
		public enum Kind {
			CUE,
			PLAY,
			PAUSE,  
			STOP
		}
		
	}
	
	public enum State {
		INITING,
		CUEING,		
		PLAYING,
		PAUSING,
		STOPPING
	}
    /**
     * Returns the Media used by this Performance.
     * @return Media used by this Performance
     */
	public	MediaType						getMedia();
    
    /**
     * Returns the Channel which created this Performance.
     * @return Channel which created this Performance
     */
	//public	Channel<Cursor, MediaType, WorldTime>		getChannel();
	public	PerfChannel				getChannel();
	
    /**
     * Returns the current State of this Performance.
     * @return current State of this Performance
     */
	public State	getState();
	
	public Cursor getCursor();

	
    /**
     * Attempts to schedule the given Action at the desired Time.
     * @param action Action to be scheduled
     * @param t desired time to perform the Action
     * @return true if successful
     */
	public boolean attemptToScheduleInstruction(WorldTime t, Instruction instruct);
	
	/**
	 * A TextPerf is a Performance constrained to use Text Media.
	 * @param WorldTime 
	 */
	
	public interface TextPerf<Cursor, M extends Media.Text<Cursor>, WorldTime> 	extends Performance<Cursor, M, WorldTime> {	
	}
	/**
	 * A FramePerf is a Performance constrained to use Framed Media.
	 * @param WorldTime
	 * @param <F> 
	 */
	public interface FramedPerf<F, Cursor, WorldTime> 	extends Performance<Cursor, Media.Framed<F, Cursor>, WorldTime> {	
	}


}
