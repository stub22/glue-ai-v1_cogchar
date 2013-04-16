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
 * * A PerfChannel is a Channel that produces Performances from a given Media type.
 * 
 * @param <M> Type of Media used by this Channel
 * @param <Time> Time unit used by the channel for timekeeping and scheduling
 * @author Stu B. <www.texpedient.com>
 */
public interface PerfChannel extends Channel  {
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
     * Returns the maximum number of simultaneous Performances allowed by this
     * Channel.
     * @return maximum number of simultaneous Performances allowed by this
     * Channel.
     */
    public int	getMaxAllowedPerformances();
	
    public <Cursor, M extends Media<Cursor>, Time> boolean schedulePerfInstruction(Performance<Cursor, M, Time> perf, Time worldTime, 
					Performance.Instruction<Cursor> instruct);

}
