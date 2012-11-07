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

import org.appdapter.core.name.Ident;
import java.util.List;

/**
 * A Channel produces Performances from a given Media type.
 * 
 * @param <M> Type of Media used by this Channel
 * @param <Time> Time unit used by the channel for timekeeping and scheduling
 * @author Stu B. <www.texpedient.com>
 */
public interface Channel<M extends Media, Time> // , C extends Channel<M, Time, C>> {
{
    public enum Status {
        /**
         * Channel is initializing and not ready.
         */
        INIT,
        /**
         * Channel is initialized and ready, but is not performing on media
         */
        IDLE,
        /**
         * Channel is currently performing on some media
         */
        PERFORMING,
        /**
         * The Channel has encountered an error and is unable to perform
         */
        ERROR
	}
    /**
     * Returns the current status of the Channel.
     * @return current status of the Channel
     */
    public Status getStatus();
	
    /**
     * Returns the unique identifier for this Channel.
     * @return unique identifier for this Channel
     */
    public Ident getIdent();
    /**
     * Returns the name of the Channel.
     * @return name of the Channel
     */
    public String getName();
	
    /**
     * Returns the maximum number of simultaneous Performances allowed by this
     * Channel.
     * @return maximum number of simultaneous Performances allowed by this
     * Channel.
     */
    public int	getMaxAllowedPerformances();
	
    /**
     * Creates a new Performance instance to act on the given media.
     * @param media the media to be used by the performance
     * @return new Performance to act on the given media
     */
    public Performance<M, Time> makePerformanceForMedia(M media);
	
    /**
     * ??
     * @param perf
     * @param action
     * @param actionTime
     * @return
     */
    public boolean schedulePerfAction(Performance<M, Time> perf, Performance.Action action, Time actionTime);


    public interface Text<Time> extends Channel<Media.Text, Time> {
		
	}
    public interface Framed<Time, F> extends Channel<Media.Framed<F>, Time> {
		
	}
	
    /**
     * ??
     * @param <Time> 
     */
    public class Bank<Time> {
        public	List<Channel<?, Time>>	myWildcardChannels;
        
        public void test(Text<Time> textChan) { 
			myWildcardChannels.add(textChan);
		}
	}
}
