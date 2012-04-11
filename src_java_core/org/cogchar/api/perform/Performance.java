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
import org.appdapter.api.module.Module.State;
/**
 * A Performance acts on some Media.  It can be scheduled to start, pause, 
 * resume, and stop at given times.
 * @param <M> Type of Media the Performance acts upon
 * @param <Time> Time unit used within this Performance
 * @author Stu B. <www.texpedient.com>
 */
public interface Performance<M extends Media, Time> // ,  C extends Channel<M, Time, C>> //, P extends Performance<M, Time, C, P>> //, E extends Event<Performance<M, Time, C, E>, Time>> 
								//extends Notifier<Performance<M, Time, C, E>, Time, E> {
{
	public enum Action {
		START, 
		PAUSE, 
		RESUME, 
		STOP
	}
    /**
     * Returns the Media used by this Performance.
     * @return Media used by this Performance
     */
	public	M						getMedia();
    
    /**
     * Returns the Channel which created this Performance.
     * @return Channel which created this Performance
     */
	public	Channel<M, Time>		getChannel();
	
    /**
     * Returns the current State of this Performance.
     * @return current State of this Performance
     */
	public State	getState();
	
    /**
     * Attempts to schedule the given Action at the desired Time.
     * @param action Action to be scheduled
     * @param t desired time to perform the Action
     * @return true if successful
     */
	public boolean attemptToScheduleAction(Action action, Time t);
	
	public interface TextPerf<Time> 	extends Performance<Media.Text, Time> {	
	}
	public interface FramedPerf<Time, F> 	extends Performance<Media.Framed<F>, Time> {	
	}


}
