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
package org.cogchar.api.channel;

import org.appdapter.core.name.Ident;
import java.util.List;

/**
 * Channel is the primary boundary concept connecting lower-cogchar (Graphs + Services) with upper-cogchar (Scenes + Behaviors).
 * Both upper and lower layers know about channels, but view them differently.  
 * 
 * Channels are used as descriptions and
 * handles for data-flow pathways, but are not much mixed up in the implementation of those pathways, except in 
 * providing config parameters and a handle for reporting the channel status.
 * 
 * Channels are annotated with RDF-types and properties indicating their specific purpose and meaning.
 * 
 * A Channel is a handle to a useful symbol/signal stream resource producer/consumer,
 * seen from either side, but usually not both at the same time.   Hello Algebra!
 * 
 * @author Stu B. <www.texpedient.com>
 */

public interface Channel {
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
}
