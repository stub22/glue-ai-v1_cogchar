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

	public	M						getMedia();
	public	Channel<M, Time>		getChannel();
	
	public State	getState();
	
	public boolean attemptToScheduleAction(Action action, Time t);
	
	public interface TextPerf<Time> 	extends Performance<Media.Text, Time> {	
	}
	public interface FramedPerf<Time, F> 	extends Performance<Media.Framed<F>, Time> {	
	}


}
